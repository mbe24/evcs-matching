package org.beyene.protocol.ledger.ev;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import org.beyene.ledger.api.*;
import org.beyene.ledger.iota.IotaLedgerProvider;
import org.beyene.protocol.api.ApiProvider;
import org.beyene.protocol.api.EvApi;
import org.beyene.protocol.common.dto.Message;
import org.beyene.protocol.ledger.util.JsonMessageMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class IotaEvApiProvider implements ApiProvider<EvApi, IotaEvOptions> {

    @Override
    public EvApi newApi(IotaEvOptions configuration) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("ssl.check.disable", Boolean.TRUE);
        properties.put("iota.node.protocol", configuration.node.getProtocol());
        properties.put("iota.node.host", configuration.node.getHost());
        properties.put("iota.node.port", Objects.toString(configuration.node.getPort()));

        return new IotaEvApi(properties, configuration);
    }

}
