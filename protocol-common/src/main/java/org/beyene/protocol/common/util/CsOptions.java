package org.beyene.protocol.common.util;

import picocli.CommandLine;

import java.util.List;

@CommandLine.Command
public class CsOptions {

    @CommandLine.Option(names = {"-po", "--payment-option"}, required = true, arity = "1", split = ",",
            description = "Define supported payment options")
    public List<String> paymentOptions;
}
