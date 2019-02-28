package org.beyene.protocol.ledger.cs;

import com.google.protobuf.Timestamp;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beyene.ledger.api.*;
import org.beyene.ledger.iota.IotaLedgerProvider;
import org.beyene.protocol.api.CsApi;
import org.beyene.protocol.api.data.CsOffer;
import org.beyene.protocol.api.data.CsReservation;
import org.beyene.protocol.api.data.EvRequest;
import org.beyene.protocol.common.io.ZmqServer;
import org.beyene.protocol.common.dto.*;
import org.beyene.protocol.common.io.MessageHandler;
import org.beyene.protocol.common.io.MetaMessage;
import org.beyene.protocol.ledger.util.JsonMessageMapper;
import org.beyene.protocol.ledger.util.LedgerToMessagHandlerAdapter;
import org.beyene.protocol.common.util.Util;
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

import static org.beyene.protocol.common.util.Data.getNext;
import static org.beyene.protocol.common.util.Data.indexOf;

class IotaCsApi implements CsApi, TransactionListener<Message>, MessageHandler {

    private static final Log logger = LogFactory.getLog(IotaCsApi.class);

    private final String name;
    private final String tag;
    private final String endpoint;
    private final boolean allTxs;

    private ZContext context;
    private ZMQ.Poller poller;
    private MessageHandler zmqHandler;
    private ZmqServer zmqIo;
    private Ledger<Message, String> ledger;
    private LedgerToMessagHandlerAdapter handler;
    private ExecutorService executor;

    private final Map<String, Object> properties;

    private final List<EvRequest> requests = new CopyOnWriteArrayList<>();
    private final Map<String, String> addressesByRequest = new ConcurrentHashMap<>();
    private final Map<String, String> tagsByRequest = new ConcurrentHashMap<>();
    private final Map<String, List<CsOffer>> offersByRequest = new ConcurrentHashMap<>();

    private final List<String> paymentOptions;
    private final List<CsReservation> reservations = new CopyOnWriteArrayList<>();

    private final AtomicBoolean configured = new AtomicBoolean(false);

    public IotaCsApi(Map<String, Object> properties, IotaCsOptions configuration) {
        this.properties = properties;
        this.name = configuration.name;

        this.endpoint = configuration.endpoint;
        this.paymentOptions = new ArrayList<>(configuration.paymentOptions);

        this.tag = configuration.tag;
        this.allTxs = configuration.allTxs;
    }

    @Override
    public void init() throws Exception {
        if (configured.compareAndSet(false, true)) {
            initialize();
        } else {
            clearUserdata();
            new Timer().schedule(Util.toTimerTask(() ->  initialize()), 1_000);
        }

        if (allTxs) {
            TimerTask task = Util.toTimerTask(() -> {
                logger.info("Reading all past transactions...");
                List<Transaction<Message>> txs = ledger.getTransactions(Instant.MIN, Instant.MAX);
                logger.info("Found: " + txs.size());
                txs.stream().forEach(this::onTransaction);
            });
            new Timer().schedule(task, 5_000);
        }
    }

    private void initialize() {
        this.context = new ZContext();
        this.poller = context.createPoller(1);
        ZMQ.Socket socket = createSocketAndBind(endpoint);
        poller.register(socket, ZMQ.Poller.POLLIN);
        this.zmqIo = new ZmqServer(poller, this);
        this.zmqHandler = zmqIo;

        this.executor = Executors.newFixedThreadPool(2);
        executor.submit(zmqIo);

        logger.info("Configuring with main tag: " + tag);
        Map<String, TransactionListener<Message>> listener = new HashMap<>();
        listener.put(tag, this);
        Mapper<Message, String> mapper = new JsonMessageMapper();
        this.ledger = new IotaLedgerProvider().newLedger(mapper, Data.STRING, listener, properties);
        this.handler = new LedgerToMessagHandlerAdapter(ledger);

        executor.submit(handler);
    }

    private void clearUserdata() {
        // deregister all listeners (except main tag)
        for (String tag : ledger.getTransactionListeners().keySet())
            if (!this.tag.equals(tag))
                ledger.removeTransactionListener(tag);

        requests.clear();
        tagsByRequest.clear();
        addressesByRequest.clear();
        offersByRequest.clear();
        reservations.clear();
    }

    private ZMQ.Socket createSocketAndBind(String addr) {
        ZMQ.Socket socket = context.createSocket(SocketType.ROUTER);
        socket.setIdentity(name.getBytes(ZMQ.CHARSET));
        socket.bind(addr);
        logger.info("Trying to bind to: " + addr);
        return socket;
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
            if (message.hasRequest())
                handleRequest(addressee, message.getRequest());
            else if (message.hasOffer())
                handleOffer(addressee, message.getOffer());
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

        tagsByRequest.put(r.id, request.getSource());
        followNewTag(request.getSource());
        requests.add(r);
    }

    private void followNewTag(String tag) {
        ledger.addTransactionListener(tag, this);
        logger.info("Following new tag: " + tag);
    }

    private void handleOffer(String addressee, Offer offer) {
        String requestId = offer.getRequestId();

        List<CsOffer> offers = offersByRequest.get(requestId);
        if (Objects.isNull(offers))
            offersByRequest.put(requestId, offers = new CopyOnWriteArrayList<>());

        String id = offer.getId();
        OptionalInt rIndex = indexOf(offers, id, o -> o.id);
        if (rIndex.isPresent()) {
            logger.info("Aborting. Read own offer from ledger: " + id);
            return;
        }

        Timestamp ts = offer.getDate();
        Instant instant = Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

        LocalDate date = dateTime.toLocalDate();
        LocalTime time = dateTime.toLocalTime();

        CsOffer csOffer = new CsOffer();
        csOffer.id = id;
        csOffer.energy = offer.getEnergy();
        csOffer.price = offer.getPrice();
        csOffer.date = date;
        csOffer.time = time;
        csOffer.window = offer.getWindow();


        offers.add(csOffer);
    }

    private void handleForwardOffer(String addressee, ForwardOffer fwdOffer) {
        logger.info("Received ForwardOffer on IOTA backend. Message is obsolete and therefore dropped: " + fwdOffer);
    }

    private void handleReservation(String addressee, Reservation reservation) {
        logger.info("Handling reservation");
        String requestId = reservation.getRequest();
        String offerId = reservation.getOffer();

        OptionalInt rIndex = indexOf(requests, requestId, r -> r.id);
        if (!rIndex.isPresent()) {
            logger.info("No request with id: " + requestId);
            return;
        }
        List<CsOffer> offers = offersByRequest.get(requestId);

        OptionalInt oIndex = indexOf(offers, offerId, o -> o.id);
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
        String address = reservation.getSource();
        addressesByRequest.put(requestId, address);
    }

    private void handleReservationAction(String addressee, ReservationAction reservationAction) {
        Reservation reservation = reservationAction.getReservation();

        if (Action.PAY != reservationAction.getAction())
            throw new IllegalArgumentException("invalid action [PAY] for reservation: " + reservation.getId());

        logger.info("Handling action for reservation: " + reservation.getId());

        OptionalInt rIndex = indexOf(reservations, reservation.getId(), r -> r.id);
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
            result = getNext(requests, lastId, request -> request.id);
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
            result = getNext(reservations, lastId, reservation -> reservation.id);
        }

        return result;
    }

    @Override
    public void updateReservation(String id, CsReservation.Operation op) {
        logger.info("Update reservation: " + id + ". Operation: " + op);

        OptionalInt rIndex = indexOf(reservations, id, r -> r.id);
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
        zmqHandler.handle(new MetaMessage(address, message));

        if (CsReservation.Operation.ACCEPT == op) {
            reservation.status = CsReservation.Status.ACCEPTED;

            ReservationPaymentOption paymentOption = ReservationPaymentOption.newBuilder()
                    .setReservation(res).addAllOptions(paymentOptions).build();
            Message m = Message.newBuilder().setPaymentOptions(paymentOption).build();
            zmqHandler.handle(new MetaMessage(address, m));
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
                .setSource(endpoint)
                .setRequestId(requestId)
                .setId(offer.id)
                .setEnergy(offer.energy)
                .setPrice(offer.price)
                .setDate(ts)
                .setWindow(offer.window)
                .build();

        Message message = Message.newBuilder().setOffer(offer1).build();

        String tag = tagsByRequest.get(requestId);
        handler.handle(new MetaMessage(tag, message));

        return offer;
    }

    @Override
    public List<CsOffer> getOffers(String requestId, String lastId) {
        logger.info("Offers for requestId=" + requestId + ", lastId=" + lastId);
        List<CsOffer> offers = offersByRequest.get(requestId);

        if (Objects.isNull(offers))
            offersByRequest.put(requestId, (offers = new CopyOnWriteArrayList<>()));

        List<CsOffer> next = getNext(offers, lastId, offer -> offer.id);
        logger.info("Returned offers: " + next.size());
        return next;
    }

    @Override
    public void close() throws IOException {
        // https://github.com/zeromq/jeromq/issues/489 -- release network resources, necessary for restart
        poller.getSocket(0).unbind(endpoint);

        zmqIo.close();
        if (!context.isClosed()) {
            context.close();
        }

        ledger.close();
        handler.close();

        executor.shutdownNow();
    }
}
