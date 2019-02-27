package org.beyene.protocol.ledger.cs;

import com.google.protobuf.util.JsonFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beyene.protocol.common.dto.Message;
import org.beyene.protocol.common.util.MessageHandler;
import org.beyene.protocol.common.util.MetaMessage;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

class ZmqIo implements Callable<Void>, MessageHandler, Closeable {

    private static final Log logger = LogFactory.getLog(ZmqIo.class);

    private final ZMQ.Poller poller;
    private final BlockingQueue<MetaMessage> queue = new LinkedBlockingQueue<>();

    private final MessageHandler handler;

    private final AtomicBoolean quit = new AtomicBoolean();

    public ZmqIo(ZMQ.Poller poller, MessageHandler handler) {
        this.poller = poller;
        this.handler = handler;
    }

    @Override
    public void close() throws IOException {
        quit.set(true);
    }

    @Override
    public void handle(MetaMessage m) {
        logger.info("Adding to send queue: " + m);
        queue.add(m);
    }

    @Override
    public Void call() throws Exception {
        logger.info("Started listening");
        while (!Thread.currentThread().isInterrupted() && !quit.get()) {

            while (!queue.isEmpty()) {
                logger.info("Polling queue...");
                MetaMessage item = queue.poll();

                Message message = item.message;
                String json = JsonFormat.printer().print(message);

                byte[] identity = item.addressee.getBytes(ZMQ.CHARSET);
                ZMsg response = new ZMsg();
                response.add(identity); // specify receiver by sending identity
                response.add(new byte[0]); // empty delimiter frame for dealer socket
                response.add(json);
                response.send(poller.getSocket(0));
                logger.debug("Sent=" + json);
            }

            try {
                poller.poll(10);
            } catch (Exception e) {
                logger.info("Exit=" + e);
                if (ClosedByInterruptException.class.isInstance(e.getCause())) {
                    logger.info("Exiting due to interruption");
                    break;
                } else
                    throw e;
            }

            if (poller.pollin(0)) {
                ZMQ.Socket currentSocket = poller.getSocket(0);
                ZMsg request = ZMsg.recvMsg(currentSocket);

                if (request == null || request.size() != 2)
                    continue;

                handleRequest(request);
            }
        }

        logger.info("Finished listening");
        return null;
    }

    private void handleRequest(ZMsg message) {
        ZFrame identity = message.poll();
        String id = identity.getString(ZMQ.CHARSET);


        Message.Builder builder = Message.newBuilder();
        try {
            String json = message.poll().getString(ZMQ.CHARSET);
            logger.debug("Received=" + json);
            JsonFormat.parser().ignoringUnknownFields().merge(json, builder);
        } catch (Exception e) {
            return;
        }

        Message response = builder.build();
        message.destroy();
        handler.handle(new MetaMessage(id, response));
    }

}
