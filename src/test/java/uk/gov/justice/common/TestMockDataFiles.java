package uk.gov.justice.common;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import org.yaml.snakeyaml.Yaml;

public class TestMockDataFiles {

    public static void mockSetupForConfig(final String hostName,
                                          final String hostScheme,
                                          final Integer hostPort,
                                          final int proxyPort,
                                          final String proxyHost,
                                          final String filePath) throws IOException {

        final Map<String, Object> data = new HashMap<>();
        data.put("hostName", hostName);
        data.put("hostScheme", hostScheme);
        data.put("hostPort", hostPort);
        data.put("proxyPort", proxyPort);
        data.put("proxyHost", proxyHost);
        data.put("maximumConnections", 40);
        new Yaml().dump(data, new FileWriter(filePath));
    }

    public static void mockSetupForSearchCriteria(final List<String> keywords,
                                                  final List<String> regexes,
                                                  final int durationMinutes,
                                                  final String from,
                                                  final String to,
                                                  final String filePath)
            throws IOException {
        final Map<String, Object> data = new HashMap<>();
        data.put("keywords", keywords);
        data.put("regexes", regexes);
        data.put("durationMinutes", durationMinutes);
        data.put("fromTime", from);
        data.put("toTime", to);

        new Yaml().dump(data,new FileWriter(filePath));
    }

    public static void mockSetupForUserListFile(final String filePath) throws IOException {
        final CsvMapper mapper = new CsvMapper();
        final List<List<String>> values = new ArrayList<>();
        final List row1Values = new ArrayList();
        row1Values.add("user1");
        row1Values.add("password1");
        values.add(row1Values);
        final List row2Values = new ArrayList();
        row2Values.add("user2");
        row2Values.add("password2");
        values.add(row2Values);
        mapper.writeValue(new FileWriter(filePath), values);
    }
}
