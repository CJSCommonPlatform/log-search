Input :
     config.yaml with the following inputs:
       hostName: <<kibana url>>
       hostPort: <<80 or 443>>
       hostScheme: <<http or https>>
       proxyHost: <<proxy ip address>>
       proxyPort: <<proxy port>>and criteria.yaml any where in the file system

     criteria.yaml with the following inputs:
        regexes:
            -  (([0-1]?[0-9]{1,2}\.)|(2[0-4][0-9]\.)|(25[0-5]\.)){3}(([0-1]?[0-9]{1,2})|(2[0-4][0-9])|(25[0-5]))
        keywords:
            - jdbc:
        durationMinutes: 60
        fromTime: "2017-02-21T09:03:25.877Z"
        toTime: "2017-02-24T15:45:25.877Z"
        responseSize: 300

Output: Hits on console and hits and messages on results.html

Run the application:
Application requires 1 command and 3 parameters
    searchlogs = Commmand
    -config   = config yaml file location  (mandatory)
    -search   = criteria yaml file location (mandatory)
    -output   = output file location (optional) will output results.html to the pwd

java -jar log-search.jar searchlogs -config /Users/name/config.yaml -search /Users/name/criteria.yaml -output /Users/name/result.html
