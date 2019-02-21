package org.beyene.protocol.api;

import org.beyene.protocol.common.dto.CsOffer;
import org.beyene.protocol.common.dto.CsReservation;
import org.beyene.protocol.common.dto.CsReservation.Operation;

import org.beyene.protocol.common.dto.EvRequest;

import java.util.List;

public interface CsApi {

    List<EvRequest> getRequests(String lastId);

    List<CsReservation> getReservations(String lastId);

    void updateReservation(String id, Operation op);

    void submitOffer(String requestId, CsOffer offer);

    List<CsOffer> getOffers(String requestId, String lastId);
}
