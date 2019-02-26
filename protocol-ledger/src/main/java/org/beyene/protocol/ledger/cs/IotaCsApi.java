package org.beyene.protocol.ledger.cs;

import org.beyene.ledger.api.Ledger;
import org.beyene.ledger.api.Transaction;
import org.beyene.ledger.api.TransactionListener;
import org.beyene.protocol.api.CsApi;
import org.beyene.protocol.api.data.CsOffer;
import org.beyene.protocol.api.data.CsReservation;
import org.beyene.protocol.api.data.EvRequest;
import org.beyene.protocol.common.dto.Message;

import java.io.IOException;
import java.util.List;

public class IotaCsApi implements CsApi, TransactionListener<Message> {

    private final Ledger<Message, String> ledger;

    public IotaCsApi(Ledger<Message, String> ledger, IotaCsOptions configuration) {
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
    public List<CsReservation> getReservations(String lastId) {
        return null;
    }

    @Override
    public void updateReservation(String id, CsReservation.Operation op) {

    }

    @Override
    public CsOffer submitOffer(String requestId, CsOffer offer) {
        return null;
    }

    @Override
    public List<CsOffer> getOffers(String requestId, String lastId) {
        return null;
    }

    @Override
    public void close() throws IOException {

    }
}
