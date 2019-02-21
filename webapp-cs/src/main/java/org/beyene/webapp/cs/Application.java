package org.beyene.webapp.cs;

import org.beyene.protocol.api.CsApi;
import org.beyene.protocol.tcp.CsApiComponent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import picocli.CommandLine;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.UnmatchedArgumentException;

@SpringBootApplication
public class Application {

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        CommandLine cmd = new CommandLine(options);
        cmd.setUsageHelpWidth(120);

        try {
            cmd.parse(args);
            if (cmd.isUsageHelpRequested()) {
                cmd.usage(System.out);
                return;
            } else if (cmd.isVersionHelpRequested()) {
                cmd.printVersionHelp(System.out);
                return;
            }

            new Application().runWith(options);
        } catch (ParameterException ex) {
            System.err.println(ex.getMessage());
            if (!UnmatchedArgumentException.printSuggestions(ex, System.err)) {
                ex.getCommandLine().usage(System.err);
            }
        }
    }

    public void runWith(Options options) throws Exception {
        SpringApplication application = new SpringApplication(Application.class);
        registerBeans(application, options);
        ConfigurableApplicationContext context = application.run();
    }

    private void registerBeans(SpringApplication application, Options options) {
        CsApi api = new CsApiComponent();
        application.addInitializers((GenericApplicationContext ctx) -> ctx.registerBean(CsApi.class, () -> api));
    }

}
