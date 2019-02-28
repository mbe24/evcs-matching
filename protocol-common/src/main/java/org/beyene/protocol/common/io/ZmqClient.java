package org.beyene.protocol.common.io;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beyene.protocol.common.dto.Message;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.io.Closeable;
import java.nio.channels.ClosedByInterruptException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class ZmqClient implements Callable<Void>, MessageHandler, Closeable {

    private static final Log logger = LogFactory.getLog(ZmqClient.class);

    private final ZContext context;
    private final ZMQ.Poller poller;
    private final BlockingQueue<MetaMessage> queue = new LinkedBlockingQueue<>();

    private final MessageHandler handler;
    private final Map<String, Integer> sockets = new ConcurrentHashMap<>();
    private final Map<Integer, String> socketsReverse = new ConcurrentHashMap<>();

    private final AtomicBoolean quit = new AtomicBoolean();

    public ZmqClient(ZContext context, MessageHandler handler) {
        this.context = context;
        // poller gets grows, if size limit is reached
        this.poller = context.createPoller(0);
        this.handler = handler;
    }

    public boolean hasAddress(String address) {
        return sockets.containsKey(address);
    }

    public void connectAddress(String address, String name) {
        sockets.computeIfAbsent(address, addr -> {
            ZMQ.Socket socket = createSocketAndConnect(addr, name);
            int index = poller.register(socket, ZMQ.Poller.POLLIN);
            return index;
        });

        Map<Integer, String> map = sockets.entrySet()
                .stream()
                .collect(Collectors.toMap(e -> e.getValue(), e -> e.getKey()));

        synchronized (socketsReverse) {
            socketsReverse.clear();
            socketsReverse.putAll(map);
        }
    }

    private ZMQ.Socket createSocketAndConnect(String addr, String name) {
        ZMQ.Socket socket = context.createSocket(SocketType.DEALER);
        socket.setIdentity(name.getBytes(ZMQ.CHARSET));
        socket.connect(addr);
        logger.info("Trying to connect to: " + addr);
        return socket;
    }

    @Override
    public void close() {
        quit.set(true);

        // https://github.com/zeromq/jeromq/issues/489 -- release network resources, necessary for restart
        for (int socketIndex = 0; socketIndex < poller.getSize(); socketIndex++) {
            ZMQ.Socket socket = poller.getSocket(socketIndex);
            if (Objects.nonNull(socket))
                socket.disconnect(socketsReverse.get(socketIndex));
        }
    }

    @Override
    public void handle(MetaMessage m) {
        logger.info("Adding to send queue: " + m);
        queue.add(m);
    }

    @Override
    public Void call() throws Exception {
        logger.info("Started listening");

        try {
            callDelegate();
        } catch (Exception e) {
            logger.info("Error in ZmqClient!", e);
        }

        logger.info("Finished listening");
        return null;
    }

    private Void callDelegate() throws InvalidProtocolBufferException {
        while (!Thread.currentThread().isInterrupted() && !quit.get()) {

            while (!queue.isEmpty()) {
                logger.info("Polling queue...");
                MetaMessage item = queue.poll();

                Message message = item.message;
                String json = JsonFormat.printer().print(message);

                String addressee = item.addressee;
                int socketIndex = sockets.get(addressee);
                ZMQ.Socket currentSocket = poller.getSocket(socketIndex);
                currentSocket.send(json);
                logger.debug("Sent=" + json);
            }

            try {
                poller.poll(10);
            } catch (Exception e) {
                if (ClosedByInterruptException.class.isInstance(e.getCause())) {
                    logger.info("Exiting due to interruption");
                    break;
                } else {
                    logger.info("Error=" + e);
                    throw e;
                }
            }

            for (int socketIndex = 0; socketIndex < poller.getSize(); socketIndex++) {
                if (poller.pollin(socketIndex)) {
                    ZMQ.Socket currentSocket = poller.getSocket(socketIndex);
                    ZMsg message = ZMsg.recvMsg(currentSocket);
                    handleResponse(message, socketIndex);
                }
            }
        }

        return null;
    }

    private void handleResponse(ZMsg message, int socketIndex) {
        message.poll(); // delimiter frame

        Message.Builder builder = Message.newBuilder();
        try {
            String json = message.poll().getString(ZMQ.CHARSET);
            logger.debug("Received=" + json);
            JsonFormat.parser().ignoringUnknownFields().merge(json, builder);
        } catch (Exception e) {
            return;
        }

        Message response = builder.build();
        String address = socketsReverse.get(socketIndex);
        message.destroy();

        handler.handle(new MetaMessage(address, response));
    }
}
