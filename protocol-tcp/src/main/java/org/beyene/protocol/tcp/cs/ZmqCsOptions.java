package org.beyene.protocol.tcp.cs;

import org.beyene.protocol.api.ApiConfiguration;
import org.beyene.protocol.api.CsApi;
import org.beyene.protocol.common.util.CsOptions;
import org.beyene.protocol.common.util.EndpointConverter;
import picocli.CommandLine;

@CommandLine.Command(name = "zmq", description = "Use ZMQ CS backend.")
public class ZmqCsOptions extends CsOptions implements ApiConfiguration<CsApi, ZmqCsOptions> {

    @CommandLine.Option(names = {"-e", "--endpoint"}, required = true, arity = "1", description = "Define ZMQ endpoint. " +
            "Only the tcp protocol is supported.", converter = EndpointConverter.class)
    public String endpoint;

}
