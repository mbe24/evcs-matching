package org.beyene.webapp.cs.stub;

import org.beyene.protocol.api.ApiProvider;
import org.beyene.protocol.api.CsApi;

public class StubApiProvider implements ApiProvider<CsApi, StubOptions> {

    @Override
    public CsApi newApi(StubOptions configuration) {
        return new StubCsApi();
    }
}
