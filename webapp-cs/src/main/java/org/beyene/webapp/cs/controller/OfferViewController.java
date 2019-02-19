package org.beyene.webapp.cs.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beyene.webapp.common.dto.CsOffer;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin
@RequestMapping(path = "api/v1/offers")
@ResponseBody
@RestController
public class OfferViewController {

    private static final Log logger = LogFactory.getLog(OfferFormController.class);

    @PostMapping(
            value = "/load",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<CsOffer> loadOffers(@RequestBody String requestId, @RequestParam(value = "lastId") long lastId) {
        logger.info("RequestId=" + requestId + ", lastId=" + lastId);

        CsOffer offer = new CsOffer();
        offer.id = ++lastId;
        offer.price = 22;
        offer.energy = 22.56;
        offer.date = LocalDate.now();
        offer.time = LocalTime.now();
        offer.window = 30;

        List<CsOffer> offers = new ArrayList<>();
        offers.add(offer);
        return offers;
    }
}