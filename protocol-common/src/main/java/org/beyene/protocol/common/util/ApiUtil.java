package org.beyene.protocol.common.util;

import com.google.protobuf.Timestamp;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beyene.protocol.api.data.CsOffer;
import org.beyene.protocol.api.data.CsReservation;
import org.beyene.protocol.api.data.EvRequest;
import org.beyene.protocol.common.dto.*;
import org.beyene.protocol.common.io.MessageHandler;
import org.beyene.protocol.common.io.MetaMessage;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.beyene.protocol.common.util.Data.getNext;

public final class ApiUtil {

    private static final Log logger = LogFactory.getLog(ApiUtil.class);

    private ApiUtil() {
        throw new AssertionError("no instances allowed");
    }

    public static List<EvRequest> getRequests(List<EvRequest> requests, String lastId) {
        logger.info("Get requests, lastId: " + lastId);

        List<EvRequest> result;
        if (Objects.isNull(lastId) || lastId.isEmpty() || lastId.equals("-1")) {
            result = requests;
        } else {
            result = Data.getNext(requests, lastId, request -> request.id);
        }

        return result;
    }

    public static CsOffer submitOffer(Map<String, List<CsOffer>> offersByRequest,
                                      String name,
                                      MessageHandler handler,
                                      String offerSource,
                                      Map<String, String> targetsByRequestId,
                                      String requestId,
                                      CsOffer offer) {
        // in order to alter hash
        offer.time = offer.time.plusNanos(new Random().nextInt(250));
        int hash = Objects.hash(offer.time, offer.price, offer.energy, offer.window, offer.date);

        // encode name in id
        offer.id = name + "-" + Objects.toString(Math.abs(hash)).substring(0, 6);
        logger.info("New offer: " + offer);

        List<CsOffer> offers = offersByRequest.get(requestId);
        if (Objects.isNull(offers))
            offersByRequest.put(requestId, offers = new CopyOnWriteArrayList<>());
        offers.add(offer);

        LocalDateTime dateTime = LocalDateTime.of(offer.date, offer.time);
        ZoneId systemZone = ZoneId.systemDefault();
        ZoneOffset offset = systemZone.getRules().getOffset(dateTime);
        Instant instant = dateTime.toInstant(offset);
        Timestamp ts = Timestamp.newBuilder().setSeconds(instant.getEpochSecond()).setNanos(instant.getNano()).build();
        Offer offer1 = Offer.newBuilder()
                .setSource(offerSource)
                .setRequestId(requestId)
                .setId(offer.id)
                .setEnergy(offer.energy)
                .setPrice(offer.price)
                .setDate(ts)
                .setWindow(offer.window)
                .build();

        Message message = Message.newBuilder().setOffer(offer1).build();

        String address = targetsByRequestId.get(requestId);
        handler.handle(new MetaMessage(address, message));

        return offer;
    }

    public static List<CsOffer> getOffers(Map<String, List<CsOffer>> offersByRequest, String requestId, String lastId) {
        logger.info("Offers for requestId=" + requestId + ", lastId=" + lastId);

        List<CsOffer> offers = offersByRequest.get(requestId);

        if (Objects.isNull(offers))
            offersByRequest.put(requestId, (offers = new CopyOnWriteArrayList<>()));

        List<CsOffer> next = getNext(offers, lastId, offer -> offer.id);
        logger.info("Returned offers: " + next.size());
        return next;
    }

    public static List<CsReservation> getReservations(List<CsReservation> reservations, String lastId) {
        logger.info("Get reservations, lastId: " + lastId);

        List<CsReservation> result;
        if (Objects.isNull(lastId) || lastId.isEmpty() || lastId.equals("-1")) {
            result = reservations;
        } else {
            result = Data.getNext(reservations, lastId, reservation -> reservation.id);
        }

        return result;
    }

    public static void updateReservation(List<CsReservation> reservations,
                                         Map<String, String> addressesByRequest,
                                         MessageHandler handler,
                                         List<String> paymentOptions,
                                         String id,
                                         CsReservation.Operation op) {
        logger.info("Update reservation: " + id + ". Operation: " + op);

        OptionalInt rIndex = Data.indexOf(reservations, id, r -> r.id);
        if (!rIndex.isPresent()) {
            logger.info("No reservation with id: " + id);
            return;
        }
        CsReservation reservation = reservations.get(rIndex.getAsInt());

        Reservation res = Reservation.newBuilder()
                .setId(reservation.id)
                .setOffer(reservation.offerId)
                .setRequest(reservation.requestId)
                .build();

        ReservationAction reservationAction = ReservationAction.newBuilder()
                .setReservation(res)
                .setAction(op == CsReservation.Operation.ACCEPT ? Action.ACCEPT : Action.REJECT)
                .build();

        String address = addressesByRequest.get(reservation.requestId);
        Message message = Message.newBuilder().setReservationAction(reservationAction).build();
        handler.handle(new MetaMessage(address, message));

        if (CsReservation.Operation.ACCEPT == op) {
            reservation.status = CsReservation.Status.ACCEPTED;

            ReservationPaymentOption paymentOption = ReservationPaymentOption.newBuilder()
                    .setReservation(res).addAllOptions(paymentOptions).build();
            Message m = Message.newBuilder().setPaymentOptions(paymentOption).build();
            handler.handle(new MetaMessage(address, m));
        } else {
            reservation.status = CsReservation.Status.REJECTED;
        }
    }
}
