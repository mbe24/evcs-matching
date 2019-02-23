package org.beyene.protocol.common.util;

import picocli.CommandLine;
import java.util.regex.Pattern;

public class EndpointConverter implements CommandLine.ITypeConverter<String> {

    private final Pattern pattern = Pattern.compile("^tcp://[^,]+:(\\d+)$");
    private final Pattern patternMultiple = Pattern.compile("^tcp://.+:(\\d+)$");

    @Override
    public String convert(String value) throws Exception {
        if (pattern.matcher(value).find())
            return value;
        else if (patternMultiple.matcher(value).find()) {
            String message = "Multiple arguments are not supported: ";
            throw new Exception(message + value);
        } else {
            String message = "Format needs to be tcp://<host>:<port>. Argument does not match: ";
            throw new Exception(message + value);
        }
    }
}