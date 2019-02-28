package org.beyene.protocol.common.util;

import com.google.protobuf.Timestamp;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beyene.protocol.api.data.CsOffer;
import org.beyene.protocol.api.data.CsReservation;
import org.beyene.protocol.api.data.EvRequest;
import org.beyene.protocol.api.data.EvReservation;
import org.beyene.protocol.common.dto.*;
import org.beyene.protocol.common.io.MessageHandler;
import org.beyene.protocol.common.io.MetaMessage;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import static org.beyene.protocol.common.util.Data.getNext;
import static org.beyene.protocol.common.util.Data.indexOf;

public final class ApiUtil {

    private static final Log logger = LogFactory.getLog(ApiUtil.class);

    private ApiUtil() {
        throw new AssertionError("no instances allowed");
    }

    public static Message responseForSubmitRequest(List<EvRequest> requests,
                                                      String name,
                                                      EvRequest request,
                                                      String source) {
        // in order to alter hash
        request.time = request.time.plusNanos(new Random().nextInt(250));
        int hash = Objects.hash(request.time, request.energy, request.date, request.window);

        // encode name in id
        request.id = name + "-" + Objects.toString(Math.abs(hash)).substring(0, 6);
        logger.info("New request: " + request);

        requests.add(request);

        LocalDateTime dateTime = LocalDateTime.of(request.date, request.time);
        ZoneId systemZone = ZoneId.systemDefault();
        ZoneOffset offset = systemZone.getRules().getOffset(dateTime);
        Instant instant = dateTime.toInstant(offset);
        Timestamp ts = Timestamp.newBuilder().setSeconds(instant.getEpochSecond()).setNanos(instant.getNano()).build();

        Request r = Request.newBuilder()
                .setSource(source)
                .setId(request.id)
                .setEnergy(request.energy)
                .setDate(ts)
                .setWindow(request.window)
                .build();

        Message message = Message.newBuilder().setRequest(r).build();
        return message;
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

    public static List<CsOffer> getOffers(Map<String, List<CsOffer>> offersByRequest,
                                          String requestId,
                                          String lastId) {
        logger.info("Offers for requestId=" + requestId + ", lastId=" + lastId);

        List<CsOffer> offers = offersByRequest.get(requestId);

        if (Objects.isNull(offers))
            offersByRequest.put(requestId, (offers = new CopyOnWriteArrayList<>()));

        List<CsOffer> next = getNext(offers, lastId, offer -> offer.id);
        logger.info("Returned offers: " + next.size());
        return next;
    }

    public static void makeReservation(Map<String, List<CsOffer>> offersByRequest,
                                       Map<String, String> addressesByOffer,
                                       String name,
                                       MessageHandler handler,
                                       String offerId,
                                       String requestId) {
        logger.info("Reservation for offer: " + offerId);

        List<CsOffer> offers = offersByRequest.get(requestId);

        if (Objects.isNull(offers))
            offersByRequest.put(requestId, offers = new CopyOnWriteArrayList<>());

        OptionalInt oIndex = indexOf(offers, offerId, o -> o.id);
        if (!oIndex.isPresent()) {
            logger.info("No offer with id: " + offerId);
            throw new IllegalArgumentException("there is no offer with id: " + offerId);
        }

        CsOffer offer = offers.get(oIndex.getAsInt());
        offer.reserved = true;

        Reservation reservation = Reservation.newBuilder()
                .setSource(name)
                .setOffer(offerId)
                .setRequest(requestId).build();
        Message message = Message.newBuilder().setReservation(reservation).build();

        String address = addressesByOffer.get(offerId);
        handler.handle(new MetaMessage(address, message));
    }

    public static List<CsReservation> getCsReservations(List<CsReservation> reservations, String lastId) {
        return getReservations(reservations, lastId, reservation -> reservation.id);
    }

    public static List<EvReservation> getEvReservations(List<EvReservation> reservations, String lastId) {
        return getReservations(reservations, lastId, reservation -> reservation.id);
    }

    private static <T> List<T> getReservations(List<T> reservations, String lastId, Function<T, String> toKey) {
        logger.info("Get reservations, lastId: " + lastId);

        List<T> result;
        if (Objects.isNull(lastId) || lastId.isEmpty() || lastId.equals("-1")) {
            result = reservations;
        } else {
            result = Data.getNext(reservations, lastId, toKey);
        }

        return result;
    }

    public static EvReservation updateEvReservation(List<EvReservation> reservations,
                                                    Map<String, String> addressesByOffer,
                                                    MessageHandler handler,
                                                    String id,
                                                    String option) {
        logger.info("Paying for reservation: " + id + "[" + option + "]");

        OptionalInt rIndex = indexOf(reservations, id, r -> r.id);
        if (!rIndex.isPresent()) {
            logger.info("No reservation with id: " + id);
            throw new IllegalArgumentException("there is no reservation with id: " + id);
        }

        EvReservation r = reservations.get(rIndex.getAsInt());
        r.status = CsReservation.Status.PAID;
        r.payment = option;

        Reservation reservation = Reservation.newBuilder()
                .setId(r.id)
                .setOffer(r.offerId)
                .setRequest(r.requestId)
                .build();

        Message message = Message.newBuilder()
                .setReservationAction(ReservationAction.newBuilder()
                        .setReservation(reservation)
                        .setAction(Action.PAY)
                        .setArgument(option))
                .build();
        String address = addressesByOffer.get(r.offerId);
        handler.handle(new MetaMessage(address, message));

        return r;
    }

    public static void updateCsReservation(List<CsReservation> reservations,
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
