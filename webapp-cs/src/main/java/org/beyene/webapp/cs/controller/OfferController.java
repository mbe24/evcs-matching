package org.beyene.webapp.cs.controller;

import org.beyene.protocol.api.CsApi;
import org.beyene.protocol.common.dto.CsOffer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RequestMapping(path = "app/api/v1/offers")
@ResponseBody
@RestController
public class OfferController {

    @Autowired
    private CsApi csApi;

    @PostMapping(
            value = "/create/r/{id}",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void submitOffer(@PathVariable(value = "id") String requestId, @RequestBody CsOffer offer) {
        csApi.submitOffer(requestId, offer);
    }

    @GetMapping(
            value = "/r/{id}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<CsOffer> getOffers(@PathVariable(value = "id") String requestId, @RequestParam(value = "lastId") String lastId) {
        return csApi.getOffers(requestId, lastId);
    }
}
