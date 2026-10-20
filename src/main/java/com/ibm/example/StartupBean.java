package com.ibm.example;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

/**
 * Created eagerly at application start. Blocks for 5 seconds in {@link #init()},
 * then logs that it is done.
 */
@Startup
@Singleton
public class StartupBean {

    private static final Logger LOG = Logger.getLogger(StartupBean.class.getName());

    @PostConstruct
    void init() {
        LOG.info("StartupBean waiting 5 seconds...");
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.warning("StartupBean wait interrupted");
            return;
        }
        LOG.info("StartupBean is done.");
    }
}
