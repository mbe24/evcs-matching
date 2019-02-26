package org.beyene.protocol.api;

import org.beyene.protocol.api.data.CsOffer;
import org.beyene.protocol.api.data.CsReservation;
import org.beyene.protocol.api.data.CsReservation.Operation;

import org.beyene.protocol.api.data.EvRequest;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;

public interface CsApi extends Closeable {

    @PostConstruct
    default void init() throws Exception {
    }

    List<EvRequest> getRequests(String lastId);

    List<CsReservation> getReservations(String lastId);

    void updateReservation(String id, Operation op);

    CsOffer submitOffer(String requestId, CsOffer offer);

    List<CsOffer> getOffers(String requestId, String lastId);

    @PreDestroy
    @Override
    default void close() throws IOException {
    }
}
