package org.beyene.protocol.ledger.ev;

import org.beyene.protocol.api.EvApi;
import org.beyene.protocol.common.dto.CsOffer;
import org.beyene.protocol.common.dto.EvRequest;
import org.beyene.protocol.common.dto.EvReservation;

import java.io.IOException;
import java.util.List;

public class IotaEvApi implements EvApi {

    @Override
    public void init() throws Exception {

    }

    @Override
    public List<EvRequest> getRequests(String lastId) {
        return null;
    }

    @Override
    public EvRequest submitRequest(EvRequest request) {
        return null;
    }

    @Override
    public EvReservation updateReservation(String id, String option) {
        return null;
    }

    @Override
    public List<EvReservation> getReservations(String lastId) {
        return null;
    }

    @Override
    public void makeReservation(String offerId, String requestId) {

    }

    @Override
    public List<CsOffer> getOffers(String requestId, String lastId) {
        return null;
    }

    @Override
    public void close() throws IOException {

    }
}
