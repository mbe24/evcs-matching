package org.beyene.protocol.tcp.ev;

import org.beyene.protocol.api.ApiConfiguration;
import org.beyene.protocol.common.util.EndpointConverter;
import picocli.CommandLine;

import java.util.List;

@CommandLine.Command(name = "zmq", description = "Use ZMQ EV backend.")
public class ZmqEvOptions implements ApiConfiguration<ZmqEvApi, ZmqEvOptions> {

    @CommandLine.Option(names = {"-e", "--endpoint"}, required = true, arity = "1", split = ",", description = "Define ZMQ endpoints. " +
            "Only the tcp protocol is supported.", converter = EndpointConverter.class)
    public List<String> endpoints;

    @CommandLine.Option(names = {"-n", "--name"}, required = false, arity = "1", description = "Define ZMQ endpoint name")
    public String name = "EV";
}
