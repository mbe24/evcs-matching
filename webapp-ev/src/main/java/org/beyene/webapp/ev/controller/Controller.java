package org.beyene.webapp.ev.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beyene.webapp.common.dto.Message;
import org.beyene.webapp.common.dto.EvRequest;
import org.beyene.webapp.ev.dto.Time;
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

    // test with
    //curl -H "Content-Type: application/json" \
    //        > -d '{ "message": "This is how we do"}' \
    //        > http://localhost:8080/app/api/v1/message
    @PostMapping(
            value = "/message",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void sendMessage(@RequestBody Message message) {
        logger.info("New message: " + message.message);
    }

    @PostMapping(
            value = "/message2",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Message sendMessage2(@RequestBody Message message) {
        logger.info("New message: " + message.message);
        logger.info("Issuing response");

        if ("THROW".equalsIgnoreCase(message.message))
            throw new IllegalArgumentException("THROW is not a valid argument!");

        return new Message(message.message);
    }

    @PostMapping(
            value = "/request",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void doRequest(@RequestBody EvRequest request) {
        logger.info("New message: " + request);
    }

    @GetMapping(value = "/hello", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getHello(@RequestParam(value = "name") String name) {
        logger.info("/hello with: " + name);
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
