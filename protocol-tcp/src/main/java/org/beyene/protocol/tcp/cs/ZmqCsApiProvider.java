package org.beyene.protocol.tcp.cs;

import org.beyene.protocol.api.CsApi;
import org.beyene.protocol.api.ApiProvider;

import java.util.List;

public class ZmqCsApiProvider implements ApiProvider<CsApi, ZmqCsOptions> {

    @Override
    public CsApi newApi(ZmqCsOptions configuration) {
        return null;
    }
}
