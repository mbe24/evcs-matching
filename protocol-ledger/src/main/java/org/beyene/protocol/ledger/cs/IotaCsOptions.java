package org.beyene.protocol.ledger.cs;

import org.beyene.protocol.api.ApiConfiguration;
import org.beyene.protocol.common.cmd.CsBaseOptions;
import picocli.CommandLine;

@CommandLine.Command(name = "iota", description = "Use IOTA CS backend.")
public class IotaCsOptions extends CsBaseOptions implements ApiConfiguration<IotaCsApi, IotaCsOptions> {
}
