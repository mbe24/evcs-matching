package org.beyene.protocol.tcp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beyene.protocol.api.EvApi;
import org.beyene.protocol.common.dto.CsOffer;
import org.beyene.protocol.common.dto.CsReservation;
import org.beyene.protocol.common.dto.EvRequest;
import org.beyene.protocol.common.dto.EvReservation;
import org.beyene.webapp.common.Data;
import org.springframework.core.style.ToStringCreator;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;


@Component
public class EvApiComponent implements EvApi {

    private static final Log logger = LogFactory.getLog(EvApiComponent.class);

    private final List<EvRequest> requests = new CopyOnWriteArrayList<>();

    private final List<EvReservation> preReservations = new CopyOnWriteArrayList<>();

    @Override
    public List<EvRequest> getRequests(String lastId) {
        logger.info("Get requests, lastId: " + lastId);

        List<EvRequest> result;
        if (Objects.isNull(lastId) || lastId.isEmpty() || lastId.equals("-1")) {
            result = requests;
        } else {
            result = Data.getNext(requests, lastId, request -> request.id);
        }

        return result;
    }

    @Override
    public EvRequest submitRequest(EvRequest request) {
        // in order to alter hash
        request.time = request.time.plusNanos(new Random().nextInt(250));
        String s = new ToStringCreator(request)
                .append("energy", request.energy)
                .append("date", request.date)
                .append("time", request.time)
                .append("window", request.window)
                .toString();
        logger.info("New request: " + s);

        int hash = Objects.hash(request.energy, request.date, request.time, request.window);

        EvRequest copy = new EvRequest();
        copy.id = Objects.toString(Math.abs(hash));
        copy.energy = request.energy;
        copy.date = request.date;
        copy.time = request.time;
        copy.window = request.window;

        requests.add(copy);
        return copy;
    }

    @Override
    public EvReservation updateReservation(String id, String option) {
        logger.info("Reservation: " + id + ", payment=" + option);
        OptionalInt index = Data.indexOf(preReservations, id, reservation -> reservation.id);

        if (!index.isPresent())
            throw new IllegalArgumentException("there is no reservation with id: " + id);

        EvReservation reservation = preReservations.get(index.getAsInt());
        reservation.status = CsReservation.Status.PAID;
        reservation.payment = option;
        return reservation;
    }

    @Override
    public List<EvReservation> getReservations(String lastId) {

        int hash = Instant.now().hashCode();

        EvReservation reservation = new EvReservation();
        reservation.id = Objects.toString(Math.abs(hash));
        reservation.requestId = "" + 20277;
        reservation.offerId = "" + 22443;
        reservation.price = 55.45;
        reservation.paymentOptions = Arrays.asList("PayPal", "BTC", "LTC", "XRP");
        reservation.status = new Random().nextInt(10) > 4 ? CsReservation.Status.ACCEPTED : CsReservation.Status.REJECTED;


        preReservations.add(reservation);
        return Arrays.asList(reservation);
    }

    @Override
    public void makeReservation(String offerId, String requestId) {
        logger.info("Reservation for offerId=" + offerId + ", requestId=" + requestId);
    }

    @Override
    public List<CsOffer> getOffers(String requestId, String lastId) {
        logger.info("RequestId=" + requestId + ", lastId=" + lastId);

        int hash = Instant.now().hashCode();

        CsOffer offer = new CsOffer();
        offer.id = Objects.toString(Math.abs(hash));
        offer.energy = Math.floor(100 * 100 * Math.random()) / 100;
        offer.price = Math.floor(100 * offer.energy * 0.2) / 100;
        offer.date = LocalDate.now();
        offer.time = LocalTime.now();
        offer.window = 30;

        List<CsOffer> offers = new ArrayList<>();
        offers.add(offer);
        return offers;
    }
}
