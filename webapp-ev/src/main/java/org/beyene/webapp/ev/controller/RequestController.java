package org.beyene.webapp.ev.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beyene.webapp.common.dto.EvRequest;
import org.springframework.core.style.ToStringCreator;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin
@RequestMapping(path = "api/v1/requests")
@ResponseBody
@RestController
public class RequestController {

    private static final Log logger = LogFactory.getLog(RequestController.class);

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

    @PostMapping(
            value = "/create",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void submitEvRequest(@RequestBody EvRequest request) {
        String s = new ToStringCreator(request)
                .append("energy", request.energy)
                .append("date", request.date)
                .append("time", request.time)
                .append("window", request.window)
                .toString();

        logger.info("New request: " + s);
    }
}
