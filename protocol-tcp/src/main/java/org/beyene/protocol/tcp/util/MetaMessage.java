package org.beyene.protocol.tcp.util;

import org.beyene.protocol.common.dto.Message;

public class MetaMessage {

    public String addressee;
    public Message message;

    public MetaMessage() {
    }

    public MetaMessage(String addressee, Message message) {
        this.addressee = addressee;
        this.message = message;
    }

    @Override
    public String toString() {
        return "MetaMessage{" +
                "addressee='" + addressee + '\'' +
                ", message=" + message +
                '}';
    }
}
