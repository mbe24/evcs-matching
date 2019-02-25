package org.beyene.webapp.cs;

import org.beyene.protocol.api.CsApi;
import org.beyene.protocol.ledger.cs.IotaCsApiProvider;
import org.beyene.protocol.ledger.cs.IotaCsOptions;
import org.beyene.protocol.tcp.cs.ZmqCsApiProvider;
import org.beyene.protocol.tcp.cs.ZmqCsOptions;
import org.beyene.webapp.common.util.GeneralizedApiRunner;
import org.beyene.webapp.cs.stub.StubApiProvider;
import org.beyene.webapp.cs.stub.StubOptions;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import picocli.CommandLine;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@ComponentScan({"org.beyene.webapp.common.controller", "org.beyene.webapp.cs.controller"})
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

            if (Objects.nonNull(options.logDir)) {
                System.setProperty("LOG_DIR", options.logDir.getAbsolutePath());
                System.setProperty("LOG_FILE", "TRUE");
            }

            GeneralizedApiRunner<CsApi> runner = new GeneralizedApiRunner<>(Application.class, CsApi.class);

            Object subOptions = parsed.get(1).getCommand();
            if (ZmqCsOptions.class.isInstance(subOptions))
                runner.runWith(new ZmqCsApiProvider(), ZmqCsOptions.class.cast(subOptions));
            else if (IotaCsOptions.class.isInstance(subOptions))
                runner.runWith(new IotaCsApiProvider(), IotaCsOptions.class.cast(subOptions));
            else if (StubOptions.class.isInstance(subOptions)) {
                runner.runWith(new StubApiProvider(), StubOptions.class.cast(subOptions));
            } else
                throw new IllegalStateException("subcommand is not supported: " + Arrays.toString(args));

        } catch (CommandLine.ParameterException ex) {
            System.err.println(ex.getMessage());
            if (!CommandLine.UnmatchedArgumentException.printSuggestions(ex, System.err)) {
                ex.getCommandLine().usage(System.err);
            }
        }
    }

}
