package org.beyene.webapp.common.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ShutdownController {

    @Autowired
    private ApplicationContext context;

    @PostMapping("/shutdown")
    @ResponseBody
    public String shutdownContext() {
        ((ConfigurableApplicationContext) context).close();
        return "Shutting down...";
    }
}
