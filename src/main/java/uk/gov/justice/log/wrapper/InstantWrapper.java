package uk.gov.justice.log.wrapper;

import java.time.Instant;

/**
 * Used for mocking now values
 */
public class InstantWrapper {

    /**
     * Now instant.
     *
     * @return the instant
     */
    public Instant now() {
        return Instant.now();
    }
}
