package org.beyene.protocol.ledger.ev;

import org.beyene.ledger.api.Ledger;
import org.beyene.ledger.api.Transaction;
import org.beyene.ledger.api.TransactionListener;
import org.beyene.protocol.api.EvApi;
import org.beyene.protocol.api.data.CsOffer;
import org.beyene.protocol.api.data.EvRequest;
import org.beyene.protocol.api.data.EvReservation;
import org.beyene.protocol.common.dto.Message;

import java.io.IOException;
import java.util.List;

public class IotaEvApi implements EvApi, TransactionListener<Message> {


    private final Ledger<Message, String> ledger;

    public IotaEvApi(Ledger<Message, String> ledger, IotaEvOptions configuration) {
        this.ledger = ledger;
        ledger.addTransactionListener(configuration.tag, this);
    }

    @Override
    public void init() throws Exception {

    }

    @Override
    public void onTransaction(Transaction<Message> transaction) {

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
