package uk.gov.justice.log.search;

public class KeywordFoundException extends RuntimeException {

    public KeywordFoundException(final String message) {
        super(message);
    }

    public KeywordFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
