package org.beyene.protocol.api;

import org.beyene.protocol.common.dto.CsOffer;
import org.beyene.protocol.common.dto.EvRequest;
import org.beyene.protocol.common.dto.EvReservation;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;

public interface EvApi extends Closeable {

    @PostConstruct
    default void init() throws Exception {
    }

    List<EvRequest> getRequests(String lastId);

    EvRequest submitRequest(EvRequest request);

    EvReservation updateReservation(String id, String option);

    List<EvReservation> getReservations(String lastId);

    void makeReservation(String offerId, String requestId);

    List<CsOffer> getOffers(String requestId, String lastId);

    @PreDestroy
    @Override
    default void close() throws IOException {
    }
}
