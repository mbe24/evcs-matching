package org.beyene.protocol.ledger.cs;

import org.beyene.protocol.api.ApiProvider;
import org.beyene.protocol.api.CsApi;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class IotaCsApiProvider implements ApiProvider<CsApi, IotaCsOptions> {

    @Override
    public CsApi newApi(IotaCsOptions configuration) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("ssl.check.disable", Boolean.TRUE);
        properties.put("iota.node.protocol", configuration.node.getProtocol());
        properties.put("iota.node.host", configuration.node.getHost());
        properties.put("iota.node.port", Objects.toString(configuration.node.getPort()));

        return new IotaCsApi(properties, configuration);
    }
}
