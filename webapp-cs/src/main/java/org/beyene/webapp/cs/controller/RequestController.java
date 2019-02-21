package org.beyene.webapp.cs.controller;

import org.beyene.protocol.api.CsProtocol;
import org.beyene.protocol.common.dto.EvRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RequestMapping(path = "api/v1/requests")
@ResponseBody
@RestController
public class RequestController {

    @Autowired
    private CsProtocol csProtocol;

    @GetMapping(value = "/load", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public List<EvRequest> getRequests(@RequestParam(value = "lastId") String lastId) {
        return csProtocol.getRequests(lastId);
    }
}
