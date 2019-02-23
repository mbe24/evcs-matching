package org.beyene.protocol.tcp.cs;

import org.beyene.protocol.api.CsApi;
import org.beyene.protocol.common.dto.CsOffer;
import org.beyene.protocol.common.dto.CsReservation;
import org.beyene.protocol.common.dto.EvRequest;

import java.io.IOException;
import java.util.List;

public class ZmqCsApi implements CsApi {

    @Override
    public void init() throws Exception {

    }

    @Override
    public List<EvRequest> getRequests(String lastId) {
        return null;
    }

    @Override
    public List<CsReservation> getReservations(String lastId) {
        return null;
    }

    @Override
    public void updateReservation(String id, CsReservation.Operation op) {

    }

    @Override
    public void submitOffer(String requestId, CsOffer offer) {

    }

    @Override
    public List<CsOffer> getOffers(String requestId, String lastId) {
        return null;
    }

    @Override
    public void close() throws IOException {

    }
}
