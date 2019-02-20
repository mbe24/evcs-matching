package org.beyene.webapp.cs.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beyene.webapp.common.dto.EvReservation;
import org.beyene.webapp.common.dto.EvReservation.Operation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@CrossOrigin
@RequestMapping(path = "api/v1/reservations")
@ResponseBody
@RestController
public class ReservationController {

    private static final Log logger = LogFactory.getLog(ReservationController.class);

    private final List<EvReservation> reservations = new ArrayList<>();
    private long id = 0;

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public void updateReservation(@PathVariable("id") long id, @RequestParam(value = "op") Operation op) {
        logger.info("Update: " + id + ", op=" + op);

        Optional<EvReservation> optional = reservations.stream().filter(r -> r.id == id).findFirst();
        if (!optional.isPresent())
            return;

        EvReservation reservation = optional.get();
        switch (op) {
            case ACCEPT:
                reservation.status = EvReservation.Status.ACCEPTED;
                break;
            case REJECT:
                reservation.status = EvReservation.Status.REJECTED;
                break;
            default:
                break;
        }
    }

    @GetMapping(value = "/load", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public List<EvReservation> getReservations(@RequestParam(value = "lastId") long lastId) {
        EvReservation reservation = new EvReservation();
        reservation.id = id++;
        reservation.requestId = 20277;
        reservation.offerId = 22443;
        reservation.price = 55.45;
        reservation.payment = "PayPal";
        reservation.status = EvReservation.Status.OPEN;

        //EvReservation.Status[] status = EvReservation.Status.values();
        //reservation.status = status[new Random().nextInt(status.length)];

        reservations.add(reservation);
        return reservations;
    }
}
