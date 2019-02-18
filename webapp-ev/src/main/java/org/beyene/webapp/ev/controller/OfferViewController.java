package org.beyene.webapp.ev.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beyene.webapp.common.dto.CsOffer;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@CrossOrigin
@RequestMapping(path = "api/v1/offer")
@ResponseBody
@RestController
public class OfferViewController {

    private static final Log logger = LogFactory.getLog(OfferViewController.class);

    @GetMapping(value = "/time", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public List<CsOffer> getOffers(@RequestParam(value = "name") String name) {
        logger.info("Get offers: ");
        return Collections.emptyList();
    }
}
