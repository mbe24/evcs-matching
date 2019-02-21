package org.beyene.protocol.api;

import java.util.Map;

public interface CsApiProvider {

    // or use options object
    CsApi newCsApi(Map<String, Object> properties);
}
