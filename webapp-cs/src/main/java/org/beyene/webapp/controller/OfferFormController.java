package org.beyene.webapp.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beyene.webapp.common.dto.EvOffer;
import org.springframework.core.style.ToStringCreator;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RequestMapping(path = "api/v1/offer")
@ResponseBody
@RestController
public class OfferFormController {

    private static final Log logger = LogFactory.getLog(OfferFormController.class);

    @PostMapping(
            value = "/create",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void doRequest(@RequestBody EvOffer offer) {
        String s = new ToStringCreator(offer)
                .append("price", offer.price)
                .append("energy", offer.energy)
                .append("date", offer.date)
                .append("time", offer.time)
                .append("window", offer.window)
                .toString();

        logger.info("New offer: " + s);
    }
}
