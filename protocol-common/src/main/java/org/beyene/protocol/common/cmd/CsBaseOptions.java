package org.beyene.protocol.common.cmd;

import picocli.CommandLine;

import java.util.List;

public class CsBaseOptions {

    @CommandLine.Option(names = {"-po", "--payment-option"}, required = true, arity = "1", split = ",",
            description = "Define supported payment options")
    public List<String> paymentOptions;
}
