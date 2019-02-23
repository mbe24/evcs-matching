package org.beyene.webapp.ev.stub;

import org.beyene.protocol.api.ApiConfiguration;
import org.beyene.protocol.api.EvApi;
import picocli.CommandLine;

@CommandLine.Command(name = "stub", description = "Use stub EV backend.")
public class StubOptions implements ApiConfiguration<EvApi, StubOptions> {
}
