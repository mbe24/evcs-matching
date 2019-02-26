package org.beyene.webapp.cs.controller;

import org.beyene.protocol.api.CsApi;
import org.beyene.protocol.api.data.CsReservation;
import org.beyene.protocol.api.data.CsReservation.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RequestMapping(path = "app/api/v1/reservations")
@ResponseBody
@RestController
public class ReservationController {

    @Autowired
    private CsApi csApi;

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public void updateReservation(@PathVariable("id") String id, @RequestParam(value = "op") Operation op) {
        csApi.updateReservation(id, op);
    }

    @GetMapping(value = "/load", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public List<CsReservation> getReservations(@RequestParam(value = "lastId") String lastId) {
        return csApi.getReservations(lastId);
    }
}
