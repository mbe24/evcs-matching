package org.beyene.webapp.cs;

import org.beyene.protocol.ledger.cs.IotaCsOptions;
import org.beyene.protocol.tcp.cs.ZmqCsOptions;
import org.beyene.webapp.cs.stub.StubOptions;
import picocli.CommandLine;

import java.io.File;

@CommandLine.Command(
        name = "java -jar webapp-cs.jar",
        mixinStandardHelpOptions = true,
        version = "webapp-cs 1.0-SNAPSHOT",
        subcommands = {ZmqCsOptions.class, IotaCsOptions.class, StubOptions.class}
)
public class Options {

    @CommandLine.Option(names = {"-log", "--log-directory"}, required = false, description = "Logging directory")
    public File logDir;

}
