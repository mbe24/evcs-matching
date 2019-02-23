package org.beyene.webapp.cs.stub;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beyene.protocol.api.CsApi;
import org.beyene.protocol.common.dto.CsOffer;
import org.beyene.protocol.common.dto.CsReservation;
import org.beyene.protocol.common.dto.EvRequest;
import org.beyene.protocol.common.util.Data;
import org.springframework.core.style.ToStringCreator;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

class StubCsApi implements CsApi {

    private static final Log logger = LogFactory.getLog(StubCsApi.class);

    private final List<EvRequest> requests = new CopyOnWriteArrayList<>();
    private final List<CsReservation> reservations = new CopyOnWriteArrayList<>();
    private final Map<String, List<CsOffer>> offersByRequest = new ConcurrentHashMap<>();

    @Override
    public void init() throws Exception {
        logger.info("org.beyene.webapp.cs.stub.StubCsApi.init");
    }

    @Override
    public List<EvRequest> getRequests(String lastId) {
        int hash = Instant.now().hashCode();

        EvRequest request = new EvRequest();
        request.id = Objects.toString(Math.abs(hash));
        request.energy = Math.floor(100 * 100 * Math.random()) / 100;
        request.date = LocalDate.now();
        request.time = LocalTime.now();
        request.window = 250;

        requests.add(request);
        return Data.getNext(requests, lastId, req -> req.id);
    }

    @Override
    public void updateReservation(String id, CsReservation.Operation op) {
        logger.info("Update: " + id + ", op=" + op);

        OptionalInt index = Data.indexOf(reservations, id, reservation -> reservation.id);
        if (!index.isPresent())
            throw new IllegalArgumentException("there is no reservation with id: " + id);

        CsReservation reservation = reservations.get(index.getAsInt());
        switch (op) {
            case ACCEPT:
                reservation.status = CsReservation.Status.ACCEPTED;
                break;
            case REJECT:
                reservation.status = CsReservation.Status.REJECTED;
                break;
            default:
                throw new IllegalArgumentException("operation is not permitted: " + op);
        }
    }

    @Override
    public List<CsReservation> getReservations(String lastId) {
        int hash = Instant.now().hashCode();

        CsReservation reservation = new CsReservation();
        reservation.id = Objects.toString(Math.abs(hash));
        reservation.requestId = "" + 20277;
        reservation.offerId = "" + 22443;
        reservation.price = 55.45;
        reservation.payment = "PayPal";
        reservation.status = CsReservation.Status.OPEN;

        reservations.add(reservation);
        return reservations;
    }

    @Override
    public void submitOffer(String requestId, CsOffer offer) {
        OptionalInt index = Data.indexOf(requests, requestId, req -> req.id);

        if (!index.isPresent())
            throw new IllegalArgumentException("there is no request with id: " + requestId);

        int hash = Instant.now().hashCode();
        offer.id = Objects.toString(Math.abs(hash));

        String s = new ToStringCreator(offer)
                .append("price", offer.price)
                .append("energy", offer.energy)
                .append("date", offer.date)
                .append("time", offer.time)
                .append("window", offer.window)
                .toString();

        logger.info("RequestId=" + requestId + ", newOffer=" + s);
        List<CsOffer> offers = offersByRequest.get(requestId);

        if (Objects.isNull(offers))
            offersByRequest.put(requestId, offers = new ArrayList<>());

        offers.add(offer);
    }

    @Override
    public List<CsOffer> getOffers(String requestId, String lastId) {
        logger.info("RequestId=" + requestId + ", lastId=" + lastId);
        List<CsOffer> offers = offersByRequest.get(requestId);

        if (Objects.isNull(offers))
            offersByRequest.put(requestId, offers = new ArrayList<>());

        return Data.getNext(offers, lastId, offer -> offer.id);
    }

    @Override
    public void close()  {
        logger.info("org.beyene.webapp.cs.stub.StubCsApi.close");
    }
}
