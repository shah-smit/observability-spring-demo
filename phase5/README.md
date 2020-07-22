### Version 5.0

Log

```
[2020-06-10T15:05:02,685],[WMB,MBI48901741U90,TRUMP,MB148901615-d064883782fc4165ba757a,MB],DEBUG,BANKFRAME,CASA,TXNHANDLER,STEPH:::com.bankframe.ei.txnhandler.dataformat.dbs.DataFormatTxn_10601::postProcessor
[2020-06-10T15:05:02,685],[WMB,MBI48901741U90,TRUMP,MB148901615-d064883782fc4165ba757a,MB],INFO,BANKFRAME,CASA,TXNHANDLER,STEPH:::com.bankframe.ei.txnhandler.dataformat.dbs.DataFormatTxn_10601::postProcessor
```

Kafka (Consumer Snippet), sent from Filebeat

```json5
{"@timestamp":"2020-07-22T17:52:34.556Z","@metadata":{"beat":"filebeat","type":"_doc","version":"7.8.0"},"tags":["observability","audit"],"input":{"type":"log"},"agent":{"ephemeral_id":"39e1b812-8789-4500-9be5-7ba0bdb0d90b","id":"864be1a9-e233-4d41-8624-cf94e916a0b7","name":"Smits-MacBook-Pro.local","type":"filebeat","version":"7.8.0","hostname":"Smits-MacBook-Pro.local"},"ecs":{"version":"1.5.0"},"host":{"name":"Smits-MacBook-Pro.local"},"log":{"file":{"path":"/Users/Smit/Downloads/chrome/observability/spring_app_log_file.log"},"offset":3878},"message":"[2020-06-10T15:05:02,685],[WMB,MBI48901741U90,TRUMP,MB148901615-d064883782fc4165ba757a,MB],DEBUG,BANKFRAME,CASA,TXNHANDLER,STEPH:::com.bankframe.ei.txnhandler.dataformat.dbs.DataFormatTxn_10601::postProcessor"}
```

```json5
{"@timestamp":"2020-07-22T17:52:34.556Z","@metadata":{"beat":"filebeat","type":"_doc","version":"7.8.0"},"ecs":{"version":"1.5.0"},"host":{"name":"Smits-MacBook-Pro.local"},"agent":{"id":"864be1a9-e233-4d41-8624-cf94e916a0b7","name":"Smits-MacBook-Pro.local","type":"filebeat","version":"7.8.0","hostname":"Smits-MacBook-Pro.local","ephemeral_id":"39e1b812-8789-4500-9be5-7ba0bdb0d90b"},"log":{"offset":4087,"file":{"path":"/Users/Smit/Downloads/chrome/observability/spring_app_log_file.log"}},"message":"[2020-06-10T15:05:02,685],[WMB,MBI48901741U90,TRUMP,MB148901615-d064883782fc4165ba757a,MB],INFO,BANKFRAME,CASA,TXNHANDLER,STEPH:::com.bankframe.ei.txnhandler.dataformat.dbs.DataFormatTxn_10601::postProcessor","tags":["observability","audit"],"input":{"type":"log"}}
```

Logstash Picking up:

```
{
      "@timestamp" => 2020-07-22T17:52:34.556Z,
      "customerId" => "TRUMP",
        "@version" => "1",
             "ecs" => {
        "version" => "1.5.0"
    },
            "date" => "2020-06-10T15:05:02,685",
          "client" => "WMB",
             "log" => {
        "offset" => 3878,
          "file" => {
            "path" => "/Users/Smit/Downloads/chrome/observability/spring_app_log_file.log"
        }
    },
         "message" => "[2020-06-10T15:05:02,685],[WMB,MBI48901741U90,TRUMP,MB148901615-d064883782fc4165ba757a,MB],DEBUG,BANKFRAME,CASA,TXNHANDLER,STEPH:::com.bankframe.ei.txnhandler.dataformat.dbs.DataFormatTxn_10601::postProcessor",
    "clientPrefix" => "MB",
       "subsystem" => "BANKFRAME",
         "service" => "CASA",
           "agent" => {
                  "id" => "864be1a9-e233-4d41-8624-cf94e916a0b7",
                "type" => "filebeat",
                "name" => "Smits-MacBook-Pro.local",
        "ephemeral_id" => "39e1b812-8789-4500-9be5-7ba0bdb0d90b",
             "version" => "7.8.0",
            "hostname" => "Smits-MacBook-Pro.local"
    },
            "tags" => [
        [0] "observability",
        [1] "audit",
        [2] "_jsonparsefailure"
    ],
           "input" => {
        "type" => "log"
    },
         "logMode" => "DEBUG",
        "hostname" => {
        "name" => "Smits-MacBook-Pro.local"
    }
}
{
      "@timestamp" => 2020-07-22T17:52:34.556Z,
      "customerId" => "TRUMP",
        "@version" => "1",
             "ecs" => {
        "version" => "1.5.0"
    },
            "date" => "2020-06-10T15:05:02,685",
          "client" => "WMB",
             "log" => {
        "offset" => 4087,
          "file" => {
            "path" => "/Users/Smit/Downloads/chrome/observability/spring_app_log_file.log"
        }
    },
         "message" => "[2020-06-10T15:05:02,685],[WMB,MBI48901741U90,TRUMP,MB148901615-d064883782fc4165ba757a,MB],INFO,BANKFRAME,CASA,TXNHANDLER,STEPH:::com.bankframe.ei.txnhandler.dataformat.dbs.DataFormatTxn_10601::postProcessor",
    "clientPrefix" => "MB",
       "subsystem" => "BANKFRAME",
         "service" => "CASA",
           "agent" => {
                  "id" => "864be1a9-e233-4d41-8624-cf94e916a0b7",
                "type" => "filebeat",
                "name" => "Smits-MacBook-Pro.local",
            "hostname" => "Smits-MacBook-Pro.local",
             "version" => "7.8.0",
        "ephemeral_id" => "39e1b812-8789-4500-9be5-7ba0bdb0d90b"
    },
            "tags" => [
        [0] "observability",
        [1] "audit",
        [2] "_jsonparsefailure"
    ],
           "input" => {
        "type" => "log"
    },
         "logMode" => "INFO",
        "hostname" => {
        "name" => "Smits-MacBook-Pro.local"
    }
}
```

Note: First log will go to `lunaindex-*` indexed as it has a logMode of `DEBUG` where all other type of the logs will go to default `logstash-2020.06.27-000001`


### Version 5.1

Same as Version 5.0, but instead of sending to two different elastic index, i am sending a log with "DEBUG" keyword to `logstash_dump.txt` however still sending all the logs to default elastic search.

As such the above still remains true, except now there will a new file created and here is sample data posted to the file:

```
custom format: [2020-06-10T15:05:02,685],[WMB,MBI48901741U90,SMITSHAH,MB148901615-d064883782fc4165ba757a,MB],DEBUG,BANKFRAME,CASA,TXNHANDLER,STEPH:::com.bankframe.ei.txnhandler.dataformat.dbs.DataFormatTxn_10601::postProcessor
custom format: [2020-06-10T15:05:02,685],[WMB,MBI48901741U90,TESTSHAH,MB148901615-d064883782fc4165ba757a,MB],DEBUG,BANKFRAME,CASA,TXNHANDLER,STEPH:::com.bankframe.ei.txnhandler.dataformat.dbs.DataFormatTxn_10601::postProcessor
 ```

