package org.beyene.protocol.tcp.ev;

import com.google.protobuf.Timestamp;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beyene.protocol.api.EvApi;
import org.beyene.protocol.common.dto.CsOffer;
import org.beyene.protocol.common.dto.EvRequest;
import org.beyene.protocol.common.dto.EvReservation;
import org.beyene.protocol.common.util.Data;
import org.beyene.protocol.tcp.message.*;
import org.beyene.protocol.tcp.util.MessageHandler;
import org.beyene.protocol.tcp.util.MetaMessage;
import org.springframework.core.style.ToStringCreator;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class ZmqEvApi implements EvApi, MessageHandler {

    private static final Log logger = LogFactory.getLog(ZmqEvApi.class);

    private final String name;
    private final ZContext context;
    private final ZMQ.Poller poller;

    private final List<String> addresses;

    private final MessageHandler handler;
    private final ExecutorService executor;

    private final List<EvRequest> requests = new CopyOnWriteArrayList<>();
    private final List<EvReservation> reservations = new CopyOnWriteArrayList<>();
    private final Map<String, List<CsOffer>> offersByRequest = new ConcurrentHashMap<>();

    public ZmqEvApi(ZmqEvOptions configuration) {
        this.name = configuration.name;
        this.addresses = new ArrayList<>(configuration.endpoints);

        this.context = new ZContext();
        this.poller = context.createPoller(addresses.size());

        addresses.stream()
                .map(this::createSocketAndConnect)
                .forEach(socket -> poller.register(socket, ZMQ.Poller.POLLIN));

        Map<String, Integer> sockets = IntStream.range(0, addresses.size())
                .boxed()
                .collect(Collectors.toMap(addresses::get, i -> i));

        ZmqIo io = new ZmqIo(poller, this, sockets);
        this.executor = Executors.newSingleThreadExecutor();
        executor.submit(io);

        this.handler = io;
    }

    private ZMQ.Socket createSocketAndConnect(String addr) {
        ZMQ.Socket socket = context.createSocket(ZMQ.DEALER);
        socket.setIdentity(name.getBytes(ZMQ.CHARSET));
        socket.connect(addr);
        logger.info("Trying to connect to: " + addr);
        return socket;
    }

    @Override
    public void handle(MetaMessage m) {
        logger.info("handle=" + m);
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

        ForwardOffer forwardOffer = ForwardOffer.newBuilder()
                .setOffer(offer)
                .build();
        Message message = Message.newBuilder().setForwardOffer(forwardOffer).build();

        List<String> addrs = new ArrayList<>(addresses);
        addrs.remove(addressee);
        addrs.stream().map(addr -> new MetaMessage(addr, message)).forEach(handler::handle);
    }

    private void handleReservationAction(String addressee, ReservationAction reservationAction) {
    }

    private void handlePaymentOption(String addressee, ReservationPaymentOption paymentOptions) {
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

        String s = new ToStringCreator(request)
                .append("id", request.id)
                .append("energy", request.energy)
                .append("date", request.date)
                .append("time", request.time)
                .append("window", request.window)
                .toString();
        logger.info("New request: " + s);

        requests.add(request);

        LocalDateTime dateTime = LocalDateTime.of(request.date, request.time);
        Instant instant = dateTime.toInstant(ZoneOffset.UTC);
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
        return null;
    }

    @Override
    public List<EvReservation> getReservations(String lastId) {
        logger.info("Get reservations, lastId: " + lastId);

        List<EvReservation> result;
        if (Objects.isNull(lastId) || lastId.isEmpty() || lastId.equals("-1")) {
            result = reservations;
        } else {
            result = Data.getNext(reservations, lastId, reservation -> reservation.id);
        }

        return result;
    }

    @Override
    public void makeReservation(String offerId, String requestId) {

    }

    @Override
    public List<CsOffer> getOffers(String requestId, String lastId) {
        logger.info("Offers for requestId=" + requestId + ", lastId=" + lastId);
        List<CsOffer> offers = offersByRequest.get(requestId);

        if (Objects.isNull(offers))
            offersByRequest.put(requestId, offers = new CopyOnWriteArrayList<>());

        return Data.getNext(offers, lastId, offer -> offer.id);
    }

    @Override
    public void close() throws IOException {
        // https://stackoverflow.com/questions/10504172/how-to-shutdown-an-executorservice
        executor.shutdownNow();

        if (!context.isClosed())
            context.close();
    }
}
