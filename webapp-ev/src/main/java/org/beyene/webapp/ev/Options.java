package org.beyene.webapp.ev;

import org.beyene.webapp.common.EndpointConverter;
import picocli.CommandLine;

import java.util.List;

@CommandLine.Command(name = "java -jar webapp-ev.jar", mixinStandardHelpOptions = true, version = "webapp-ev 1.0-SNAPSHOT")
public class Options {

    @CommandLine.Option(names = {"-e", "--endpoint"}, required = true, arity = "1", split = ",", description = "Define ZMQ endpoints. " +
            "Only the tcp protocol is supported.", converter = EndpointConverter.class)
    List<String> endpoints;

}
