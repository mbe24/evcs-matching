package org.beyene.protocol.ledger.ev;

import com.google.protobuf.Timestamp;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beyene.ledger.api.*;
import org.beyene.ledger.iota.IotaLedgerProvider;
import org.beyene.protocol.api.EvApi;
import org.beyene.protocol.api.data.CsOffer;
import org.beyene.protocol.api.data.CsReservation;
import org.beyene.protocol.api.data.EvRequest;
import org.beyene.protocol.api.data.EvReservation;
import org.beyene.protocol.common.dto.*;
import org.beyene.protocol.common.util.MessageHandler;
import org.beyene.protocol.common.util.MetaMessage;
import org.beyene.protocol.ledger.util.JsonMessageMapper;
import org.beyene.protocol.ledger.util.LedgerToMessagHandlerAdapter;
import org.beyene.protocol.ledger.util.Util;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.beyene.protocol.common.util.Data.getNext;
import static org.beyene.protocol.common.util.Data.indexOf;

public class IotaEvApi implements EvApi, TransactionListener<Message>, MessageHandler {

    private static final Log logger = LogFactory.getLog(IotaEvApi.class);

    private final String name;
    private final String tag;

    private final ZContext context;
    private final ZMQ.Poller poller;

    private MessageHandler zmqHandler;
    private ZmqIo zmqIo;

    private Ledger<Message, String> ledger;
    private LedgerToMessagHandlerAdapter handler;

    private final Map<String, Object> properties;
    private final ExecutorService executor;

    private final List<EvRequest> requests = new CopyOnWriteArrayList<>();
    private final Map<String, List<CsOffer>> offersByRequest = new ConcurrentHashMap<>();
    private final Map<String, String> addressesByOffer = new ConcurrentHashMap<>();

    private final List<EvReservation> reservations = new CopyOnWriteArrayList<>();
    private final List<EvReservation> awaitingPayment = new CopyOnWriteArrayList<>();
    private final Map<String, List<String>> paymentByReservation = new ConcurrentHashMap<>();

    private final AtomicBoolean configured = new AtomicBoolean(false);

    public IotaEvApi(Map<String, Object> properties, IotaEvOptions configuration) {
        this.properties = properties;
        this.name = configuration.name;
        this.tag = configuration.tag;

        this.handler = new LedgerToMessagHandlerAdapter(ledger);

        this.context = new ZContext();
        // poller gets grows, if size limit is reached
        this.poller = context.createPoller(5);
        this.executor = Executors.newFixedThreadPool(2);
    }

    @Override
    public void init() throws Exception {
        if (configured.compareAndSet(false, true)) {
            logger.info("Initializing IOTA EV backend...");

            this.zmqIo = new ZmqIo(poller, this);
            this.zmqHandler = zmqIo;
            executor.submit(zmqIo);

            Mapper<Message, String> mapper = new JsonMessageMapper();
            this.ledger = new IotaLedgerProvider()
                    .newLedger(mapper, Data.STRING, Collections.emptyMap(), properties);
            this.handler = new LedgerToMessagHandlerAdapter(ledger);

            executor.submit(handler);
        } else {
            clearUserdata();
        }
    }

    private void clearUserdata() {
        logger.info("Reinitializing IOTA EV backend...");

        for (int socketIndex = 0; socketIndex < poller.getSize(); socketIndex++)
            poller.unregister(poller.getSocket(socketIndex));

        requests.clear();
        offersByRequest.clear();
        addressesByOffer.clear();
        reservations.clear();
        awaitingPayment.clear();
    }

    @Override
    public void onTransaction(Transaction<Message> transaction) {
        logger.debug("Entered onTransaction");

        Message message = transaction.getObject();

        String address = transaction.getTag();
        handle(new MetaMessage(address, message));
    }

    @Override
    public void handle(MetaMessage m) {
        Message message = m.message;
        String addressee = m.addressee;

        logger.info("Handle=" + m);

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

        String address = offer.getSource();
        if (!addressesByOffer.containsValue(address))
            registerEndpoint(address);
        addressesByOffer.put(csOffer.id, address);
    }

    private void registerEndpoint(String address) {
        /*
        synchronized (sockets) {
            if (sockets.isEmpty()) {
                sockets.put(address, 0);

                ZMQ.Socket socket = createSocketAndConnect(address);
                poller.register(socket, ZMQ.Poller.POLLIN);

                this.zmqIo = new ZmqIo(poller, this, sockets);
                this.zmqHandler = zmqIo;
                executor.submit(zmqIo);
            } else {
                ZMQ.Socket socket = createSocketAndConnect(address);
                zmqIo.registerSocket(address, socket);
            }
        }
        */

        synchronized (zmqIo) {
            ZMQ.Socket socket = createSocketAndConnect(address);
            zmqIo.registerSocket(address, socket);
        }
    }

    private ZMQ.Socket createSocketAndConnect(String addr) {
        ZMQ.Socket socket = context.createSocket(SocketType.DEALER);
        socket.setIdentity(name.getBytes(ZMQ.CHARSET));
        socket.connect(addr);
        logger.info("Trying to connect to: " + addr);
        return socket;
    }

    private void handleReservationAction(String addressee, ReservationAction reservationAction) {
        logger.info("Handling reservation action");

        Reservation reservation = reservationAction.getReservation();
        String requestId = reservation.getRequest();
        String offerId = reservation.getOffer();

        OptionalInt rIndex = indexOf(requests, requestId, r -> r.id);
        if (!rIndex.isPresent()) {
            throw new IllegalStateException("no request with id: " + requestId);
        }
        List<CsOffer> offers = offersByRequest.get(requestId);

        OptionalInt oIndex = indexOf(offers, offerId, o -> o.id);
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
            result = getNext(requests, lastId, request -> request.id);
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

        String requestTag = tag + "9" + Util.generateString(6);
        followNewTag(requestTag);

        LocalDateTime dateTime = LocalDateTime.of(request.date, request.time);
        ZoneId systemZone = ZoneId.systemDefault();
        ZoneOffset offset = systemZone.getRules().getOffset(dateTime);
        Instant instant = dateTime.toInstant(offset);
        Timestamp ts = Timestamp.newBuilder().setSeconds(instant.getEpochSecond()).setNanos(instant.getNano()).build();
        Request r = Request.newBuilder()
                .setSource(requestTag)
                .setId(request.id)
                .setEnergy(request.energy)
                .setDate(ts)
                .setWindow(request.window)
                .build();

        Message message = Message.newBuilder().setRequest(r).build();
        handler.handle(new MetaMessage(tag, message));
        return null;
    }

    private void followNewTag(String tag) {
        ledger.addTransactionListener(tag, this);
        logger.info("Following new tag: " + tag);
    }

    @Override
    public EvReservation updateReservation(String id, String option) {
        logger.info("Paying for reservation: " + id + "[" + option + "]");

        OptionalInt rIndex = indexOf(reservations, id, r -> r.id);
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
        zmqHandler.handle(new MetaMessage(address, message));

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
            result = getNext(reservations, lastId, reservation -> reservation.id);
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

        OptionalInt oIndex = indexOf(offers, offerId, o -> o.id);
        if (!oIndex.isPresent()) {
            logger.info("No offer with id: " + offerId);
            throw new IllegalArgumentException("there is no offer with id: " + offerId);
        }

        CsOffer offer = offers.get(oIndex.getAsInt());
        offer.reserved = true;

        Reservation reservation = Reservation.newBuilder()
                .setSource(name)
                .setOffer(offerId)
                .setRequest(requestId).build();
        Message message = Message.newBuilder().setReservation(reservation).build();

        String address = addressesByOffer.get(offerId);
        zmqHandler.handle(new MetaMessage(address, message));
    }

    @Override
    public List<CsOffer> getOffers(String requestId, String lastId) {
        logger.info("Offers for requestId=" + requestId + ", lastId=" + lastId);
        List<CsOffer> offers = offersByRequest.get(requestId);

        if (Objects.isNull(offers))
            offersByRequest.put(requestId, offers = new CopyOnWriteArrayList<>());

        List<CsOffer> next = getNext(offers, lastId, offer -> offer.id);
        logger.info("Returned offers: " + next.size());
        return next;
    }

    @Override
    public void close() throws IOException {
        ledger.close();
        handler.close();

        if (Objects.nonNull(zmqIo))
            zmqIo.close();

        executor.shutdownNow();
    }
}
