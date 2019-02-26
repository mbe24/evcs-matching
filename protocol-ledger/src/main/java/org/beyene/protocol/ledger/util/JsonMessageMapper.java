package org.beyene.protocol.ledger.util;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import org.beyene.ledger.api.Mapper;
import org.beyene.protocol.common.dto.Message;

public class JsonMessageMapper implements Mapper<Message, String> {

    @Override
    public String serialize(Message message) throws MappingException {
        try {
            return JsonFormat.printer().print(message);
        } catch (InvalidProtocolBufferException e) {
            throw new MappingException(e.getMessage());
        }
    }

    @Override
    public Message deserialize(String s) throws MappingException {
        Message.Builder builder = Message.newBuilder();

        try {
            JsonFormat.parser().ignoringUnknownFields().merge(s, builder);
        } catch (InvalidProtocolBufferException e) {
            throw new MappingException(e.getMessage());
        }

        return builder.build();
    }
}
