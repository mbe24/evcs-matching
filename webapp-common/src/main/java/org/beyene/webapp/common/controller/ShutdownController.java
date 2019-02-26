package org.beyene.webapp.common.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.Timer;
import java.util.TimerTask;

@CrossOrigin
@ResponseBody
@RestController
public class ShutdownController {

    private static final Log logger = LogFactory.getLog(ShutdownController.class);

    @Autowired
    private ApplicationContext context;

    @PostConstruct
    void init() throws Exception {
        logger.info("Shutdown controller initialized");
    }

    @PostMapping("/shutdown")
    public String shutdownContext() {
        logger.info("Shutting down application");
        new Timer().schedule(wrap(() -> ((ConfigurableApplicationContext) context).close()), 250);
        return String.format("Shutting down...%n");
    }

    private static TimerTask wrap(Runnable r) {
        return new TimerTask() {
            @Override
            public void run() {
                r.run();
            }
        };
    }
}
