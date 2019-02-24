package org.beyene.protocol.tcp.cs;

import org.beyene.protocol.api.ApiConfiguration;
import org.beyene.protocol.api.CsApi;
import org.beyene.protocol.common.cmd.CsBaseOptions;
import org.beyene.protocol.common.cmd.EndpointConverter;
import picocli.CommandLine;

import java.util.Random;

@CommandLine.Command(name = "zmq", description = "Use ZMQ CS backend.")
public class ZmqCsOptions extends CsBaseOptions implements ApiConfiguration<CsApi, ZmqCsOptions> {

    @CommandLine.Option(names = {"-e", "--endpoint"}, required = true, arity = "1", description = "Define ZMQ endpoint. " +
            "Only the tcp protocol is supported.", converter = EndpointConverter.class)
    public String endpoint;

    @CommandLine.Option(names = {"-n", "--name"}, required = false, arity = "1", description = "Define ZMQ endpoint name")
    public String name = String.format("CS-%04d", new Random().nextInt(10_000));

}
