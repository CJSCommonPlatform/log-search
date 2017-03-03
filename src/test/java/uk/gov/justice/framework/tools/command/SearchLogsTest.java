package uk.gov.justice.framework.tools.command;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.log.search.KibanaQueryBuilder;
import uk.gov.justice.log.search.SearchService;
import uk.gov.justice.log.utils.PropertyReader;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.yaml.snakeyaml.Yaml;


@RunWith(MockitoJUnitRunner.class)
public class SearchLogsTest {

    private static final String CONFIG_PATH = "src/test/resources/config.yaml";
    private static final String SEARCH_PATH = "src/test/resources/search.yaml";
    private static final String HOST_SCHEME = "http";
    private static final String HOST_NAME = "localhost";
    private static final int HOST_PORT = 9205;
    private final Map<String, String> params = new HashMap<>();

    @Mock
    PropertyReader propertyReader;

    @Mock
    SearchService searchService;

    @Mock
    KibanaQueryBuilder kibanaQueryBuilder;

    @Mock
    Response response;

    @InjectMocks
    SearchLogs searchLogs;


    private static void mockSetupForConfig(final String hostName, final String hostScheme, final Integer hostPort,
                                           final int proxyPort, final String proxyHost) throws IOException {

        final Map<String, Object> data = new HashMap<>();
        data.put("hostName", hostName);
        data.put("hostScheme", hostScheme);
        data.put("hostPort", hostPort);
        data.put("proxyPort", proxyPort);
        data.put("proxyHost", proxyHost);

        new Yaml().dump(data, new FileWriter(CONFIG_PATH));
    }

    private static void mockSetupForSearchCriteria(final List<String> keywords, final List<String> regexes,
                                                   final int durationMinutes, final String fromTime, final String toTime) throws IOException {
        final Map<String, Object> data = new HashMap<>();
        data.put("keywords", keywords);
        data.put("regexes", regexes);
        data.put("durationMinutes", durationMinutes);
        if (!StringUtils.isEmpty(fromTime)) {
            data.put("fromTime", fromTime);
        }
        if (!StringUtils.isEmpty(toTime)) {
            data.put("toTime", toTime);
        }
        new Yaml().dump(data, new FileWriter(SEARCH_PATH));
    }

    @Test
    public void shouldSearchViaSearchService() throws Exception {

        final String queryStr = "{}{\"query\":{\"bool\":{\"should\":" +
                "{\"terms\":{\"message\":[\"202\"]}},\"must\":{\"range\":" +
                "{\"@timestamp\":{\"from\":1486654918164,\"to\":1486658518164}}}}}}";
        final HttpEntity query = new NStringEntity(queryStr);
        final String responseStr = "{\n" +
                "  \"responses\": [\n" +
                "    {\n" +
                "      \"took\": 7,\n" +
                "      \"timed_out\": false,\n" +
                "      \"_shards\": {\n" +
                "        \"total\": 50,\n" +
                "        \"successful\": 50,\n" +
                "        \"failed\": 0\n" +
                "      },\n" +
                "      \"hits\": {\n" +
                "        \"total\": 1,\n" +
                "        \"max_score\": 1.9037392,\n" +
                "        \"hits\": [\n" +
                "          {\n" +
                "            \"_index\": \"c2i-2017.02.02\",\n" +
                "            \"_type\": \"wildfly\",\n" +
                "            \"_id\": \"AVn_VDtH4n7ceZwIHKjr\",\n" +
                "            \"_score\": 1.9037392,\n" +
                "            \"_source\": {\n" +
                "              \"@timestamp\": \"2017-02-02T14:57:00.620Z\",\n" +
                "              \"message\": \" [org.wildfly.extension.undertow] (ServerService Thread Pool -- 202) WFLYUT0021: Registered web context: /systemscheduling-main-controller\",\n" +
                "              \"@version\": \"1\",\n" +
                "              \"path\": \"/opt/wildfly/standalone/log/server.log\",\n" +
                "              \"host\": \"embixdapbe01.dev2.cloud.local\",\n" +
                "              \"type\": \"wildfly\",\n" +
                "              \"project\": \"c2i\",\n" +
                "     ¡         \"environment\": \"embixd\",\n" +
                "              \"hostgroup\": \"CP1_env_embixd\",\n" +
                "              \"timestamp\": \"2017-02-02 14:57:00,620\",\n" +
                "              \"severity\": \"INFO\"\n" +
                "            }\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        final HttpEntity responseEntity = new NStringEntity(responseStr, ContentType.APPLICATION_JSON);
        when(kibanaQueryBuilder.entityQuery()).thenReturn(query);
        when(kibanaQueryBuilder.query()).thenReturn(queryStr);

        when(searchService.search(kibanaQueryBuilder)).thenReturn(response);
        when(response.getEntity()).thenReturn(responseEntity);

        mockSetupForConfig(HOST_NAME, HOST_SCHEME, HOST_PORT, 0, "");
        mockSetupForSearchCriteria(Arrays.asList(" log output"), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z");

        final SearchLogs searchLogs = new SearchLogs(kibanaQueryBuilder, searchService);
        final String[] params = new String[]{CONFIG_PATH, SEARCH_PATH, null, null};
        searchLogs.run(params);


        final String responseStrActual = EntityUtils.toString(response.getEntity());
        final Integer hits = JsonPath.read(responseStrActual, "$.responses[0].hits.total");
        final JSONArray messages = JsonPath.read(responseStrActual, "$.responses[0].hits..message");

        assertThat(hits, is(1));
        assertThat(messages.size(), is(1));

        verify(searchService).search(kibanaQueryBuilder);
    }
}
