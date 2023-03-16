package io.github.jpmorganchase.fusion.parsing;

public class ParsingException extends RuntimeException {

    public ParsingException(String s) {
        super(s);
    }

    public ParsingException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
