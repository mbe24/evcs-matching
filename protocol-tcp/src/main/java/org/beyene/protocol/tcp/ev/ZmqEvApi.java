package org.beyene.protocol.tcp.ev;

import com.google.protobuf.Timestamp;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beyene.protocol.api.EvApi;
import org.beyene.protocol.api.data.CsOffer;
import org.beyene.protocol.api.data.CsReservation;
import org.beyene.protocol.api.data.EvRequest;
import org.beyene.protocol.api.data.EvReservation;
import org.beyene.protocol.common.dto.*;
import org.beyene.protocol.common.util.Data;
import org.beyene.protocol.common.util.MessageHandler;
import org.beyene.protocol.common.util.MetaMessage;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class ZmqEvApi implements EvApi, MessageHandler {

    private static final Log logger = LogFactory.getLog(ZmqEvApi.class);

    private final String name;
    private final ZContext context;
    private final ZMQ.Poller poller;

    private final List<String> addresses;

    private final MessageHandler handler;
    private final ZmqIo zmqIo;
    private final ExecutorService executor;

    private final List<EvRequest> requests = new CopyOnWriteArrayList<>();
    private final Map<String, List<CsOffer>> offersByRequest = new ConcurrentHashMap<>();
    private final Map<String, String> addressesByOffer = new ConcurrentHashMap<>();

    private final List<EvReservation> reservations = new CopyOnWriteArrayList<>();
    private final List<EvReservation> awaitingPayment = new CopyOnWriteArrayList<>();
    private final Map<String, List<String>> paymentByReservation = new ConcurrentHashMap<>();

    public ZmqEvApi(ZmqEvOptions configuration) {
        this.name = configuration.name;
        this.addresses = new ArrayList<>(configuration.endpoints);

        this.context = new ZContext();
        this.poller = context.createPoller(addresses.size());

        Map<String, Integer> sockets = IntStream.range(0, addresses.size())
                .boxed()
                .collect(Collectors.toMap(addresses::get, i -> i));

        this.zmqIo = new ZmqIo(poller, this, sockets);
        this.executor = Executors.newSingleThreadExecutor();
        this.handler = zmqIo;
    }

    @Override
    public void init() throws Exception {
        if (poller.getSize() != addresses.size()) {
            addresses.stream()
                    .map(this::createSocketAndConnect)
                    .forEach(socket -> poller.register(socket, ZMQ.Poller.POLLIN));

            executor.submit(zmqIo);
        } else {
            clearUserdata();
        }
    }

    private void clearUserdata() {
        requests.clear();
        offersByRequest.clear();
        addressesByOffer.clear();
        reservations.clear();
        awaitingPayment.clear();
    }

    private ZMQ.Socket createSocketAndConnect(String addr) {
        ZMQ.Socket socket = context.createSocket(SocketType.DEALER);
        socket.setIdentity(name.getBytes(ZMQ.CHARSET));
        socket.connect(addr);
        logger.info("Trying to connect to: " + addr);
        return socket;
    }

    @Override
    public void handle(MetaMessage m) {
        Message message = m.message;
        String addressee = m.addressee;

        try {
            if (message.hasOffer()) {
                handleOffer(addressee, message.getOffer());
            } else if (message.hasReservationAction()) {
                handleReservationAction(addressee, message.getReservationAction());
            } else if (message.hasPaymentOptions()) {
                handlePaymentOption(addressee, message.getPaymentOptions());
            }
        } catch (Exception e) {
            logger.info("Error when handling message", e);
        }
    }

    private void handleOffer(String addressee, Offer offer) {
        Timestamp ts = offer.getDate();
        Instant instant = Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

        String requestId = offer.getRequestId();
        LocalDate date = dateTime.toLocalDate();
        LocalTime time = dateTime.toLocalTime();

        CsOffer csOffer = new CsOffer();
        csOffer.id = offer.getId();
        csOffer.energy = offer.getEnergy();
        csOffer.price = offer.getPrice();
        csOffer.date = date;
        csOffer.time = time;
        csOffer.window = offer.getWindow();

        List<CsOffer> offers = offersByRequest.get(requestId);
        if (Objects.isNull(offers))
            offersByRequest.put(requestId, offers = new CopyOnWriteArrayList<>());

        offers.add(csOffer);
        addressesByOffer.put(csOffer.id, addressee);

        ForwardOffer forwardOffer = ForwardOffer.newBuilder()
                .setOffer(offer)
                .build();
        Message message = Message.newBuilder().setForwardOffer(forwardOffer).build();

        List<String> addrs = new ArrayList<>(addresses);
        addrs.remove(addressee);
        addrs.stream().map(addr -> new MetaMessage(addr, message)).forEach(handler::handle);
    }

    private void handleReservationAction(String addressee, ReservationAction reservationAction) {
        logger.info("Handling reservation action");

        Reservation reservation = reservationAction.getReservation();
        String requestId = reservation.getRequest();
        String offerId = reservation.getOffer();

        OptionalInt rIndex = Data.indexOf(requests, requestId, r -> r.id);
        if (!rIndex.isPresent()) {
            throw new IllegalStateException("no request with id: " + requestId);
        }
        List<CsOffer> offers = offersByRequest.get(requestId);

        OptionalInt oIndex = Data.indexOf(offers, offerId, o -> o.id);
        if (!oIndex.isPresent()) {
            throw new IllegalStateException("no offer with id: " + offerId);
        }

        CsOffer offer = offers.get(oIndex.getAsInt());
        EvReservation evReservation = new EvReservation();
        evReservation.id = reservation.getId();
        evReservation.offerId = offer.id;
        evReservation.requestId = requestId;

        Action action = reservationAction.getAction();
        CsReservation.Status status;
        if (Action.ACCEPT == action)
            status = CsReservation.Status.ACCEPTED;
        else if (Action.REJECT == action)
            status = CsReservation.Status.REJECTED;
        else {
            status = CsReservation.Status.REJECTED;
            logger.info("Action was not expected: " + action);
        }

        evReservation.status = status;
        evReservation.price = offer.price;
        if (CsReservation.Status.REJECTED == status) {
            reservations.add(evReservation);
            return;
        }

        awaitingPayment.add(evReservation);
    }

    private void handlePaymentOption(String addressee, ReservationPaymentOption paymentOptions) {
        logger.info("Handling payment options");

        Reservation reservation = paymentOptions.getReservation();
        List<String> options = new ArrayList<>(paymentOptions.getOptionsList());
        paymentByReservation.put(reservation.getId(), options);
    }

    @Override
    public List<EvRequest> getRequests(String lastId) {
        logger.info("Get requests, lastId: " + lastId);

        List<EvRequest> result;
        if (Objects.isNull(lastId) || lastId.isEmpty() || lastId.equals("-1")) {
            result = requests;
        } else {
            result = Data.getNext(requests, lastId, request -> request.id);
        }

        return result;
    }

    @Override
    public EvRequest submitRequest(EvRequest request) {
        // in order to alter hash
        request.time = request.time.plusNanos(new Random().nextInt(250));
        int hash = Objects.hash(request.time, request.energy, request.date, request.window);

        // encode name in id
        request.id = name + "-" + Objects.toString(Math.abs(hash)).substring(0, 6);
        logger.info("New request: " + request);

        requests.add(request);

        LocalDateTime dateTime = LocalDateTime.of(request.date, request.time);
        ZoneId systemZone = ZoneId.systemDefault();
        ZoneOffset offset = systemZone.getRules().getOffset(dateTime);
        Instant instant = dateTime.toInstant(offset);
        Timestamp ts = Timestamp.newBuilder().setSeconds(instant.getEpochSecond()).setNanos(instant.getNano()).build();
        Request r = Request.newBuilder()
                .setSource(name)
                .setId(request.id)
                .setEnergy(request.energy)
                .setDate(ts)
                .setWindow(request.window)
                .build();

        Message message = Message.newBuilder().setRequest(r).build();
        addresses.stream().map(addr -> new MetaMessage(addr, message)).forEach(handler::handle);

        return request;
    }

    @Override
    public EvReservation updateReservation(String id, String option) {
        logger.info("Paying for reservation: " + id + "[" + option + "]");

        OptionalInt rIndex = Data.indexOf(reservations, id, r -> r.id);
        if (!rIndex.isPresent()) {
            logger.info("No reservation with id: " + id);
            throw new IllegalArgumentException("there is no reservation with id: " + id);
        }

        EvReservation r = reservations.get(rIndex.getAsInt());
        r.status = CsReservation.Status.PAID;
        r.payment = option;

        Reservation reservation = Reservation.newBuilder()
                .setId(r.id)
                .setOffer(r.offerId)
                .setRequest(r.requestId)
                .build();

        Message message = Message.newBuilder()
                .setReservationAction(ReservationAction.newBuilder()
                        .setReservation(reservation)
                        .setAction(Action.PAY)
                        .setArgument(option))
                .build();
        String address = addressesByOffer.get(r.offerId);
        handler.handle(new MetaMessage(address, message));

        return r;
    }

    @Override
    public List<EvReservation> getReservations(String lastId) {
        logger.info("Get reservations, lastId: " + lastId);
        addMissingPaymentOptions();

        List<EvReservation> result;
        if (Objects.isNull(lastId) || lastId.isEmpty() || lastId.equals("-1")) {
            result = reservations;
        } else {
            result = Data.getNext(reservations, lastId, reservation -> reservation.id);
        }

        return result;
    }

    private void addMissingPaymentOptions() {
        List<EvReservation> gettingPaymentOption = awaitingPayment
                .stream()
                .filter(r -> paymentByReservation.containsKey(r.id))
                .collect(Collectors.toList());
        awaitingPayment.removeAll(gettingPaymentOption);
        gettingPaymentOption.forEach(r -> r.paymentOptions = paymentByReservation.get(r.id));
        reservations.addAll(gettingPaymentOption);
    }

    @Override
    public void makeReservation(String offerId, String requestId) {
        logger.info("Reservation for offer: " + offerId);

        List<CsOffer> offers = offersByRequest.get(requestId);

        if (Objects.isNull(offers))
            offersByRequest.put(requestId, offers = new CopyOnWriteArrayList<>());

        OptionalInt oIndex = Data.indexOf(offers, offerId, o -> o.id);
        if (!oIndex.isPresent()) {
            logger.info("No offer with id: " + offerId);
            throw new IllegalArgumentException("there is no offer with id: " + offerId);
        }

        CsOffer offer = offers.get(oIndex.getAsInt());
        offer.reserved = true;

        Reservation reservation = Reservation.newBuilder().setOffer(offerId).setRequest(requestId).build();
        Message message = Message.newBuilder().setReservation(reservation).build();
        String address = addressesByOffer.get(offerId);
        handler.handle(new MetaMessage(address, message));
    }

    @Override
    public List<CsOffer> getOffers(String requestId, String lastId) {
        logger.info("Offers for requestId=" + requestId + ", lastId=" + lastId);
        List<CsOffer> offers = offersByRequest.get(requestId);

        if (Objects.isNull(offers))
            offersByRequest.put(requestId, offers = new CopyOnWriteArrayList<>());

        List<CsOffer> next = Data.getNext(offers, lastId, offer -> offer.id);
        logger.info("Returned offers: " + next.size());
        return next;
    }

    @Override
    public void close() throws IOException {
        zmqIo.close();
        if (!context.isClosed()) {
            context.close();
        }

        // https://stackoverflow.com/questions/10504172/how-to-shutdown-an-executorservice
        executor.shutdownNow();
    }
}
