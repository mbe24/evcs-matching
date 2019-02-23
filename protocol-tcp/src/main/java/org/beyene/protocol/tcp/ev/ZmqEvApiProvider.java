package org.beyene.protocol.tcp.ev;

import org.beyene.protocol.api.ApiProvider;
import org.beyene.protocol.api.EvApi;

public class ZmqEvApiProvider implements ApiProvider<EvApi, ZmqEvOptions> {

    @Override
    public EvApi newApi(ZmqEvOptions configuration) {
        return new ZmqEvApi(configuration);
    }
}
