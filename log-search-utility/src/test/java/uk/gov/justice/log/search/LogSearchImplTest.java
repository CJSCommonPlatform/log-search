package uk.gov.justice.log.search;


import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Properties;

import javax.management.MalformedObjectNameException;

import org.jolokia.client.exception.J4pException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LogSearchImplTest {
    private static final String CONTEXT_NAME = "people";
    private static final String JMX_ATTRIBUTE_NAME = "Mean";

    @InjectMocks
    private uk.gov.justice.log.search.LogSearchImpl logSearcher;

    @Mock
    private Properties props;

    @Mock
    private KabanaRestClient artemisJolokiaClient;

    @Test
    public void shouldReturnTimeMessageStaysInCommandQueue() throws MalformedObjectNameException, J4pException {
        when(props.getProperty(anyString())).thenReturn("abc");
      //  when(artemisJolokiaClient.getJmxAttributeValue(anyString(), anyString())).thenReturn(1.0);
        //assertThat(defaultArtemisJmxService.timeMessageStaysInCommandQueue(CONTEXT_NAME, JMX_ATTRIBUTE_NAME), lessThanOrEqualTo(1.0));
    }

}
