package org.beyene.webapp.cs;

import org.beyene.webapp.common.EndpointConverter;
import picocli.CommandLine;

@CommandLine.Command(name = "java -jar webapp-cs.jar", mixinStandardHelpOptions = true, version = "webapp-cs 1.0-SNAPSHOT")
public class Options {

    @CommandLine.Option(names = {"-e", "--endpoint"}, required = true, arity = "1", description = "Define ZMQ endpoint. " +
            "Only the tcp protocol is supported.", converter = EndpointConverter.class)
    String endpoint;

}
