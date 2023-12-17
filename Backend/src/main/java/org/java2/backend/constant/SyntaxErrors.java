package org.java2.backend.constant;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SyntaxErrors {
    public static final List<String> ERRORS = Arrays.asList(
            "Missing semicolon",
            "Missing bracket",
            "Misspelled keyword",
            "Invalid Variable or Function or Class name",
            "Cannot find the symbol",
            "Missing double-quote in String",
            "Missing parenthesis",
            "Missing square brackets",
            "Missing curly braces",
            "Not a statement"
    );
    public static final Map<String,String> ErrorMessages = Map.ofEntries(
            Map.entry("Missing semicolon","';' expected"),
            Map.entry("Missing bracket","reached end of file while parsing"),
            Map.entry("Misspelled keyword","package .* does not exist"),
            Map.entry("Invalid Variable or Function or Class name","identifier expected"),
            Map.entry("Cannot find the symbol","cannot find symbol"),
            Map.entry("Missing double-quote in String","')' expected.*error: not a statement.*error: ';' expected"),
            Map.entry("Missing parenthesis","')' expected"),
            Map.entry("Missing square brackets","']' expected"),
            Map.entry("Missing curly braces","'}' expected"),
            Map.entry("Not a statement","not a statement")
    );


}
