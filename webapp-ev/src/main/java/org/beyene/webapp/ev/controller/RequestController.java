package org.beyene.webapp.ev.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beyene.protocol.api.EvApi;
import org.beyene.protocol.api.data.EvRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RequestMapping(path = "app/api/v1/requests")
@ResponseBody
@RestController
public class RequestController {

    private static final Log logger = LogFactory.getLog(RequestController.class);

    @Autowired
    private EvApi evApi;

    @GetMapping(value = "/load", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public List<EvRequest> getRequests(@RequestParam(value = "lastId") String lastId) {
        return evApi.getRequests(lastId);
    }

    @PostMapping(
            value = "/create",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public EvRequest submitRequest(@RequestBody EvRequest request) {
        return evApi.submitRequest(request);
    }
}
