package org.beyene.webapp.ev.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beyene.webapp.common.dto.EvPreReservation;
import org.beyene.webapp.common.dto.EvReservation;
import org.beyene.webapp.common.dto.EvReservation.Status;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin
@RequestMapping(path = "api/v1/reservations")
@ResponseBody
@RestController
public class ReservationController {

    private static final Log logger = LogFactory.getLog(ReservationController.class);

    private final List<EvPreReservation> preReservations = new ArrayList<>();

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public EvPreReservation updateReservation(@PathVariable("id") long id, @RequestParam(value = "payment") String option) {
        logger.info("Reservation: " + id + ", payment=" + option);

        Optional<EvPreReservation> optional = preReservations.stream().filter(r -> r.id == id).findFirst();
        if (!optional.isPresent())
            return new EvPreReservation();

        EvPreReservation reservation = optional.get();
        reservation.status = Status.PAID;
        reservation.payment = option;
        return reservation;
    }

    @GetMapping(value = "/load", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public List<EvPreReservation> getReservations(@RequestParam(value = "lastId") long lastId) {
        EvPreReservation reservation = new EvPreReservation();
        reservation.id = ++lastId;
        reservation.requestId = 20277;
        reservation.offerId = 22443;
        reservation.price = 55.45;
        reservation.paymentOptions = Arrays.asList("PayPal", "BTC", "LTC", "XRP");
        reservation.status = new Random().nextInt(10) > 4 ? Status.ACCEPTED : Status.REJECTED;

        //EvReservation.Status[] status = EvReservation.Status.values();
        //reservation.status = status[new Random().nextInt(status.length)];

        preReservations.add(reservation);
        return Arrays.asList(reservation);
    }

    @PostMapping(
            value = "/create/offer/{id}",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void createReservation(@PathVariable("id") long  offerId, @RequestBody long requestId) {


        logger.info("Reservation for offerId=" + offerId + ", requestId=" + requestId);
    }
}
