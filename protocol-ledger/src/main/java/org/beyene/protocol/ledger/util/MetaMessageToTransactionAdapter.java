package org.beyene.protocol.ledger.util;

import org.beyene.ledger.api.Transaction;
import org.beyene.protocol.common.dto.Message;
import org.beyene.protocol.common.util.MetaMessage;

import java.time.Instant;

public class MetaMessageToTransactionAdapter implements Transaction<Message> {

    private final Instant timestamp = Instant.now();
    private final MetaMessage metaMessage;
    private final String identifier;

    public MetaMessageToTransactionAdapter(MetaMessage metaMessage, String identifier) {
        this.metaMessage = metaMessage;
        this.identifier = identifier;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public String getTag() {
        return metaMessage.addressee;
    }

    @Override
    public Message getObject() {
        return metaMessage.message;
    }
}
