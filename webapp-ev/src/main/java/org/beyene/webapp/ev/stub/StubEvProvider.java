package org.beyene.webapp.ev.stub;

import org.beyene.protocol.api.ApiProvider;
import org.beyene.protocol.api.EvApi;

public class StubEvProvider implements ApiProvider<EvApi, StubOptions> {

    @Override
    public EvApi newApi(StubOptions configuration) {
        return new StubEvApi();
    }
}
