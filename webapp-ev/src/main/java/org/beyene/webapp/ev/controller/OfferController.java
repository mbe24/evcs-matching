package org.beyene.webapp.ev.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beyene.protocol.api.EvProtocol;
import org.beyene.protocol.common.dto.CsOffer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RequestMapping(path = "api/v1/offers")
@ResponseBody
@RestController
public class OfferController {

    private static final Log logger = LogFactory.getLog(OfferController.class);

    @Autowired
    private EvProtocol evProtocol;

    @GetMapping(
            value = "/r/{id}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<CsOffer> getOffers(@PathVariable(value = "id") String requestId, @RequestParam(value = "lastId") String lastId) {
        return evProtocol.getOffers(requestId, lastId);
    }
}
