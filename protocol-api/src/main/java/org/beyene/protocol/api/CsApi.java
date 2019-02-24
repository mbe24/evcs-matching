package org.beyene.protocol.api;

import org.beyene.protocol.common.dto.CsOffer;
import org.beyene.protocol.common.dto.CsReservation;
import org.beyene.protocol.common.dto.CsReservation.Operation;

import org.beyene.protocol.common.dto.EvRequest;

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
