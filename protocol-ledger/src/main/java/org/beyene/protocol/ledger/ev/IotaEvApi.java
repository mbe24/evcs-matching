package org.beyene.protocol.ledger.ev;

import com.google.protobuf.Timestamp;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beyene.ledger.api.*;
import org.beyene.ledger.iota.IotaLedgerProvider;
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
import org.beyene.protocol.ledger.util.JsonMessageMapper;
import org.beyene.protocol.ledger.util.LedgerToMessagHandlerAdapter;
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

class IotaEvApi implements EvApi, TransactionListener<Message>, MessageHandler {

    private static final Log logger = LogFactory.getLog(IotaEvApi.class);

    private final String name;
    private final String tag;

    private ZContext context;
    private MessageHandler zmqHandler;
    private ZmqClient zmqIo;
    private ExecutorService executor;
    private Ledger<Message, String> ledger;
    private LedgerToMessagHandlerAdapter handler;

    private final Map<String, Object> properties;

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
        logger.info("Initializing IOTA EV backend...");

        this.context = new ZContext();
        this.executor = Executors.newFixedThreadPool(2);

        this.zmqIo = new ZmqClient(context, this);
        this.zmqHandler = zmqIo;
        executor.submit(zmqIo);

        Mapper<Message, String> mapper = new JsonMessageMapper();
        this.ledger = new IotaLedgerProvider()
                .newLedger(mapper, Data.STRING, Collections.emptyMap(), properties);
        this.handler = new LedgerToMessagHandlerAdapter(ledger);

        executor.submit(handler);
    }

    private void clearUserdata() {
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
        if (!zmqIo.hasAddress(address))
            zmqIo.connectAddress(address, name);

        addressesByOffer.put(csOffer.id, address);
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
        String requestTag = tag + "9" + Util.generateString(6);
        Message message = ApiUtil.responseForSubmitRequest(requests, name, request, requestTag);
        MetaMessage m = new MetaMessage(tag, message);
        handler.handle(m);

        followNewTag(requestTag);
        return request;
    }

    private void followNewTag(String tag) {
        ledger.addTransactionListener(tag, this);
        logger.info("Following new tag: " + tag);
    }

    @Override
    public EvReservation updateReservation(String id, String option) {
        return ApiUtil.updateEvReservation(reservations, addressesByOffer, zmqHandler, id, option);
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
        ApiUtil.makeReservation(offersByRequest, addressesByOffer, name, zmqHandler, offerId, requestId);
    }

    @Override
    public List<CsOffer> getOffers(String requestId, String lastId) {
        return ApiUtil.getOffers(offersByRequest, requestId, lastId);
    }

    @Override
    public void close() throws IOException {
        try {
            ledger.close();
            handler.close();

            if (Objects.nonNull(zmqIo))
                zmqIo.close();

            executor.shutdownNow();

        } catch (Exception e) {
            logger.info("Error on close", e);
            throw e;
        }
    }

}
