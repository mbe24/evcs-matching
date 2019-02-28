package org.beyene.protocol.tcp.cs;

import com.google.protobuf.Timestamp;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beyene.protocol.api.CsApi;
import org.beyene.protocol.api.data.CsOffer;
import org.beyene.protocol.api.data.CsReservation;
import org.beyene.protocol.api.data.EvRequest;
import org.beyene.protocol.common.dto.*;
import org.beyene.protocol.common.io.MessageHandler;
import org.beyene.protocol.common.io.MetaMessage;
import org.beyene.protocol.common.io.ZmqServer;
import org.beyene.protocol.common.util.ApiUtil;
import org.beyene.protocol.common.util.HandlerUtil;
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

class ZmqCsApi implements CsApi, MessageHandler {

    private static final Log logger = LogFactory.getLog(ZmqCsApi.class);

    private final String name;
    private final String endpoint;

    private ZContext context;
    private ZMQ.Poller poller;
    private ZmqServer zmqIo;
    private MessageHandler handler;
    private ExecutorService executor;

    private final List<EvRequest> requests = new CopyOnWriteArrayList<>();
    private final Map<String, String> addressesByRequest = new ConcurrentHashMap<>();
    private final Map<String, List<CsOffer>> offersByRequest = new ConcurrentHashMap<>();

    private final List<CsReservation> reservations = new CopyOnWriteArrayList<>();
    private final List<String> paymentOptions;

    private final AtomicBoolean configured = new AtomicBoolean(false);

    public ZmqCsApi(ZmqCsOptions configuration) {
        this.name = configuration.name;
        this.endpoint = configuration.endpoint;
        this.paymentOptions = new ArrayList<>(configuration.paymentOptions);
    }

    @Override
    public void init() throws Exception {
        if (configured.compareAndSet(false, true)) {
            initialize();
        } else {
            clearUserdata();
            new Timer().schedule(Util.toTimerTask(() -> initialize()), 1_000);
        }
    }

    private void initialize() {
        logger.info("Initializing ZMQ CS backend...");

        this.context = new ZContext();
        this.poller = context.createPoller(1);

        ZMQ.Socket socket = createSocketAndBind(endpoint);
        this.poller.register(socket, ZMQ.Poller.POLLIN);
        this.zmqIo = new ZmqServer(poller, this);
        this.handler = zmqIo;

        this.executor = Executors.newSingleThreadExecutor();
        this.executor.submit(zmqIo);
    }

    private ZMQ.Socket createSocketAndBind(String addr) {
        ZMQ.Socket socket = context.createSocket(SocketType.ROUTER);
        socket.setIdentity(name.getBytes(ZMQ.CHARSET));
        socket.bind(addr);
        logger.info("Trying to bind to: " + addr);
        return socket;
    }

    private void clearUserdata() {
        requests.clear();
        addressesByRequest.clear();
        offersByRequest.clear();
        reservations.clear();
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
        HandlerUtil.handleRequest(requests, addressesByRequest, addressee, request);
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
        HandlerUtil.handleReservation(requests, offersByRequest, reservations, addressee, reservation);
    }

    private void handleReservationAction(String addressee, ReservationAction reservationAction) {
        HandlerUtil.handleReservationActionCs(reservations, addressee, reservationAction);
    }

    @Override
    public List<EvRequest> getRequests(String lastId) {
        return ApiUtil.getRequests(requests, lastId);
    }

    @Override
    public List<CsReservation> getReservations(String lastId) {
        return ApiUtil.getCsReservations(reservations, lastId);
    }

    @Override
    public void updateReservation(String id, CsReservation.Operation op) {
        ApiUtil.updateCsReservation(reservations, addressesByRequest, handler, paymentOptions, id, op);
    }

    @Override
    public CsOffer submitOffer(String requestId, CsOffer offer) {
        return ApiUtil.submitOffer(offersByRequest, name, handler, name, addressesByRequest, requestId, offer);
    }

    @Override
    public List<CsOffer> getOffers(String requestId, String lastId) {
        return ApiUtil.getOffers(offersByRequest, requestId, lastId);
    }

    @Override
    public void close() throws IOException {
        // https://github.com/zeromq/jeromq/issues/489 -- release network resources, necessary for restart
        poller.getSocket(0).unbind(endpoint);

        zmqIo.close();
        if (!context.isClosed()) {
            context.close();
        }

        // https://stackoverflow.com/questions/10504172/how-to-shutdown-an-executorservice
        executor.shutdownNow();
    }
}
