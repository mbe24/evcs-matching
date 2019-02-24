package org.beyene.webapp.common.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

@RestController
public class ShutdownController {

    private static final Log logger = LogFactory.getLog(ShutdownController.class);

    @Autowired
    private ApplicationContext context;

    @PostConstruct
    void init() throws Exception {
        logger.info("Shutdown controller initialized.");
    }

    @PostMapping("/shutdown")
    @ResponseBody
    public String shutdownContext() {
        ((ConfigurableApplicationContext) context).close();
        return "Shutting down...";
    }
}
