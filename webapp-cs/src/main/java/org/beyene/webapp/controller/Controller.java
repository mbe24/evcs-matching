package org.beyene.webapp.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beyene.webapp.ev.dto.dto.Time;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.Objects;

@CrossOrigin
@RequestMapping(path = "api/v1/")
@ResponseBody
@RestController
public class Controller {

    private static final Log logger = LogFactory.getLog(Controller.class);

    @GetMapping(value = "/hello", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getHello(@RequestParam(value = "name") String name) {
        logger.info("/hello with: " + name);
        return String.format("Hello %s!", Objects.toString(name));
    }

    @GetMapping(value = "/test", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getTest(@RequestParam(value = "name") String name) {

        if ("THROW".equalsIgnoreCase(name))
            throw new IllegalArgumentException("THROW is not a valid argument!");

        return String.format("Hello %s!", Objects.toString(name));
    }

    @GetMapping(value = "/time", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Time getTime() {
        LocalTime now = LocalTime.now();
        String hour = String.format("%02d", now.getHour());
        String min = String.format("%02d", now.getMinute());
        String s = String.format("%02d", now.getSecond());
        return new Time(hour, min, s);
    }

}
