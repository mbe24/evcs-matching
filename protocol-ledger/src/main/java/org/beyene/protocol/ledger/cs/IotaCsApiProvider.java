package org.beyene.protocol.ledger.cs;

import org.beyene.ledger.api.Data;
import org.beyene.ledger.api.Ledger;
import org.beyene.ledger.api.Mapper;
import org.beyene.ledger.api.TransactionListener;
import org.beyene.ledger.iota.IotaLedgerProvider;
import org.beyene.protocol.api.ApiProvider;
import org.beyene.protocol.api.CsApi;
import org.beyene.protocol.common.dto.Message;
import org.beyene.protocol.ledger.ev.IotaEvApi;
import org.beyene.protocol.ledger.util.JsonMessageMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class IotaCsApiProvider implements ApiProvider<CsApi, IotaCsOptions> {

    @Override
    public CsApi newApi(IotaCsOptions configuration) {
        Mapper<Message, String> mapper = new JsonMessageMapper();
        Map<String, TransactionListener<Message>> listener = new HashMap<>();
        //listener.put("EVBID", new BroadcastingListener(messagingTemplate));

        Map<String, Object> properties = new HashMap<>();
        properties.put("ssl.check.disable", Boolean.TRUE);
        properties.put("iota.node.protocol", configuration.node.getProtocol());
        properties.put("iota.node.host", configuration.node.getHost());
        properties.put("iota.node.port", Objects.toString(configuration.node.getPort()));

        Ledger<Message, String> ledger = new IotaLedgerProvider().newLedger(mapper, Data.STRING, listener, properties);
        return new IotaCsApi(ledger, configuration);
    }
}
