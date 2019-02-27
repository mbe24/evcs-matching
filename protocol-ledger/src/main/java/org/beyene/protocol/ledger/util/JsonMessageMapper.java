package org.beyene.protocol.ledger.util;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beyene.ledger.api.Mapper;
import org.beyene.protocol.common.dto.Message;

public class JsonMessageMapper implements Mapper<Message, String> {

    private static final Log logger = LogFactory.getLog(JsonMessageMapper.class);

    @Override
    public String serialize(Message message) throws MappingException {
        try {
            String json = JsonFormat.printer().print(message);
            logger.debug("Sending=" + json);
            return json;
        } catch (InvalidProtocolBufferException e) {
            throw new MappingException(e.getMessage());
        }
    }

    @Override
    public Message deserialize(String json) throws MappingException {
        Message.Builder builder = Message.newBuilder();

        try {
            logger.debug("Received=" + json);
            JsonFormat.parser().ignoringUnknownFields().merge(json, builder);
        } catch (InvalidProtocolBufferException e) {
            throw new MappingException(e.getMessage());
        }

        return builder.build();
    }
}
