package org.beyene.protocol.api;

import java.util.Map;

public interface EvApiProvider {

    EvApi newEvApi(Map<String, Object> properties);
}
