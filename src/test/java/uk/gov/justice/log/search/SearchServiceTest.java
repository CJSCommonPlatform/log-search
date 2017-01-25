package uk.gov.justice.log.search;


import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.log.utils.CommonConstant.ELASTIC_MULTI_SEARCH_URL;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SearchServiceTest {

    private final Map<String, String> params = new HashMap<>();

    @Mock
    RestClient restClient;

    @Mock
    KibanaQueryBuilder kibanaQueryBuilder;

    @Mock
    Response response;

    private SearchService searchService;

    @Before
    public void setUp() {
        searchService = new SearchService(restClient);
    }

    @Test
    public void shouldReturnReponse() throws IOException {
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
        searchService.setParams(params);

        when(kibanaQueryBuilder.entityQuery()).thenReturn(query);
        when(restClient.performRequest("POST", ELASTIC_MULTI_SEARCH_URL, params, query)).
                thenReturn(response);
        when(response.getEntity()).thenReturn(responseEntity);

        final Response response = searchService.search(kibanaQueryBuilder);

        final String responseStrActual = EntityUtils.toString(response.getEntity());
        final Integer hits = JsonPath.read(responseStrActual, "$.responses[0].hits.total");

        final JSONArray messages = JsonPath.read(responseStrActual, "$.responses[0].hits..message");
        assertThat(hits, is(1));
        assertThat(messages.size(), is(1));
    }
}
