package org.beyene.protocol.common.util;

import com.google.protobuf.Timestamp;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beyene.protocol.api.data.CsOffer;
import org.beyene.protocol.api.data.CsReservation;
import org.beyene.protocol.api.data.EvRequest;
import org.beyene.protocol.common.dto.Action;
import org.beyene.protocol.common.dto.Request;
import org.beyene.protocol.common.dto.Reservation;
import org.beyene.protocol.common.dto.ReservationAction;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public final class HandlerUtil {

    private static final Log logger = LogFactory.getLog(ApiUtil.class);

    private HandlerUtil() {
        throw new AssertionError("no instances allowed");
    }

    public static void handleRequest(List<EvRequest> requests,
                                     Map<String, String> targetsByRequestId,
                                     String addressee, Request request) {
        Timestamp ts = request.getDate();
        Instant instant = Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

        EvRequest r = new EvRequest();
        r.id = request.getId();
        r.energy = request.getEnergy();
        r.date = dateTime.toLocalDate();
        r.time = dateTime.toLocalTime();
        r.window = request.getWindow();

        targetsByRequestId.put(request.getId(), addressee);
        requests.add(r);
    }

    public static void handleReservation(List<EvRequest> requests,
                                         Map<String, List<CsOffer>> offersByRequest,
                                         List<CsReservation> reservations,
                                         String addressee,
                                         Reservation reservation) {
        logger.info("Handling reservation");
        String requestId = reservation.getRequest();
        String offerId = reservation.getOffer();

        OptionalInt rIndex = Data.indexOf(requests, requestId, r -> r.id);
        if (!rIndex.isPresent()) {
            logger.info("No request with id: " + requestId);
            return;
        }
        List<CsOffer> offers = offersByRequest.get(requestId);

        OptionalInt oIndex = Data.indexOf(offers, offerId, o -> o.id);
        if (!oIndex.isPresent()) {
            logger.info("No offer with id: " + offerId);
            return;
        }

        CsOffer offer = offers.get(oIndex.getAsInt());

        CsReservation csReservation = new CsReservation();
        csReservation.offerId = reservation.getOffer();
        csReservation.requestId = requestId;
        csReservation.price = offer.price;
        csReservation.status = CsReservation.Status.OPEN;
        csReservation.payment = "";

        int hash = Objects.hash(new Random().nextDouble());
        csReservation.id = addressee + "-" + Objects.toString(hash).substring(0, 6);

        reservations.add(csReservation);
    }

    public static void handleReservationAction(List<CsReservation> reservations,
                                               String addressee,
                                               ReservationAction reservationAction) {
        Reservation reservation = reservationAction.getReservation();

        if (Action.PAY != reservationAction.getAction())
            throw new IllegalArgumentException("invalid action [PAY] for reservation: " + reservation.getId());

        logger.info("Handling action for reservation: " + reservation.getId());

        OptionalInt rIndex = Data.indexOf(reservations, reservation.getId(), r -> r.id);
        if (!rIndex.isPresent()) {
            logger.info("No reservation with id: " + reservation.getId());
            return;
        }

        CsReservation csReservation = reservations.get(rIndex.getAsInt());
        csReservation.status = CsReservation.Status.PAID;
        csReservation.payment = reservationAction.getArgument();
    }

}
