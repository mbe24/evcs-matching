package org.beyene.webapp.common.util;

import org.beyene.protocol.api.ApiConfiguration;
import org.beyene.protocol.api.ApiProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

public class GeneralizedApiRunner<API> {

    private final Class<?> application;
    private final Class<API> apiType;

    public GeneralizedApiRunner(Class<?> application, Class<API> apiType) {
        this.application = application;
        this.apiType = apiType;
    }

    public <A extends API, T extends ApiConfiguration<? extends A, T>> void runWith(
            ApiProvider<A, T> provider, T options) {
        SpringApplication application = new SpringApplication(this.application);
        registerBeans(application, provider.newApi(options));
        ConfigurableApplicationContext context = application.run();
    }

    private <A extends API>void registerBeans(SpringApplication application, A api) {
        application.addInitializers((GenericApplicationContext ctx) -> ctx.registerBean(this.apiType, () -> api));
    }

}
