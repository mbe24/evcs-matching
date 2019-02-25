package org.beyene.protocol.tcp.ev;

import com.google.protobuf.util.JsonFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beyene.protocol.tcp.message.Message;
import org.beyene.protocol.tcp.util.MessageHandler;
import org.beyene.protocol.tcp.util.MetaMessage;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;
import zmq.ZError;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

class ZmqIo implements Callable<Void>, MessageHandler, Closeable {

    private static final Log logger = LogFactory.getLog(ZmqIo.class);

    private final ZMQ.Poller poller;
    private final BlockingQueue<MetaMessage> queue = new LinkedBlockingQueue<>();

    private final MessageHandler handler;
    private final Map<String, Integer> sockets;
    private final Map<Integer, String> socketsReverse = new HashMap<>();

    private final AtomicBoolean quit = new AtomicBoolean();

    public ZmqIo(ZMQ.Poller poller, MessageHandler handler, Map<String, Integer> sockets) {
        this.poller = poller;
        this.handler = handler;
        this.sockets = sockets;
        sockets.entrySet().stream().forEach(e -> socketsReverse.put(e.getValue(), e.getKey()));
    }

    @Override
    public void close() {
        quit.set(true);
    }

    @Override
    public void handle(MetaMessage m) {
        logger.info("handle=" + m);
        queue.add(m);
    }

    @Override
    public Void call() throws Exception {
        logger.info("started listening");
        while (!Thread.currentThread().isInterrupted() && !quit.get()) {

            while (!queue.isEmpty()) {
                logger.info("polling queue...");
                MetaMessage item = queue.poll();

                Message message = item.message;
                String json = JsonFormat.printer().print(message);

                String addressee = item.addressee;
                int socketIndex = sockets.get(addressee);
                ZMQ.Socket currentSocket = poller.getSocket(socketIndex);
                currentSocket.send(json);
                logger.info("sent=" + json);
            }

            poller.poll(10);

            for (int socketIndex = 0; socketIndex < poller.getSize(); socketIndex++) {
                if (poller.pollin(socketIndex)) {
                    ZMQ.Socket currentSocket = poller.getSocket(socketIndex);
                    ZMsg message = ZMsg.recvMsg(currentSocket);
                    handleResponse(message, socketIndex);
                }
            }
        }

        logger.info("finished listening");
        return null;
    }

    private void handleResponse(ZMsg message, int socketIndex) {
        message.poll(); // delimiter frame

        Message.Builder builder = Message.newBuilder();
        try {
            String json = message.poll().getString(ZMQ.CHARSET);
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
