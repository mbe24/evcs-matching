package org.beyene.webapp.cs.stub;

import org.beyene.protocol.api.ApiConfiguration;
import org.beyene.protocol.api.CsApi;
import picocli.CommandLine;

@CommandLine.Command(name = "stub", description = "Use stub CS backend.")
public class StubOptions implements ApiConfiguration<CsApi, StubOptions> {
}
