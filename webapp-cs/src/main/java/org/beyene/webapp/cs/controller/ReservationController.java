package org.beyene.webapp.cs.controller;

import org.beyene.protocol.api.CsProtocol;
import org.beyene.protocol.common.dto.CsReservation;
import org.beyene.protocol.common.dto.CsReservation.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RequestMapping(path = "api/v1/reservations")
@ResponseBody
@RestController
public class ReservationController {

    @Autowired
    private CsProtocol csProtocol;

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public void updateReservation(@PathVariable("id") String id, @RequestParam(value = "op") Operation op) {
        csProtocol.updateReservation(id, op);
    }

    @GetMapping(value = "/load", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public List<CsReservation> getReservations(@RequestParam(value = "lastId") String lastId) {
        return csProtocol.getReservations(lastId);
    }
}
