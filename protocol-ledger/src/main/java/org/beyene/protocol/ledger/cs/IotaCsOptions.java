package org.beyene.protocol.ledger.cs;

import org.beyene.protocol.api.ApiConfiguration;
import org.beyene.protocol.common.cmd.CsBaseOptions;
import org.beyene.protocol.common.cmd.EndpointConverter;
import picocli.CommandLine;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

@CommandLine.Command(name = "iota", description = "Use IOTA CS backend.")
public class IotaCsOptions extends CsBaseOptions implements ApiConfiguration<IotaCsApi, IotaCsOptions> {

    @CommandLine.Option(names = {"-e", "--endpoint"}, required = true, arity = "1", description = "Define ZMQ endpoint. " +
            "Only the tcp protocol is supported", converter = EndpointConverter.class)
    public String endpoint;

    @CommandLine.Option(names = {"-n", "--name"}, required = false, arity = "1", description = "Define ZMQ endpoint name")
    public String name = String.format("CS-%04d", new Random().nextInt(10_000));

    @CommandLine.Option(names = {"-i", "--iota-node"}, required = true, description = "Define IOTA node")
    public URL node;

    @CommandLine.Option(names = {"-t", "--tag"}, required = true, description = "Set application tag")
    public String tag;

    public IotaCsOptions() {
        try {
            this.node = new URL("http", "nodes.devnet.thetangle.org", 443, "");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

}
