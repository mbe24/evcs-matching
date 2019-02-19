package org.beyene.webapp.ev.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beyene.webapp.common.dto.EvRequest;
import org.springframework.core.style.ToStringCreator;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RequestMapping(path = "api/v1/request")
@ResponseBody
@RestController
public class RequestFormController {

    private static final Log logger = LogFactory.getLog(RequestFormController.class);

    @PostMapping(
            value = "/create",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void doRequest(@RequestBody EvRequest request) {
        String s = new ToStringCreator(request)
                .append("energy", request.energy)
                .append("date", request.date)
                .append("time", request.time)
                .append("window", request.window)
                .toString();

        logger.info("New request: " + s);
    }
}
