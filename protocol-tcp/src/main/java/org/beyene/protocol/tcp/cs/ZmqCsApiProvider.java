package org.beyene.protocol.tcp.cs;

import org.beyene.protocol.api.ApiProvider;
import org.beyene.protocol.api.CsApi;

public class ZmqCsApiProvider implements ApiProvider<CsApi, ZmqCsOptions> {

    @Override
    public CsApi newApi(ZmqCsOptions configuration) {
        return new ZmqCsApi(configuration);
    }
}
