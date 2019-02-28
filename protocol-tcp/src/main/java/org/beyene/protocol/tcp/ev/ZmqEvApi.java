package org.beyene.protocol.tcp.ev;

import com.google.protobuf.Timestamp;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beyene.protocol.api.EvApi;
import org.beyene.protocol.api.data.CsOffer;
import org.beyene.protocol.api.data.EvRequest;
import org.beyene.protocol.api.data.EvReservation;
import org.beyene.protocol.common.dto.*;
import org.beyene.protocol.common.io.MessageHandler;
import org.beyene.protocol.common.io.MetaMessage;
import org.beyene.protocol.common.io.ZmqClient;
import org.beyene.protocol.common.util.ApiUtil;
import org.beyene.protocol.common.util.HandlerUtil;
import org.beyene.protocol.common.util.Util;
import org.zeromq.ZContext;

import java.io.IOException;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

class ZmqEvApi implements EvApi, MessageHandler {

    private static final Log logger = LogFactory.getLog(ZmqEvApi.class);

    private final String name;
    private final List<String> addresses;

    private ZContext context;
    private MessageHandler handler;
    private ZmqClient zmqIo;
    private ExecutorService executor;

    private final List<EvRequest> requests = new CopyOnWriteArrayList<>();
    private final Map<String, List<CsOffer>> offersByRequest = new ConcurrentHashMap<>();
    private final Map<String, String> addressesByOffer = new ConcurrentHashMap<>();

    private final List<EvReservation> reservations = new CopyOnWriteArrayList<>();
    private final List<EvReservation> awaitingPayment = new CopyOnWriteArrayList<>();
    private final Map<String, List<String>> paymentByReservation = new ConcurrentHashMap<>();

    private final AtomicBoolean configured = new AtomicBoolean(false);

    public ZmqEvApi(ZmqEvOptions configuration) {
        this.name = configuration.name;
        this.addresses = new ArrayList<>(configuration.endpoints);
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
        logger.info("Initializing ZMQ EV backend...");

        this.context = new ZContext();
        this.zmqIo = new ZmqClient(context, this);
        this.executor = Executors.newSingleThreadExecutor();
        this.handler = zmqIo;

        addresses.stream().forEach(address -> {
            zmqIo.connectAddress(address, name);
        });

        executor.submit(zmqIo);
    }

    private void clearUserdata() {
        requests.clear();
        offersByRequest.clear();
        addressesByOffer.clear();
        reservations.clear();
        awaitingPayment.clear();
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
        HandlerUtil.handleReservationAction(requests, reservations, offersByRequest, awaitingPayment,
                addressee, reservationAction);
    }

    private void handlePaymentOption(String addressee, ReservationPaymentOption paymentOptions) {
        logger.info("Handling payment options");

        Reservation reservation = paymentOptions.getReservation();
        List<String> options = new ArrayList<>(paymentOptions.getOptionsList());
        paymentByReservation.put(reservation.getId(), options);
    }

    @Override
    public List<EvRequest> getRequests(String lastId) {
        return ApiUtil.getRequests(requests, lastId);
    }

    @Override
    public EvRequest submitRequest(EvRequest request) {
        Message message = ApiUtil.responseForSubmitRequest(requests, name, request, name);
        addresses.stream().map(addr -> new MetaMessage(addr, message)).forEach(handler::handle);
        return request;
    }

    @Override
    public EvReservation updateReservation(String id, String option) {
        return ApiUtil.updateEvReservation(reservations, addressesByOffer, handler, id, option);
    }

    @Override
    public List<EvReservation> getReservations(String lastId) {
        addMissingPaymentOptions();
        return ApiUtil.getEvReservations(reservations, lastId);
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
        ApiUtil.makeReservation(offersByRequest, addressesByOffer, name, handler, offerId, requestId);
    }

    @Override
    public List<CsOffer> getOffers(String requestId, String lastId) {
        return ApiUtil.getOffers(offersByRequest, requestId, lastId);
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
