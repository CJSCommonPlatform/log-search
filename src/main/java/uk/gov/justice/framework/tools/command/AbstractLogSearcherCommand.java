package uk.gov.justice.framework.tools.command;

import uk.gov.justice.log.search.main.output.ConsolePrinter;
import uk.gov.justice.log.search.main.output.OutputPrinter;

import com.beust.jcommander.Parameter;

abstract class AbstractLogSearcherCommand {

    final OutputPrinter consolePrinter = new ConsolePrinter();

    @Parameter(names = "-config", description = "config file yaml path", required = true)
    String configYamlPath;

    @Parameter(names = "-search", description = "search criteria yaml path", required = true)
    String searchCriteriaYamlPath;

    @Parameter(names = "-output", description = "output file path", required = false)
    String outputFilePath;

    @Parameter(names = "-userlist", description = "user list path", required = false)
    String userListFilePath;
}
