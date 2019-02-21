package org.beyene.protocol.api;

import org.beyene.protocol.common.dto.CsOffer;
import org.beyene.protocol.common.dto.EvRequest;
import org.beyene.protocol.common.dto.EvReservation;

import java.util.List;

public interface EvProtocol {

    List<EvRequest> getRequests(String lastId);

    EvRequest submitRequest(EvRequest request);

    EvReservation updateReservation(String id, String option);

    List<EvReservation> getReservations(String lastId);

    void makeReservation(String offerId, String requestId);

    List<CsOffer> getOffers(String requestId, String lastId);
}
