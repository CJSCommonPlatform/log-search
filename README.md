# Search Kibana Logs

Application to search for any exact matches of keywords or regexes appearing anywhere in message field of aggregate Kibana (elastic search) logs
- Keyword is mandatory
- Regexes is optional
- One of duration minutes or From/time in ISO Format should be specified

## How to Run the application
* To run this application you would require 5 parameters to be passed as below
    - -config   = config yaml file location  (mandatory)
    - -search   = criteria yaml file location (mandatory)
    - -userlist = user list csv file(optional)
    - -output   = Result file location (optional) default to pwd as results.html
    - -msg = Display messages on the console additional (yes/no) default yes

_java -jar log-search.jar  -config <path>/config.yaml -search <path>/criteria.yaml -userlist <path>/userlist.json 
        -output <path>/result.html -msg yes_

See examples of command parameters discussed in detail below

## Inputs
* Input: config.yaml (Mandatory)

       hostName: <<kibana url>>
       hostPort: <<80 or 443>>
       hostScheme: <<http or https>>
       proxyHost: <<proxy ip address>>
       proxyPort: <<proxy port>>
    
* Criteria Input: criteria.yaml (Mandatory)
 
       regexes:
            -  (([0-1]?[0-9]{1,2}\.)|(2[0-4][0-9]\.)|(25[0-5]\.)){3}(([0-1]?[0-9]{1,2})|(2[0-4][0-9])|(25[0-5]))
       keywords:
            - jdbc:
       durationMinutes: 60 ( Either Duration Minutes or From/To time)

        fromTime: "2017-02-21T09:03:25.877Z"
        toTime: "2017-02-24T15:45:25.877Z"

        responseSize: 300

* User list: userlist.csv (Optional)
        charginglawyer001@test.cps.gov.uk,SIT - Performance Test Organisation,Mr,Sean,William,Reynolds,
        admin,Password123!

* Result output path: Results file location (optional) will output results.html to the working directory

## Outputs
* Output: 
    - Hits on console and hits and messages on results output
    - Supports text files if the output file specified with  .txt extension otherwise defaults to html
    
    

[![Build Status](https://travis-ci.org/CJSCommonPlatform/log-search.svg?branch=master)](https://travis-ci.org/CJSCommonPlatform/log-search) [![Coverage Status](https://coveralls.io/repos/github/CJSCommonPlatform/log-search/badge.svg?branch=master)](https://coveralls.io/github/CJSCommonPlatform/log-search?branch=master)
