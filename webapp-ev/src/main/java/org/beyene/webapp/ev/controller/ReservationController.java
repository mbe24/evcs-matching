package org.beyene.webapp.ev.controller;

import org.beyene.protocol.api.EvApi;
import org.beyene.protocol.common.dto.EvReservation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin
@RequestMapping(path = "api/v1/reservations")
@ResponseBody
@RestController
public class ReservationController {

    @Autowired
    private EvApi evApi;

    private final List<EvReservation> preReservations = new ArrayList<>();

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public EvReservation updateReservation(@PathVariable("id") String id, @RequestParam(value = "payment") String option) {
        return evApi.updateReservation(id, option);
    }

    @GetMapping(value = "/load", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public List<EvReservation> getReservations(@RequestParam(value = "lastId") String lastId) {
        return evApi.getReservations(lastId);
    }

    @PostMapping(
            value = "/create/offer/{id}",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void makeReservation(@PathVariable("id") String offerId, @RequestBody String requestId) {
        evApi.makeReservation(offerId, requestId);
    }
}
