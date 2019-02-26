package org.beyene.protocol.tcp.cs;

import com.google.protobuf.Timestamp;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beyene.protocol.api.CsApi;
import org.beyene.protocol.api.data.CsOffer;
import org.beyene.protocol.api.data.CsReservation;
import org.beyene.protocol.api.data.EvRequest;
import org.beyene.protocol.common.dto.*;
import org.beyene.protocol.common.util.Data;
import org.beyene.protocol.tcp.util.MessageHandler;
import org.beyene.protocol.tcp.util.MetaMessage;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

class ZmqCsApi implements CsApi, MessageHandler {

    private static final Log logger = LogFactory.getLog(ZmqCsApi.class);

    private final String name;
    private final ZContext context;
    private final ZMQ.Poller poller;

    private final String address;

    private final MessageHandler handler;
    private final ZmqIo zmqIo;
    private final ExecutorService executor;

    private final List<EvRequest> requests = new CopyOnWriteArrayList<>();
    private final Map<String, String> addressesByRequest = new ConcurrentHashMap<>();

    private final List<CsReservation> reservations = new CopyOnWriteArrayList<>();

    private final Map<String, List<CsOffer>> offersByRequest = new ConcurrentHashMap<>();

    private final List<String> paymentOptions;

    public ZmqCsApi(ZmqCsOptions configuration) {
        this.name = configuration.name;
        this.address = configuration.endpoint;
        this.paymentOptions = new ArrayList<>(configuration.paymentOptions);

        this.context = new ZContext();
        this.poller = context.createPoller(1);

        this.zmqIo = new ZmqIo(poller, this);
        this.executor = Executors.newSingleThreadExecutor();
        this.handler = zmqIo;
    }

    @Override
    public void init() throws Exception {
        ZMQ.Socket socket = createSocketAndBind(address);
        poller.register(socket, ZMQ.Poller.POLLIN);

        executor.submit(zmqIo);
    }

    private ZMQ.Socket createSocketAndBind(String addr) {
        ZMQ.Socket socket = context.createSocket(SocketType.ROUTER);
        socket.setIdentity(name.getBytes(ZMQ.CHARSET));
        socket.bind(addr);
        logger.info("Trying to bind to: " + addr);
        return socket;
    }

    @Override
    public void handle(MetaMessage m) {
        Message message = m.message;
        String addressee = m.addressee;

        try {
            if (message.hasRequest())
                handleRequest(addressee, message.getRequest());
            else if (message.hasForwardOffer())
                handleForwardOffer(addressee, message.getForwardOffer());
            else if (message.hasReservation())
                handleReservation(addressee, message.getReservation());
            else if (message.hasReservationAction())
                handleReservationAction(addressee, message.getReservationAction());
        } catch (Exception e) {
            logger.info("Error when handling message", e);
        }
    }

    private void handleRequest(String addressee, Request request) {
        Timestamp ts = request.getDate();
        Instant instant = Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

        EvRequest r = new EvRequest();
        r.id = request.getId();
        r.energy = request.getEnergy();
        r.date = dateTime.toLocalDate();
        r.time = dateTime.toLocalTime();
        r.window = request.getWindow();

        addressesByRequest.put(request.getId(), addressee);
        requests.add(r);
    }

    private void handleForwardOffer(String addressee, ForwardOffer forwardOffer) {
        Offer offer = forwardOffer.getOffer();

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
    }

    private void handleReservation(String addressee, Reservation reservation) {
        logger.info("Handling reservation");
        String requestId = reservation.getRequest();
        String offerId = reservation.getOffer();

        OptionalInt rIndex = Data.indexOf(requests, requestId, r -> r.id);
        if (!rIndex.isPresent()) {
            logger.info("No request with id: " + requestId);
            return;
        }
        List<CsOffer> offers = offersByRequest.get(requestId);

        OptionalInt oIndex = Data.indexOf(offers, offerId, o -> o.id);
        if (!oIndex.isPresent()) {
            logger.info("No offer with id: " + offerId);
            return;
        }

        CsOffer offer = offers.get(oIndex.getAsInt());

        CsReservation csReservation = new CsReservation();
        csReservation.offerId = reservation.getOffer();
        csReservation.requestId = requestId;
        csReservation.price = offer.price;
        csReservation.status = CsReservation.Status.OPEN;
        csReservation.payment = "";

        int hash = Objects.hash(new Random().nextDouble());
        csReservation.id = addressee + "-" + Objects.toString(hash).substring(0, 6);

        reservations.add(csReservation);
    }

    private void handleReservationAction(String addressee, ReservationAction reservationAction) {
        Reservation reservation = reservationAction.getReservation();

        if (Action.PAY != reservationAction.getAction())
            throw new IllegalArgumentException("invalid action [PAY] for reservation: " + reservation.getId());

        logger.info("Handling action for reservation: " + reservation.getId());

        OptionalInt rIndex = Data.indexOf(reservations, reservation.getId(), r -> r.id);
        if (!rIndex.isPresent()) {
            logger.info("No reservation with id: " + reservation.getId());
            return;
        }

        CsReservation csReservation = reservations.get(rIndex.getAsInt());
        csReservation.status = CsReservation.Status.PAID;
        csReservation.payment = reservationAction.getArgument();
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
    public List<CsReservation> getReservations(String lastId) {
        logger.info("Get reservations, lastId: " + lastId);

        List<CsReservation> result;
        if (Objects.isNull(lastId) || lastId.isEmpty() || lastId.equals("-1")) {
            result = reservations;
        } else {
            result = Data.getNext(reservations, lastId, reservation -> reservation.id);
        }

        return result;
    }

    @Override
    public void updateReservation(String id, CsReservation.Operation op) {
        logger.info("Update reservation: " + id + ". Operation: " + op);

        OptionalInt rIndex = Data.indexOf(reservations, id, r -> r.id);
        if (!rIndex.isPresent()) {
            logger.info("No reservation with id: " + id);
            return;
        }
        CsReservation reservation = reservations.get(rIndex.getAsInt());

        Reservation res = Reservation.newBuilder()
                .setId(reservation.id)
                .setOffer(reservation.offerId)
                .setRequest(reservation.requestId)
                .build();

        ReservationAction reservationAction = ReservationAction.newBuilder()
                .setReservation(res)
                .setAction(op == CsReservation.Operation.ACCEPT ? Action.ACCEPT : Action.REJECT)
                .build();

        String address = addressesByRequest.get(reservation.requestId);
        Message message = Message.newBuilder().setReservationAction(reservationAction).build();
        handler.handle(new MetaMessage(address, message));

        if (CsReservation.Operation.ACCEPT == op) {
            reservation.status = CsReservation.Status.ACCEPTED;

            ReservationPaymentOption paymentOption = ReservationPaymentOption.newBuilder()
                    .setReservation(res).addAllOptions(paymentOptions).build();
            Message m = Message.newBuilder().setPaymentOptions(paymentOption).build();
            handler.handle(new MetaMessage(address, m));
        } else {
            reservation.status = CsReservation.Status.REJECTED;
        }
    }

    @Override
    public CsOffer submitOffer(String requestId, CsOffer offer) {
        // in order to alter hash
        offer.time = offer.time.plusNanos(new Random().nextInt(250));
        int hash = Objects.hash(offer.time, offer.price, offer.energy, offer.window, offer.date);

        // encode name in id
        offer.id = name + "-" + Objects.toString(Math.abs(hash)).substring(0, 6);
        logger.info("New offer: " + offer);

        List<CsOffer> offers = offersByRequest.get(requestId);
        if (Objects.isNull(offers))
            offersByRequest.put(requestId, offers = new CopyOnWriteArrayList<>());
        offers.add(offer);

        LocalDateTime dateTime = LocalDateTime.of(offer.date, offer.time);
        ZoneId systemZone = ZoneId.systemDefault();
        ZoneOffset offset = systemZone.getRules().getOffset(dateTime);
        Instant instant = dateTime.toInstant(offset);
        Timestamp ts = Timestamp.newBuilder().setSeconds(instant.getEpochSecond()).setNanos(instant.getNano()).build();
        Offer offer1 = Offer.newBuilder()
                .setSource(name)
                .setRequestId(requestId)
                .setId(offer.id)
                .setEnergy(offer.energy)
                .setPrice(offer.price)
                .setDate(ts)
                .setWindow(offer.window)
                .build();

        Message message = Message.newBuilder().setOffer(offer1).build();

        String addr = addressesByRequest.get(requestId);
        handler.handle(new MetaMessage(addr, message));

        return offer;
    }

    @Override
    public List<CsOffer> getOffers(String requestId, String lastId) {
        logger.info("Offers for requestId=" + requestId + ", lastId=" + lastId);
        List<CsOffer> offers = offersByRequest.get(requestId);

        if (Objects.isNull(offers))
            offersByRequest.put(requestId, (offers = new CopyOnWriteArrayList<>()));

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
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
        }
    }
}
