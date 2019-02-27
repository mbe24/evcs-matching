package org.beyene.protocol.ledger.ev;

import org.beyene.protocol.api.ApiConfiguration;
import org.beyene.protocol.common.cmd.EndpointConverter;
import org.beyene.protocol.ledger.util.TagConverter;
import picocli.CommandLine;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Random;

@CommandLine.Command(name = "iota", description = "Use IOTA EV backend.")
public class IotaEvOptions implements ApiConfiguration<IotaEvApi, IotaEvOptions> {

    @CommandLine.Option(names = {"-e", "--endpoint"}, required = true, arity = "1", split = ",", description = "Define ZMQ endpoints. " +
            "Only the tcp protocol is supported", converter = EndpointConverter.class)
    public List<String> endpoints;

    @CommandLine.Option(names = {"-n", "--name"}, required = false, arity = "1", description = "Define ZMQ endpoint name")
    public String name = String.format("EV-%04d", new Random().nextInt(10_000));

    @CommandLine.Option(names = {"-i", "--iota-node"}, description = "Define IOTA node")
    public URL node;

    @CommandLine.Option(names = {"-t", "--tag"}, required = true,
            description = "Set application tag", converter = TagConverter.class)
    public String tag;

    @CommandLine.Option(names = {"-a", "--all-txs"}, required = false,
            description = "Handle past transactions")
    public boolean allTxs = false;

    public IotaEvOptions() {
        try {
            this.node = new URL("https", "nodes.devnet.thetangle.org", 443, "");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
