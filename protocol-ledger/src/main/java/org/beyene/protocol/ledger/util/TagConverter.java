package org.beyene.protocol.ledger.util;

import picocli.CommandLine;

import java.util.regex.Pattern;

public class TagConverter implements CommandLine.ITypeConverter<String> {

    private final Pattern pattern = Pattern.compile("^[a-zA-z9]{1,27}$");

    @Override
    public String convert(String value) throws Exception {
        if (pattern.matcher(value).find())
            return value;
        else if (value.length() < 1 || value.length() > 27) {
            String message = "Tag length needs to be in [1, 27]. Argument does not match: ";
            throw new Exception(message + value);
        } else {
            String message = "Tag characters must be alphabetical or 9. Argument does not match: ";
            throw new Exception(message + value);
        }
    }
}
