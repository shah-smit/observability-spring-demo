#Filebeat setting filebeat
cd Documents/Dev/ELK/filebeat-7.8.0-darwin-x86_64
sudo chown root filebeat.yml
sudo ./filebeat -e

#Logstash
brew services start elastic/tap/logstash-full
logstash -f /usr/local/etc/logstash/logstash.conf

#Elastic Search
# Route to Elastic Search dir
cd /Users/Smit/Documents/Dev/ELK/elasticsearch-7.8.0 
bin/elasticsearch

#Kibana
# Route to Kibana Search dir
cd /Users/Smit/Documents/Dev/ELK/kibana-7.8.0-darwin-x86_64
bin/kibana

#Kafka
brew cask install java
brew install kafka
zookeeper-server-start /usr/local/etc/kafka/zookeeper.properties


kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic test
kafka-server-start /usr/local/etc/kafka/server.properties
kafka-console-producer --broker-list localhost:9002 --topic test


kafka-console-consumer --bootstrap-server localhost:9002 --topic test --from-beginning

https://gist.github.com/tomysmile/ba6c0ba4488ea51e6423d492985a7953


input {
 file {
    type => "java"
    path => "/Users/Smit/Downloads/chrome/observability/spring_app_log_file.log"
    codec => multiline {
      pattern => "^%{YEAR}-%{MONTHNUM}-%{MONTHDAY} %{TIME}.*"
      negate => "true"
      what => "previous"
    }
  }
}


LOGSTASH CONFIG 03072020232835
```
input {
 file {
    type => "java"
    path => "/Users/Smit/Downloads/chrome/observability/spring_app_log_file.log"
  }
kafka {
    bootstrap_servers => "localhost:9002"
    topics => "test"
    }
}
filter {
  #If log line contains tab character followed by 'at' then we will tag that entry as stacktrace
  if [message] =~ "\tat" {
    grok {
      match => ["message", "^(\tat)"]
      add_tag => ["stacktrace"]
    }
  }
  if [message] =~ "GET Mapping" {
    grok {
       match => { "message" => "%{DATESTAMP:mytimestamp} %{WORD:debugger} %{NUMBER} --- %{GREEDYDATA} %{GREEDYDATA:customerId}" }
	}
   }
  mutate {
    rename => ["host", "hostname"]
    convert => {"hostname" => "string"} 
  }
 
}
 
output {
   
  stdout {
    codec => rubydebug
  }
 
  # Sending properly parsed log events to elasticsearch
  elasticsearch {
    hosts => ["localhost:9200"]
  }
}
```


https://streamsets.com/documentation/datacollector/latest/help/datacollector/UserGuide/Apx-GrokPatterns/GrokPatterns_title.html


Another Sample
```
input {
 file {
    type => "java"
    path => "/Users/Smit/Downloads/chrome/observability/spring_app_log_file.log"
  }
kafka {
    bootstrap_servers => "localhost:9002"
    topics => "test"
    }
}


filter {
  #If log line contains tab character followed by 'at' then we will tag that entry as stacktrace
  grok {
    match => { 'message' => '\[%{TIMESTAMP_ISO8601:date}\],\[%{WORD:client},%{WORD},%{WORD:customerId},%{USERNAME},%{WORD:clientPrefix}\],%{WORD:logMode},%{WORD:subsystem},%{WORD:service}' }
  }

}

output {

  stdout {
    codec => rubydebug
  }

  # Sending properly parsed log events to elasticsearch
  elasticsearch {
    hosts => ["localhost:9200"]
  }
}
```

[2020-06-10T15:05:02,685],[WMB,MBI48901741U90,NXGEN008,MB148901615-d064883782fc4165ba757a,MB],DEBUG,BANKFRAME,CASA,TXNHANDLER,STEPH:::com.bankframe.ei.txnhandler.dataformat.dbs.DataFormatTxn_10601::postProcessor


```
input {
 file {
    type => "java"
    path => "/Users/Smit/Downloads/chrome/observability/spring_app_log_file.log"
  }
kafka {
    bootstrap_servers => "localhost:9002"
    topics => "test"
    }
}

 
filter {
  #If log line contains tab character followed by 'at' then we will tag that entry as stacktrace
  grok {
    match => { 'message' => '\[%{TIMESTAMP_ISO8601:date}\],\[%{WORD:client},%{WORD},%{WORD:customerId},%{USERNAME},%{WORD:clientPrefix}\],%{WORD:logMode},%{WORD:subsystem},%{WORD:service}' }
  }

}
 
output {
   
  stdout {
    codec => rubydebug
  }
 
  # Sending properly parsed log events to elasticsearch
  elasticsearch {
    hosts => ["localhost:9200"]
  }
}

```

More updates
```
input {
 file {
    type => "java"
    path => "/Users/Smit/Downloads/chrome/observability/spring_app_log_file.log"
  }
kafka {
    bootstrap_servers => "localhost:9002"
    topics => "test"
    }
}

 
filter {
  grok {
    match => { 'message' => '\[%{TIMESTAMP_ISO8601:date}\],\[%{WORD:client},%{WORD},%{WORD:customerId},%{USERNAME},%{WORD:clientPrefix}\],%{WORD:logMode},%{WORD:subsystem},%{WORD:service},%{WORD},%{WORD}:::%{USERNAME}::%{WORD} %{GREEDYDATA:request}' }
  }
json{
        source => "request"
        target => "parsedJson"
        remove_field=>["request"]
    }
mutate {
    add_field => {
      "accountNumber" => "%{[parsedJson][AccountNumber]}"
      "amount" => "%{[parsedJson][Amount]}"
      "nickName" => "%{[parsedJson][NickName]}"
    }
  }

}
 
output {
   
  stdout {
    codec => rubydebug
  }
 
  # Sending properly parsed log events to elasticsearch
  elasticsearch {
    hosts => ["localhost:9200"]
  }
}
```

Example for GROK:
Text: [2020-06-10T15:05:02,685],[WMB,MBI48901741U90,NXGEN008,MB148901615-d064883782fc4165ba757a,MB],DEBUG,BANKFRAME,CASA,TXNHANDLER,STEPH:::com.bankframe.ei.txnhandler.dataformat.dbs.DataFormatTxn_10601::postProcessor {"AccountNumber":"123456", "Amount":"100.0", "NickName":"Smit"}

GROK Pattern: \[%{TIMESTAMP_ISO8601:date}\],\[%{WORD:client},%{WORD},%{WORD:customerId},%{USERNAME},%{WORD:clientPrefix}\],%{WORD:logMode},%{WORD:subsystem},%{WORD:service},%{WORD},%{WORD}:::%{USERNAME}::%{WORD} %{GREEDYDATA:request}

```
input {
 file {
    type => "java"
    path => "/Users/Smit/Downloads/chrome/observability/spring_app_log_file.log"
  }
kafka {
    bootstrap_servers => "localhost:9002"
    topics => "test"
    }
}

 
filter {
  grok {
    match => { 'message' => '\[%{TIMESTAMP_ISO8601:date}\],\[%{WORD:client},%{WORD},%{WORD:customerId},%{USERNAME},%{WORD:clientPrefix}\],%{WORD:logMode},%{WORD:subsystem},%{WORD:service},%{WORD},%{WORD}:::%{USERNAME}::%{WORD} %{GREEDYDATA:request}' }
  }
json{
        source => "request"
        target => "parsedJson"
        remove_field=>["request"]
    }
mutate {
    add_field => {
      "accountNumber" => "%{[parsedJson][AccountNumber]}"
      "amount" => "%{[parsedJson][Amount]}"
      "nickName" => "%{[parsedJson][NickName]}"
    }
  }

}
 
output {
   
  stdout {
    codec => rubydebug
  }
 
  # Sending properly parsed log events to elasticsearch
  elasticsearch {
    hosts => ["localhost:9200"]
  }
}
```


Substring logstash
```
input {
kafka {
    bootstrap_servers => "localhost:9002"
    topics => "test"
    codec => json
    }
}

 
filter {
json {
    source=>"message"
    target=>"message"
  }


  ruby {
        code => "
             event.set('ID', event.get('message')[0..11])
             event.set('date', event.get('message')[12..17])
        "
    }

mutate {
    rename => ["host", "hostname"]
    convert => {"hostname" => "string"} 
  }

}
 
output {
   
  stdout {
    codec => rubydebug
  }

 if [message] =~ "R00" {
  # Sending properly parsed log events to elasticsearch
  file {
   path => "/Users/Smit/Downloads/chrome/observability/logstash_dump.txt"
   codec => line { format => "custom format: %{message}"}
  }
 }
  
    elasticsearch {
        hosts => ["localhost:9200"]
    }


}
```


```
input {
kafka {
    bootstrap_servers => "localhost:9002"
    topics => "test"
    codec => json
    }
}

 
filter {
  grok {
    match => { 'message' => '%{DATESTAMP:mytimestamp}%{SPACE}%{WORD}%{SPACE}%{NUMBER} --- %{NOTSPACE} %{NOTSPACE}%{SPACE}:%{SPACE}%{WORD}%{SPACE}%{WORD}%{SPACE}%{GREEDYDATA:request}' }
  }
json{
        source => "request"
        target => "parsedJson"
        remove_field=>["request"]
    }
mutate {
    add_field => {
      "customerId" => "%{[parsedJson][customerId]}"
      "dob" => "%{[parsedJson][dob]}"
    }
  }
ruby {
        code => "
             event.set('customer_cin', event.get('customerId')[0..8])
             event.set('customer_suffix', event.get('customerId')[8..11])
        "
    }
mutate {
    rename => ["host", "hostname"]
    convert => {"hostname" => "string"} 
  }

ruby {
        path => "/usr/local/etc/logstash/wb_log_formatter.rb"
        script_params => { 
          "format_identifier" => "test GET Greeting test" 
        }
      }
}
 
output {
   
  stdout {
    codec => rubydebug
  }


  file {
   path => "/Users/Smit/Documents/Dev/java/observability-spring-demo/logstash_dump.txt"
   codec => line { format => "%{dob_updated}%{customer_cin}%{customer_suffix}"}
  }

  if [dob_updated] != "" {
  # Sending properly parsed log events to elasticsearch
    if [customer_cin] != "" {
      if [customer_suffix] != "" {
          elasticsearch {
            hosts => ["localhost:9200"]
            index => "business_logstash-%{+YYYY.MM.dd}"
          } 
      }
    } 
  }

  
    elasticsearch {
        hosts => ["localhost:9200"]
    }


}
```


19082020 -- 15:45:30

Works for this log: 
```
2020-08-19 11:38:31.651  INFO 54221 --- [nio-8080-exec-1] c.e.o.controller.HomeController          : GET Greeting { "customerId": "S1234567C", "dob":"23022010"}
```

Config:
```
input {
kafka {
    bootstrap_servers => "localhost:9002"
    topics => "test"
    codec => json
    }
}

 
filter {
  grok {
    match => { 'message' => '%{DATESTAMP:mytimestamp}%{SPACE}%{WORD}%{SPACE}%{NUMBER} --- %{NOTSPACE} %{NOTSPACE}%{SPACE}:%{SPACE}%{WORD}%{SPACE}%{WORD}%{SPACE}%{GREEDYDATA:request}' }
  }
  json{
        source => "request"
        target => "parsedJson"
        remove_field=>["request"]
  }
  
  if [parsedJson][customerId] {
    mutate {
      add_field => {
        "customerId" => "%{[parsedJson][customerId]}"
      }
    }
  }

  if [parsedJson][dob] {
    mutate {
      add_field => {
        "dob" => "%{[parsedJson][dob]}"
      }
    }
  }
  ruby {
        code => "
             event.set('customer_cin', event.get('customerId')[0..8])
             event.set('customer_suffix', event.get('customerId')[8..11])
        "
    }
  mutate {
    rename => ["host", "hostname"]
    convert => {"hostname" => "string"} 
  }

  ruby {
      path => "/usr/local/etc/logstash/wb_log_formatter.rb"
      script_params => { 
        "format_identifier" => "test GET Greeting test" 
      }
  }
}
 
output {
   
  stdout {
    codec => rubydebug
  }

  if [dob_updated] and [dob_updated] != "" {
    if [customer_cin] and [customer_cin] != "" {
      if [customer_suffix] and [customer_suffix] != "" {
        # Sending properly parsed log events to elasticsearch
          elasticsearch {
            hosts => ["localhost:9200"]
            index => "business_logstash_02-%{+YYYY.MM.dd}"
          } 

          file {
            path => "/Users/Smit/Documents/Dev/java/observability-spring-demo/logstash_dump.txt"
            codec => line { format => "%{dob_updated}%{customer_cin}%{customer_suffix}"}
          }
      }
    } 
  }

  
    elasticsearch {
        hosts => ["localhost:9200"]
    }


}
```


23rd August

```
input {
kafka {
    bootstrap_servers => "localhost:9002"
    topics => "test"
    codec => json
    }
}
 
filter {

  if [message] =~ 'Request headers' {
    #Extract customerId and traceId
    grok {
      match => { 'message' => 
                        [ 
                          '%{UUID:id} %{NOTSPACE} %{TIMESTAMP_ISO8601:mytimestamp} %{WORD:type} %{DATA} %{DATA} %{WORD:logtype} %{NUMBER:logid} --- \[%{USERNAME}\] %{WORD:test}%{SPACE}:%{SPACE}%{DATA:datatype}: host=\[%{HOSTNAME:host}\], user-agent=\[%{WORD:useragent}\], accept=\[%{DATA:accept}\], actionid=\[%{DATA:actionid}\], authorization=\[%{DATA:authorization}\], channelid=\[%{DATA:channelid}\], client_id=\[%{DATA:client_id}\], content-type=\[%{DATA:contentType}\], customerid=\[%{DATA:customerid}\], locale=\[%{DATA:locale}\], timeout=\[%{DATA:timeout}\], x-b3-spanid=\[%{DATA:spanid}\], x-b3-traceid=\[%{DATA:traceid}\], x-cf-applicationid=\[%{DATA}\], x-cf-instanceid=\[%{DATA}\], x-cf-instanceindex=\[%{DATA}\], x-correlationid=\[%{DATA}\], x-version=\[%{DATA:version}\], x-forwarded-proto=\[%{DATA}\], x-request-start=\[%{DATA}\], x-vcap-request-id=\[%{DATA}\]',
                          '%{UUID:id} %{NOTSPACE} %{TIMESTAMP_ISO8601:mytimestamp} %{WORD:logtype} %{WORD:test}\|%{DATA:apptimestamp}\|%{WORD:country}\|%{DATA:appname}\|%{DATA:functionId}\|%{DATA:serviceId}\|%{DATA:customerid}\|%{DATA:traceid}\|' 
                        ]
      }
    }
    
    if [customerid] {
      aggregate {
       task_id => "%{traceid}"
       code => "
            map['customerid'] = event.get('customerid')
            map['channelid'] = event.get('channelid')"
       map_action => "create"
     }
    }
  }


  if [message] =~ 'Request body' { 
  
    grok {
      match => {
        "message" => "%{UUID:id} %{NOTSPACE} %{TIMESTAMP_ISO8601:mytimestamp} %{WORD:logtype} %{WORD:test}\|%{DATA:apptimestamp}\|%{WORD:country}\|%{DATA:appname}\|%{DATA:functionId}\|%{DATA:serviceId}\|%{DATA:customerid}\|%{DATA:traceid}\|%{GREEDYDATA}\|Request body: %{GREEDYDATA:requestbody}"
      }
    }

    json{
        source => "requestbody"
        target => "parsedJson"
        remove_field=>["requestbody"]
    }

    if [appname] =~ 'rates-service' {

        mutate {
            add_field => {
              "transactionAmount" => "%{[parsedJson][transactionAmount][value]}"
              "millimeters" => "100.0"
            }
        }

        memcached {
          hosts => ["localhost:11211"]
          namespace => "convert_mm"
          set => {
            "[inches]" => "%{millimeters}"
          }
          id => "memcached-set"
        }
    
    } else {

      mutate {
        add_field => {
          "rfqPricingId" => "%{[parsedJson][rfqPricingId]}"
        }
      }

      aggregate {
          task_id => "deal_booking_%{customerid}"
          code => "
            puts 'test'
            event.set('it_works', 'phase execute remittance req body')
            event.set('transactionAmount', map['transactionAmount'])"
          map_action => "update"
          end_of_task => true
          timeout => 120
      }

      aggregate {
          task_id => "%{traceid}"
          code => "
            event.set('it_works', 'phase execute remittance another req body')
            map['rfqPricingId'] = event.get('rfqPricingId')
            map['transactionAmount'] = event.get('transactionAmount')"
          map_action => "update"
      }

      memcached {
        hosts => ["localhost:11211"]
        namespace => "convert_mm"
        get => {
          "%{millimeters}" => "[inches]"
        }
        add_tag => ["from_cache"]
        id => "memcached-get"
      }
    
    }
  
  }

  #ChassisGrafanaLogger
  if [message] =~ 'error classification' {
    #Extract endpoint and traceId

    if [message] =~ 'ChassisGrafanaLogger' {
      grok {
        match => { 'message' => '%{UUID:id} %{NOTSPACE} %{TIMESTAMP_ISO8601:mytimestamp} %{WORD} %{DATA} %{DATA} %{WORD} %{NUMBER:logid} --- \[%{USERNAME}\] %{DATA:test}: :%{GREEDYDATA:request}' }
      }
    } else {
      grok {
        match => { 'message' => '%{UUID:id} %{NOTSPACE} %{TIMESTAMP_ISO8601:mytimestamp} %{WORD:test} :%{GREEDYDATA:request}' }
      }
    }

    json{
        source => "request"
        target => "parsedJson"
        remove_field=>["request"]
    }

    mutate {
      add_field => {
        "traceid" => "%{[parsedJson][traceId]}"
        "endpoint" => "%{[parsedJson][endpoint]}"
        "request_type" => "%{[parsedJson][api_type]}"
      }
    }

    aggregate {
       task_id => "%{traceid}"
       code => "event.set('customerid', map['customerid'])"
       map_action => "update"
     }
    
    if [request_type] =~ "request" {
      aggregate {
       task_id => "%{traceid}"
       code => "map['endpoint'] = event.get('endpoint')"
       map_action => "update"
     }
    }

    if [request_type] =~ "response" {
      if [endpoint] =~ "INTERBANK" {
       mutate {
        add_field => {
          "host_call" => "10 415"
        }
       }
     }
     if [endpoint] =~ "INTRABANK" {
        mutate {
        add_field => {
          "host_call" => "10 601"
        }
       }
     }

     if [endpoint] =~ 'remit-transfer' {
        mutate {
          add_field => {
            "host_call" => "10 111"
          }
        }
     
     }
    

      aggregate {
          task_id => "%{traceid}"
          code => "
                event.set('found_all_logs', 'true') 
                event.set('customerid', map['customerid'])
                event.set('endpoint', map['endpoint'])
                event.set('channelid', map['channelid'])
                event.set('rfqPricingId', map['rfqPricingId'])
                event.set('transactionAmount', map['transactionAmount'])
                event.set('host_call',  event.get('host_call'))"
          map_action => "update"
          end_of_task => true
          timeout => 120
      }
    }
  }

  mutate {
      rename => ["host", "hostname"]
      convert => {"hostname" => "string"} 
  }

  if [host_call] and [host_call] != "" {
    ruby {
        code => "
             event.set('customer_suffix', event.get('customerid')[0..2])
             event.set('customer_cin', event.get('customerid')[2..11])
        "
    }
    
    ruby {
        path => "/usr/local/etc/logstash/wb_log_formatter.rb"
        script_params => { 
          "random_identifier" => "test GET Greeting test" 
        }
    }
  }
  
}
 
output {
   
  stdout {
    codec => rubydebug
  }

  if [host_call] and [host_call] != "" {
        # Sending properly parsed log events to elasticsearch
    elasticsearch {
      hosts => ["localhost:9200"]
      index => "business_logstash_06-%{+YYYY.MM.dd}"
    } 

    file {
      path => "/Users/Smit/Documents/Dev/java/observability-spring-demo/logstash_dump.txt"
      codec => line { format => "%{wbsp}"}
    }
  }

  
    elasticsearch {
        hosts => ["localhost:9200"]
    }
}
```


MemCached Example
```
input {
  kafka {
    bootstrap_servers => "localhost:9002"
    topics => "test"
    codec => json
  }
}

filter {

  if [message] =~ 'Request headers' {

    mutate {
      add_field => {
        "phase" => "RequestHeaders"
      }
    }


    memcached {
      hosts => ["localhost"]
      namespace => "convert_mm"
      #"field1"           => "memcached-key-1"
      set => {
        "phase" => "foo"
      }
      id => "memcached-set"
    }
  }


  if [message] =~ 'Request body' {

    mutate {
      add_field => {
        "phase" => "RequestBody"
      }
    }

    memcached {
      hosts => ["localhost"]
      namespace => "convert_mm"
      #"memcached-key-1" => "field1"
      get => {
        "foo" => "phase"
      }
      add_tag => ["from_cache"]
      id => "memcached-get"
    }
  }

  mutate {
    rename => ["host", "hostname"]
    convert => {
      "hostname" => "string"
    }
  }

}

output {

  stdout {
    codec => rubydebug
  }

  if [host_call] and [host_call] != "" {
    # Sending properly parsed log events to elasticsearch
    elasticsearch {
      hosts => ["localhost:9200"]
      index => "business_logstash_06-%{+YYYY.MM.dd}"
    }

    file {
      path => "/Users/Smit/Documents/Dev/java/observability-spring-demo/logstash_dump.txt"
      codec => line {
        format => "%{wbsp}"
      }
    }
  }


  elasticsearch {
    hosts => ["localhost:9200"]
    index => "logstash_06-%{+YYYY.MM.dd}"
  }
}
```