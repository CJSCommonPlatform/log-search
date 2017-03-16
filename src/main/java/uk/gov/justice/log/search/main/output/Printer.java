package uk.gov.justice.log.search.main.output;

import java.io.IOException;

public interface Printer {

    void writeMessages(final Result result) throws IOException;

    void writeException(final IOException exception) throws IOException;

}
