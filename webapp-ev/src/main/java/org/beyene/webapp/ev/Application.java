package org.beyene.webapp.ev;

import org.beyene.protocol.api.ApiConfiguration;
import org.beyene.protocol.api.ApiProvider;
import org.beyene.protocol.api.EvApi;
import org.beyene.protocol.ledger.ev.IotaEvApiProvider;
import org.beyene.protocol.ledger.ev.IotaEvOptions;
import org.beyene.protocol.tcp.ev.ZmqEvApiProvider;
import org.beyene.protocol.tcp.ev.ZmqEvOptions;
import org.beyene.webapp.ev.stub.StubEvProvider;
import org.beyene.webapp.ev.stub.StubOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.GenericApplicationContext;
import picocli.CommandLine;

import java.util.Arrays;
import java.util.List;

@ComponentScan("org.beyene.webapp.common.controller")
@SpringBootApplication
public class Application {

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        CommandLine cmd = new CommandLine(options);
        cmd.setUsageHelpWidth(120);

        try {
            List<CommandLine> parsed = cmd.parse(args);
            if (parsed.size() != 2) {
                System.err.println("Please specify one subcommand!");
                cmd.usage(System.out);
                return;
            } else if (cmd.isUsageHelpRequested()) {
                cmd.usage(System.out);
                return;
            } else if (cmd.isVersionHelpRequested()) {
                cmd.printVersionHelp(System.out);
                return;
            }

            Object subOptions = parsed.get(1).getCommand();
            if (ZmqEvOptions.class.isInstance(subOptions))
                new Application().runWith(new ZmqEvApiProvider(), ZmqEvOptions.class.cast(subOptions));
            else if (IotaEvOptions.class.isInstance(subOptions))
                new Application().runWith(new IotaEvApiProvider(), IotaEvOptions.class.cast(subOptions));
            else if (StubOptions.class.isInstance(subOptions)) {
                new Application().runWith(new StubEvProvider(), StubOptions.class.cast(subOptions));
            }
            else
                throw new IllegalStateException("subcommand is not supported: " + Arrays.toString(args));

        } catch (CommandLine.ParameterException ex) {
            System.err.println(ex.getMessage());
            if (!CommandLine.UnmatchedArgumentException.printSuggestions(ex, System.err)) {
                ex.getCommandLine().usage(System.err);
            }
        }
    }

    private <A extends EvApi, T extends ApiConfiguration<? extends A, T>> void runWith(
            ApiProvider<A, T> provider, T options) {
        SpringApplication application = new SpringApplication(Application.class);
        registerBeans(application, provider.newApi(options));
        ConfigurableApplicationContext context = application.run();
    }

    private void registerBeans(SpringApplication application, EvApi api) {
        application.addInitializers((GenericApplicationContext ctx) -> ctx.registerBean(EvApi.class, () -> api));
    }

}
