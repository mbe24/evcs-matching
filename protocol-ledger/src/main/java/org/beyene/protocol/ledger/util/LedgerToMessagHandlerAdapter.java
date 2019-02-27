package org.beyene.protocol.ledger.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beyene.ledger.api.Ledger;
import org.beyene.protocol.common.dto.Message;
import org.beyene.protocol.common.util.MessageHandler;
import org.beyene.protocol.common.util.MetaMessage;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class LedgerToMessagHandlerAdapter implements MessageHandler, Callable<Void>, Closeable {

    private static final Log logger = LogFactory.getLog(LedgerToMessagHandlerAdapter.class);

    private final Ledger<Message, String> ledger;
    private final BlockingQueue<MetaMessage> queue = new LinkedBlockingQueue<>();
    private final AtomicBoolean quit = new AtomicBoolean();

    public LedgerToMessagHandlerAdapter(Ledger<Message, String> ledger) {
        this.ledger = ledger;
    }

    @Override
    public void handle(MetaMessage m) {
        logger.info("Adding to send queue: " + m);
        queue.add(m);
    }

    @Override
    public Void call() {
        logger.info("Started listening");
        while (!Thread.currentThread().isInterrupted() && !quit.get()) {

            MetaMessage message;
            try {
                message = queue.take();
            } catch (InterruptedException e) {
                logger.info("Exiting due to interruption");
                break;
            }

            try {
                ledger.addTransaction(new MetaMessageToTransactionAdapter(message, ""));
                logger.debug("Dispatched message to ledger...");
            } catch (IOException e) {
                logger.info("Could not send transaction: " + e.getMessage());
                logger.debug("Error when writing: " + message, e);

                // for retry on error add message to front with LinkedBlockingDeque
                // maybe use timer with exponential backoff
            }

        }

        logger.info("Finished listening");
        return null;
    }

    @Override
    public void close() throws IOException {
        quit.set(true);
    }
}
