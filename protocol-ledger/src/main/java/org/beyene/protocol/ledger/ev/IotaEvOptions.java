package org.beyene.protocol.ledger.ev;

import org.beyene.protocol.api.ApiConfiguration;
import picocli.CommandLine;

@CommandLine.Command(name = "iota", description = "Use IOTA EV backend.")
public class IotaEvOptions implements ApiConfiguration<IotaEvApi, IotaEvOptions> {
}
