package org.beyene.webapp.ev;

import org.beyene.protocol.ledger.ev.IotaEvOptions;
import org.beyene.protocol.tcp.ev.ZmqEvOptions;
import org.beyene.webapp.ev.stub.StubOptions;
import picocli.CommandLine;

import java.io.File;

@CommandLine.Command(
        name = "java -jar webapp-ev.jar",
        mixinStandardHelpOptions = true,
        version = "webapp-ev 1.0-SNAPSHOT",
        subcommands = {ZmqEvOptions.class, IotaEvOptions.class, StubOptions.class}
)
public class Options {

    @CommandLine.Option(names = {"-log", "--log-directory"}, required = false, description = "Logging directory")
    public File logDir;

}
