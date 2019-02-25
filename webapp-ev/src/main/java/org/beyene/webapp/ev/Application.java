package org.beyene.webapp.ev;

import org.beyene.protocol.api.EvApi;
import org.beyene.protocol.ledger.ev.IotaEvApiProvider;
import org.beyene.protocol.ledger.ev.IotaEvOptions;
import org.beyene.protocol.tcp.ev.ZmqEvApiProvider;
import org.beyene.protocol.tcp.ev.ZmqEvOptions;
import org.beyene.webapp.common.util.GeneralizedApiRunner;
import org.beyene.webapp.ev.stub.StubEvProvider;
import org.beyene.webapp.ev.stub.StubOptions;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import picocli.CommandLine;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@ComponentScan({"org.beyene.webapp.common.controller", "org.beyene.webapp.ev.controller"})
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

            GeneralizedApiRunner<EvApi> runner = new GeneralizedApiRunner<>(Application.class, EvApi.class);

            Object subOptions = parsed.get(1).getCommand();
            if (ZmqEvOptions.class.isInstance(subOptions))
                runner.runWith(new ZmqEvApiProvider(), ZmqEvOptions.class.cast(subOptions));
            else if (IotaEvOptions.class.isInstance(subOptions))
                runner.runWith(new IotaEvApiProvider(), IotaEvOptions.class.cast(subOptions));
            else if (StubOptions.class.isInstance(subOptions)) {
                runner.runWith(new StubEvProvider(), StubOptions.class.cast(subOptions));
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

}
