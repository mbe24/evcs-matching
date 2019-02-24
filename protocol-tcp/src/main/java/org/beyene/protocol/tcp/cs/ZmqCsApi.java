package org.beyene.protocol.tcp.cs;

import com.google.protobuf.Timestamp;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beyene.protocol.api.CsApi;
import org.beyene.protocol.common.dto.CsOffer;
import org.beyene.protocol.common.dto.CsReservation;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class ZmqCsApi implements CsApi, MessageHandler {

    private static final Log logger = LogFactory.getLog(ZmqCsApi.class);

    private final String name;
    private final ZContext context;
    private final ZMQ.Poller poller;

    private final MessageHandler handler;
    private final ExecutorService executor;

    private final List<EvRequest> requests = new CopyOnWriteArrayList<>();
    private final Map<String, String> addressesByRequests = new ConcurrentHashMap<>();

    private final List<EvReservation> reservations = new CopyOnWriteArrayList<>();
    private final Map<String, List<CsOffer>> offersByRequest = new ConcurrentHashMap<>();

    public ZmqCsApi(ZmqCsOptions configuration) {
        this.name = configuration.name;

        this.context = new ZContext();
        this.poller = context.createPoller(1);
        ZMQ.Socket socket = createSocketAndBind(configuration.endpoint);
        poller.register(socket, ZMQ.Poller.POLLIN);

        ZmqIo io = new ZmqIo(poller, this);
        this.executor = Executors.newSingleThreadExecutor();
        executor.submit(io);

        this.handler = io;
    }

    private ZMQ.Socket createSocketAndBind(String addr) {
        ZMQ.Socket socket = context.createSocket(ZMQ.ROUTER);
        socket.setIdentity(name.getBytes(ZMQ.CHARSET));
        socket.bind(addr);
        logger.info("Trying to bind to: " + addr);
        return socket;
    }

    @Override
    public void handle(MetaMessage m) {
        logger.info("handle=" + m);

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
                handleReservationAction(addressee, message.hasReservationAction());
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

        addressesByRequests.put(request.getId(), addressee);
        requests.add(r);
    }

    private void handleForwardOffer(String addressee, ForwardOffer forwardOffer) {
    }

    private void handleReservation(String addressee, Reservation reservation) {
    }

    private void handleReservationAction(String addressee, boolean b) {
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
        return null;
    }

    @Override
    public void updateReservation(String id, CsReservation.Operation op) {
    }

    @Override
    public CsOffer submitOffer(String requestId, CsOffer offer) {
        // in order to alter hash
        offer.time = offer.time.plusNanos(new Random().nextInt(250));
        int hash = Objects.hash(offer.time, offer.price, offer.energy, offer.window, offer.date);

        // encode name in id
        offer.id = name + "-" + Objects.toString(Math.abs(hash)).substring(0, 6);

        String s = new ToStringCreator(offer)
                .append("id", offer.id)
                .append("price", offer.price)
                .append("energy", offer.energy)
                .append("date", offer.date)
                .append("time", offer.time)
                .append("window", offer.window)
                .toString();
        logger.info("New offer: " + s);

        List<CsOffer> offers = offersByRequest.get(requestId);
        if (Objects.isNull(offers))
            offersByRequest.put(requestId, offers = new CopyOnWriteArrayList<>());
        offers.add(offer);

        LocalDateTime dateTime = LocalDateTime.of(offer.date, offer.time);
        Instant instant = dateTime.toInstant(ZoneOffset.UTC);
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

        String addr = addressesByRequests.get(requestId);
        handler.handle(new MetaMessage(addr, message));

        return offer;
    }

    @Override
    public List<CsOffer> getOffers(String requestId, String lastId) {
        logger.info("Offers for requestId=" + requestId + ", lastId=" + lastId);
        List<CsOffer> offers = offersByRequest.get(requestId);

        if (Objects.isNull(offers))
            offersByRequest.put(requestId, (offers = new CopyOnWriteArrayList<>()));

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
