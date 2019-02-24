package org.beyene.protocol.tcp.cs;

import com.google.protobuf.util.JsonFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beyene.protocol.tcp.message.Message;
import org.beyene.protocol.tcp.util.MessageHandler;
import org.beyene.protocol.tcp.util.MetaMessage;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;
import zmq.ZError;

import java.nio.channels.ClosedByInterruptException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;

class ZmqIo implements Callable<Void>, MessageHandler {

    private static final Log logger = LogFactory.getLog(ZmqIo.class);

    private final ZMQ.Poller poller;
    private final BlockingQueue<MetaMessage> queue = new LinkedBlockingQueue<>();

    private final MessageHandler handler;

    public ZmqIo(ZMQ.Poller poller, MessageHandler handler) {
        this.poller = poller;
        this.handler = handler;
    }

    @Override
    public void handle(MetaMessage m) {
        logger.info("handle=" +  m);
        queue.add(m);
    }

    @Override
    public Void call() throws Exception {
        logger.info("started listening");
        while (!Thread.currentThread().isInterrupted()) {

            while (!queue.isEmpty()) {
                logger.info("polling queue...");
                MetaMessage item = queue.poll();

                Message message = item.message;
                String json = JsonFormat.printer().print(message);

                byte[] identity = item.addressee.getBytes(ZMQ.CHARSET);
                ZMsg response = new ZMsg();
                response.add(identity); // specify receiver by sending identity
                response.add(new byte[0]); // empty delimiter frame for dealer socket
                response.add(json);
                response.send(poller.getSocket(0));
                logger.info("sent=" + json);
            }

            try {
                poller.poll(10);
            } catch (Exception e) {
                logger.info("exit=" + e);
                if (ClosedByInterruptException.class.isInstance(e.getCause()))
                    return null;
                else
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

        logger.info("finished listening");
        return null;
    }

    private void handleRequest(ZMsg message) {
        ZFrame identity = message.poll();
        String id = identity.getString(ZMQ.CHARSET);


        Message.Builder builder = Message.newBuilder();
        try {
            String json = message.poll().getString(ZMQ.CHARSET);
            logger.info("json=" + json);
            JsonFormat.parser().ignoringUnknownFields().merge(json, builder);
        } catch (Exception e) {
            return;
        }

        Message response = builder.build();
        message.destroy();
        handler.handle(new MetaMessage(id, response));
    }

}
