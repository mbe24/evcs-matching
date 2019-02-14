package org.beyene.webapp.controller;

import org.beyene.webapp.common.dto.EvRequest;
import org.beyene.webapp.ev.dto.dto.Time;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@CrossOrigin
@RequestMapping(path = "api/v1/requests")
@ResponseBody
@RestController
public class RequestsController {

    @GetMapping(value = "/load", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public List<EvRequest> getRequests(@RequestParam(value = "lastId") long lastId) {
        EvRequest request = new EvRequest();
        request.id = ++lastId;
        request.energy = 45.56;
        request.date = LocalDate.now();
        request.time = LocalTime.now();
        request.window = 250;

        List<EvRequest> requests = new ArrayList<>();
        requests.add(request);
        return requests;
    }
}
