package org.beyene.protocol.tcp.ev;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beyene.protocol.api.EvApi;
import org.beyene.protocol.common.dto.CsOffer;
import org.beyene.protocol.common.dto.EvRequest;
import org.beyene.protocol.common.dto.EvReservation;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ZmqEvApi implements EvApi {

    private static final Log logger = LogFactory.getLog(ZmqEvApi.class);

    private final String name;
    private final ZContext context;
    private final ZMQ.Poller poller;
    private final List<String> addresses;
    private final ExecutorService executor;

    public ZmqEvApi(ZmqEvOptions configuration) {
        this.name = configuration.name;
        this.addresses = new ArrayList<>(configuration.endpoints);
        this.context = new ZContext();
        this.poller = context.createPoller(addresses.size());
        this.executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void init() throws Exception {
        addresses.stream()
                .map(this::createSocketAndConnect)
                .forEach(socket -> poller.register(socket, ZMQ.Poller.POLLIN));
    }

    private ZMQ.Socket createSocketAndConnect(String addr) {
        ZMQ.Socket socket = context.createSocket(ZMQ.DEALER);
        socket.setIdentity(name.getBytes(ZMQ.CHARSET));
        socket.connect(addr);
        logger.info("Trying to connect to: " + addr);
        return socket;
    }

    @Override
    public List<EvRequest> getRequests(String lastId) {
        return null;
    }

    @Override
    public EvRequest submitRequest(EvRequest request) {
        return null;
    }

    @Override
    public EvReservation updateReservation(String id, String option) {
        return null;
    }

    @Override
    public List<EvReservation> getReservations(String lastId) {
        return null;
    }

    @Override
    public void makeReservation(String offerId, String requestId) {

    }

    @Override
    public List<CsOffer> getOffers(String requestId, String lastId) {
        return null;
    }

    @Override
    public void close() throws IOException {
        // https://stackoverflow.com/questions/10504172/how-to-shutdown-an-executorservice
        executor.shutdownNow();

        if (!context.isClosed())
            context.close();
    }
}
