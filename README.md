## Observability Using ELK

The whole setup is on MacOS v10.15.5 (19F101)

### Table of Content

- [Phase 1](#Phase-1) `SpringBoot <> LogStash <> Elastic Search <> Kibana`
- [Phase 2](#Phase-2) `SpringBoot <> Filebeat <> LogStash <> Elastic Search <> Kibana`
- [Phase 3](#Phase-3) `SpringBoot <> Filebeat / Kafka <> LogStash <> Elastic Search <> Kibana`
- [Phase 4](#Phase-4) `Enchanged Phase 3`
- [Phase 5](#Phase-5) `SpringBoot <> Filebeat / Kafka <> LogStash <> Elastic Search #1 / Elastic Search #2  <> Kibana`
- [Phase 6](#Phase-6) `Extracting fields from log line using Substring`
- [Phase 7](#Phase-7) `Writing custom Ruby Script`
- [Phase 8](#Phase-8) `Implemented Logstash Aggregate Filter`
- [Phase 9](#Phase-9) `Memchache to join custom events`


### Phase 1

Data flow as follows:
```
SpringBoot <> LogStash <> Elastic Search <> Kibana
```

Download Elastic Search and Kibana from:
- https://www.elastic.co/downloads/elasticsearch
- https://www.elastic.co/downloads/kibana

note: Try not to download using homebrew as config may not be under our control. Such that download and put those in `ELK` folder.

For LogStash:
```bash
brew install elastic/tap/logstash-full
```

To Start logstash:
```bash
brew services start elastic/tap/logstash-full
```

However, if you need to see logs, then try below
```bash
logstash
```

Also, use the below `logstash.confg` file:
```
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
 
filter {
  #If log line contains tab character followed by 'at' then we will tag that entry as stacktrace
  if [message] =~ "\tat" {
    grok {
      match => ["message", "^(\tat)"]
      add_tag => ["stacktrace"]
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

If you want to use the config, you can provide `-p` flag:
```bash
logstash -f /usr/local/etc/logstash/logstash.conf
```

Also, before start Kibana, ensure to add the below line to `kibana.yml`:
```
elasticsearch.hosts: ["http://localhost:9200"]
```

Issues:
1. If you find issues regarding unauthorised start for logstash or elastic, mac OS may disallow, in that case, go to `System Prefrences` then `Security and Privacy` and then `Developer` and allow permissions to `terminal` app



### Phase 2

Data flow as follows:
```
SpringBoot <> Filebeat <> LogStash <> Elastic Search <> Kibana
```


#### Download FileBeat
Installation Url: https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation.html

Steps:
```bash
curl -L -O https://artifacts.elastic.co/downloads/beats/filebeat/filebeat-7.8.0-darwin-x86_64.tar.gz
tar xzvf filebeat-7.8.0-darwin-x86_64.tar.gz
```

Update contents for `filebeat.yml` file as below:

note: Please update the path to the log file
```yaml
filebeat:
  inputs:
    - type: log
      paths:
        - /Users/Smit/Downloads/chrome/observability/spring_app_log_file.log
      tags: ["observability", "audit"]
      include_lines: ['AUDIT_LOG']

    - type: log
      paths:
        - /Users/Smit/Documents/Dev/java/spring-boot-graphql-query-example/spring_app_log_file.log
      tags: ["graphql"]
      
output:
  logstash:
    hosts: ["localhost:5044"]
```

After editing the file, start the server:
```bash
sudo chown root filebeat.yml
sudo ./filebeat -e
```

Also, will have to configure the input for LogStash to listen to Filebeat (category beats) instead of log file
```
input {
  beats {
    port => "5044"
	type => "log"
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

Issues:
1. https://stackoverflow.com/questions/62613671/logstash-error-cant-get-text-on-a-start-object-at-1708/62614015#62614015


Kafka:
- https://medium.com/@Ankitthakur/apache-kafka-installation-on-mac-using-homebrew-a367cdefd273


### Phase 3

Data flow as follows:
```
SpringBoot <> Filebeat <> Kafka <> LogStash <> Elastic Search <> Kibana
```

In this setup, I tried to use multiple-inputs for Logstash, where input can be from filebeats, or from Kafka on a particular topic.

```
input {
  beats {
        port => "5044"
	type => "log"
	tags => ["beats"]
  }
  kafka {
	bootstrap_servers => "localhost:9002"
	topics => ["test"]
	tags => ["kafka-stream"]
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

### Phase 3.1

Data flow as follows:
```
SpringBoot <> Filebeat(include:filter) <> Kafka <> LogStash <> Elastic Search <> Kibana
```

```yaml
filebeat:
  inputs:
    - type: log
      paths:
        - /Users/Smit/Downloads/chrome/observability/spring_app_log_file.log
      tags: ["observability", "audit"]
      include_lines: ['AUDIT_LOG']

    - type: log
      paths:
        - /Users/Smit/Documents/Dev/java/spring-boot-graphql-query-example/spring_app_log_file.log
      tags: ["graphql"]
      
output:
  kafka:
    hosts: ["localhost:9002"]
    topic: "test"
    type: json
```

### Phase 4

Same flow, but adding more functionality at the Logstash Config:

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

### Phase 5

This phase Logstash outputs to two different Elastic Search Index

Data flow as follows:
```
SpringBoot <> Filebeat <> Kafka <> LogStash <> Elastic Search <> Elastic Search <> Kibana
```

Filebeat.yml:

```yaml
filebeat:
  inputs:
    - type: log
      paths:
        - /Users/Smit/Downloads/chrome/observability/spring_app_log_file.log
      tags: ["observability", "audit"]
     # include_lines: ['AUDIT_LOG']

    - type: log
      paths:
        - /Users/Smit/Documents/Dev/java/spring-boot-graphql-query-example/spring_app_log_file.log
      tags: ["graphql"]
      
output:
  kafka:
    hosts: ["localhost:9002"]
    topic: "test"
```

Logstash.conf

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

  grok {
    match => { 'message' => '\[%{TIMESTAMP_ISO8601:date}\],\[%{WORD:client},%{WORD},%{WORD:customerId},%{USERNAME},%{WORD:clientPrefix}\],%{WORD:logMode},%{WORD:subsystem},%{WORD:service},%{WORD},%{WORD}:::%{USERNAME}::%{WORD}' }
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

  if [logMode] =~ "DEBUG" {
  # Sending properly parsed log events to elasticsearch
    elasticsearch {
        hosts => ["localhost:9200"]
        index => "lunaindex-%{+YYYY.MM.dd}"
    }  
  }
  else {
    elasticsearch {
        hosts => ["localhost:9200"]
    }
  }
}
```

Note: for Datas, screenshots, please view `phase5` folder and its readme.

Follow Up: What if I want to send all logs to default elastic search but certain keyworded log to a plain file:

Here is `logstash.conf` file:
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


  grok {
    match => { 'message' => '\[%{TIMESTAMP_ISO8601:date}\],\[%{WORD:client},%{WORD},%{WORD:customerId},%{USERNAME},%{WORD:clientPrefix}\],%{WORD:logMode},%{WORD:subsystem},%{WORD:service},%{WORD},%{WORD}:::%{USERNAME}::%{WORD}' }
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

 if [logMode] =~ "DEBUG" {
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

### Phase 6

In this phase, I tried to play with a log that requires sub-string, for example, I am given the below log line:

```
01999918000170702135929%WS%00000000000070030819078820913135929050000080RRN0002W900500000000C0000000000000500024464063100100 03081907882 300 R00
```

I need to extract the log base on a length, for example the first 11 characters are `ID` and next 5 characters are `date`

Use `ruby` filter in the logstash to perform substring

```
filter {
    ruby {
        code => "
             event.set('ID', event.get('message')[0..11])
             event.set('date', event.get('message')[12..17])
        "
    }
}
```

The full logstash may look like:

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
  
  elasticsearch {
        hosts => ["localhost:9200"]
  }
}
```


### Phase 7

In this phase, I tried to play with a log that requires further appending of empty strings at the end. Current implemention is done via Ruby script.

`wb_log_formatter.rb` file
```ruby
def register(params)
    @customer_cin = params["customer_cin"]
    @customer_suffix = params["customer_suffix"]
    @format_identifier = params["format_identifier"]
end

def filter(event)
    if event.get('message').include?("GET Greeting")

        if !event.get('dob').nil?
            # dob == 9 Characters
            dob = event.get("dob")
            puts dob
            dob = dob + "         "
            dob = dob[0, 9]
            event.set("dob_updated",dob)
        end
    end
    return [event]
end
```

`logstash.conf` file
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

##### References:
- https://github.com/jsvd/logstash-filter-ruby-scripts/blob/master/scripts/string_size.rb
- https://stackoverflow.com/questions/63479922/logstash-add-extra-space-on-a-particular-field-based-on-the-config
- https://discuss.elastic.co/t/logstash-add-extra-space-on-a-particular-field-based-on-the-config/245520
- https://youtu.be/a4gOU4wxUAs  #Its private video, please call @shah-smit

#### Phase 8

This phase I have tried to Logstash Agregate Filter

`logstash.conf` file:
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
      match => { 'message' => '%{UUID:id} %{NOTSPACE} %{TIMESTAMP_ISO8601:mytimestamp} %{WORD:type} %{DATA} %{DATA }%{WORD:logtype} %{NUMBER:logid} --- \[%{USERNAME}\] %{WORD:test}%{SPACE}:%{SPACE}%{DATA:datatype}: host=\[%{HOSTNAME:host}\], user-agent=\[%{WORD:useragent}\], accept=\[%{DATA:accept}\], actionid=\[%{DATA:actionid}\], authorization=\[%{DATA:authorization}\], channelid=\[%{DATA:channelid}\], client_id=\[%{DATA:client_id}\], content-type=\[%{DATA:contentType}\], customerid=\[%{DATA:customerid}\], locale=\[%{DATA:locale}\], timeout=\[%{DATA:timeout}\], x-b3-spanid=\[%{DATA:spanid}\], x-b3-traceid=\[%{DATA:traceid}\], x-cf-applicationid=\[%{DATA}\], x-cf-instanceid=\[%{DATA}\], x-cf-instanceindex=\[%{DATA}\], x-correlationid=\[%{DATA}\], x-version=\[%{DATA:version}\], x-forwarded-proto=\[%{DATA}\], x-request-start=\[%{DATA}\], x-vcap-request-id=\[%{DATA}\]' }
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

  if [message] =~ 'ChassisGrafanaLogger' {
    #Extract endpoint and traceId
    grok {
      match => { 'message' => '%{UUID:id} %{NOTSPACE} %{TIMESTAMP_ISO8601:mytimestamp} %{WORD} %{DATA} %{DATA} %{WORD} %{NUMBER:logid} --- \[%{USERNAME}\] %{DATA:test}: :%{GREEDYDATA:request}' }
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
    

      aggregate {
          task_id => "%{traceid}"
          code => "
                event.set('found_all_logs', 'true') 
                event.set('customerid', map['customerid'])
                event.set('endpoint', map['endpoint'])
                event.set('channelid', map['channelid'])
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
        path => "/usr/local/etc/logstash/wb_log_formatter.rb"
        script_params => { 
          "random_identifier" => "test GET Greeting test" 
        }
    }

    ruby {
        code => "
             event.set('customer_suffix', event.get('customerid')[0..2])
             event.set('customer_cin', event.get('customerid')[2..11])
        "
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
      index => "business_logstash_03-%{+YYYY.MM.dd}"
    } 

    file {
      path => "/Users/Smit/Documents/Dev/java/observability-spring-demo/logstash_dump.txt"
      codec => line { format => "%{customerid}%{channelid}%{customer_cin}%{customer_suffix}"}
    }
  }

  
    elasticsearch {
        hosts => ["localhost:9200"]
    }
}
```
`wb_log_formatter.rb` file:
```
def register(params)
    @customer_cin = params["customer_cin"]
    @customer_suffix = params["customer_suffix"]
    @format_identifier = params["format_identifier"]
end

def filter(event)
    if event.get('endpoint').include?("INTERBANK")
            event.set("WBLOG_PARTA","10")
            event.set("WBLOG_PARTB","415")
    end

    # channelid == 4 Characters
    channelid = event.get("channelid")
    puts channelid
    channelid_updated = channelid + "         "
    channelid_updated = channelid_updated[0, 3]
    event.set("channelid",channelid_updated)

    host_call = event.get("host_call")
    puts host_call
    host_call = host_call.gsub(' ', '')
    event.set("host_call",host_call)
    return [event]
end
```

### Phase 9

- [Phase 9.1](#Phase-9.1) `Multiple Memchace Implementation`

logstash.conf
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
      match => {
        'message' =>
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

    json {
      source => "requestbody"
      target => "parsedJson"
      remove_field => ["requestbody"]
    }

    if [appname] =~ 'rates-service' {

      mutate {
        add_field => {
          "transactionAmount" => "%{[parsedJson][transactionAmount][value]}"
        }
      }

      memcached {
        hosts => ["localhost:11211"]
        namespace => "convert_mm"
        #"field1"           => "memcached-key-1"
        set => {
          "transactionAmount" => "deal_booking_%{customerid}"
        }
        id => "memcached-set"
      }

    }
    else {

      mutate {
        add_field => {
          "rfqPricingId" => "%{[parsedJson][rfqPricingId]}"
        }
      }

      memcached {
        hosts => ["localhost:11211"]
        namespace => "convert_mm"
        #"memcached-key-1" => "field1"
        get => {
          "deal_booking_%{customerid}" => "transactionAmount"
        }
        add_tag => ["from_cache"]
        id => "memcached-get"
      }

      aggregate {
        task_id => "%{traceid}"
        code => "
            event.set('it_works', 'phase execute remittance another req body')
            map['rfqPricingId'] = event.get('rfqPricingId')
            map['transactionAmount'] = event.get('transactionAmount')"
        map_action => "update"
      }

    }

  }

  #ChassisGrafanaLogger
  if [message] =~ 'error classification' {
    #Extract endpoint and traceId

    if [message] =~ 'ChassisGrafanaLogger' {
      grok {
        match => {
          'message' => '%{UUID:id} %{NOTSPACE} %{TIMESTAMP_ISO8601:mytimestamp} %{WORD} %{DATA} %{DATA} %{WORD} %{NUMBER:logid} --- \[%{USERNAME}\] %{DATA:test}: :%{GREEDYDATA:request}'
        }
      }
    }
    else {
      grok {
        match => {
          'message' => '%{UUID:id} %{NOTSPACE} %{TIMESTAMP_ISO8601:mytimestamp} %{WORD:test} :%{GREEDYDATA:request}'
        }
      }
    }

    json {
      source => "request"
      target => "parsedJson"
      remove_field => ["request"]
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

      if [endpoint] =~ 'fx-rates' {
        mutate {
          add_field => {
            "host_call" => "10 555"
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
    convert => {
      "hostname" => "string"
    }
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
      index => "business_logstash_07-%{+YYYY.MM.dd}"
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
  }
}
```


#### Phase 9.1

Using multiple Memcache filter to combine data and use it later...

logstash.conf
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
      match => {
        'message' => 
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

    json {
      source => "requestbody"
      target => "parsedJson"
      remove_field => ["requestbody"]
    }

    if [appname] =~ 'rates-service' {

      mutate {
        add_field => {
          "transactionAmount" => "%{[parsedJson][transactionAmount][value]}"
        }
      }

      memcached {
        hosts => ["localhost"]
        namespace => "convert_mm"
        #"field1"           => "memcached-key-1"
        set => {
          "transactionAmount" => "deal_booking_%{customerid}"
        }
        id => "memcached-set"
      }

    }
    else {

      mutate {
        add_field => {
          "rfqPricingId" => "%{[parsedJson][rfqPricingId]}"
        }
      }

      memcached {
        hosts => ["localhost"]
        namespace => "convert_mm"
        #"memcached-key-1" => "field1"
        get => {
          "deal_booking_%{customerid}" => "transactionAmount"
        }
        add_tag => ["from_cache"]
        id => "memcached-get"
      }

      aggregate {
        task_id => "%{traceid}"
        code => "
            event.set('it_works', 'phase execute remittance another req body')
            map['rfqPricingId'] = event.get('rfqPricingId')
        map['transactionAmount'] = event.get('transactionAmount')"
        map_action => "update"
      }

    }

  }

  #ChassisGrafanaLogger
  if [message] =~ 'error classification' {
    #Extract endpoint and traceId

    if [message] =~ 'ChassisGrafanaLogger' {
      grok {
        match => {
          'message' => '%{UUID:id} %{NOTSPACE} %{TIMESTAMP_ISO8601:mytimestamp} %{WORD} %{DATA} %{DATA} %{WORD} %{NUMBER:logid} --- \[%{USERNAME}\] %{DATA:test}: :%{GREEDYDATA:request}'
        }
      }
    }
    else {
      grok {
        match => {
          'message' => '%{UUID:id} %{NOTSPACE} %{TIMESTAMP_ISO8601:mytimestamp} %{WORD:test} :%{GREEDYDATA:request}'
        }
      }
    }

    json {
      source => "request"
      target => "parsedJson"
      remove_field => ["request"]
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
            "formatted_service_name" => "Interbank Query"
          }
        }
      }
      if [endpoint] =~ "INTRABANK" {
        mutate {
          add_field => {
            "host_call" => "10 601"
            "formatted_service_name" => "Intrabank Query"
          }
        }
      }

      if [endpoint] =~ 'remit-transfer' {
        mutate {
          add_field => {
            "host_call" => "10 111"
            "formatted_service_name" => "Remit Transfer"
          }
        }
      }

      if [endpoint] =~ 'fx-rates' {
        mutate {
          add_field => {
            "host_call" => "10 555"
            "formatted_service_name" => "FX Rates Query"
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
                event.set('formatted_service_name', map['formatted_service_name'])
        event.set('host_call',  event.get('host_call'))"
        map_action => "update"
        end_of_task => true
        timeout => 120
      }
    }
  }

  mutate {
    rename => ["host", "hostname"]
    convert => {
      "hostname" => "string"
    }
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
      index => "business_logstash_08-%{+YYYY.MM.dd}"
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
  }
}
```

#### Phase 10

Enaching Ruby skills by adding Names

Ruby `HostCall10415.rb`:

```
class HostCall10415  
	def initialize(event)  
		# Instance variables  
		@customer_id = event.get('customerid')
		transaction = event.get('transaction')
		@transaction_amount = transaction['amount']
		@transaction_currency = transaction['currency']
	end  
	def build_wbsp  
		customerId = @customer_id.ljust(15,' ')
		#smit
		puts customerId
		transactionAmount = build_amount()
		#1005000000000002
		puts transactionAmount
		#smit           1005000000000002
		return customerId + transactionAmount
	end 
	def build_amount
		string_amount = @transaction_amount
		puts string_amount #100.50

		splitted_amount = string_amount.split('.')
		puts splitted_amount 
		puts splitted_amount[0] #100
		puts splitted_amount[1] #50

		prefixed_amount = splitted_amount[0]
		puts prefixed_amount #100

		suffixed_amount = splitted_amount[1]
		puts suffixed_amount #50

		decimal_count = suffixed_amount.length
		puts decimal_count #2
		
		#1005000000000002
		string_build = string_amount.to_s.gsub('.','').ljust(15, '0') + decimal_count.to_s
		return string_build
	end
end
```

Ruby `main.rb`

```
require_relative 'HostCall10415'

def register(params)
    @drop_percentage = params["percentage"]
end

def filter(event)
    puts @drop_percentage
    header = event.get('header')
    puts header
    word = header['endpoint'].to_s
    
    if word.include? "pay"
        puts "in pay"
        m = HostCall10415.new(event)
        wbsp_format = m.build_wbsp
        puts wbsp_format
        event.set('wbsp_format', wbsp_format)
    end
    return [event]
end
```

logstash.conf

```
# Sample Logstash configuration for creating a simple
# Beats -> Logstash -> Elasticsearch pipeline.

input {
  kafka {
    bootstrap_servers => "localhost:9002"
    topics => "api-avro"
  }
}

 filter {
      json {
        source => "message"
      }
	ruby {
        # Cancel 90% of events
        path => "/usr/local/etc/logstash/main.rb"
        script_params => { "percentage" => 0.9 }
      }
    }


output {

stdout {
    codec => rubydebug
  }


  elasticsearch {
    hosts => ["http://localhost:9200"]
  }
}
```

Example input:

```
{"customerid":"smit","last_name":"shah","age":10,"height":10,"weight":100,"automated_email":false, "header": { "endpoint":"/pay"}, "transaction": { "amount":"100.50", "currency" : "SGD", "account": "12345"}}
```

Example Output:

```
smit           1005000000000002
```


cron
```
from datetime import datetime, timedelta
from threading import Timer
from elasticsearchdemo import generate_report


def init_seconds():
    global secs
    x = datetime.today()
    y = x.replace(day=x.day, hour=14, minute=2, second=0, microsecond=0)  # + timedelta(days=1)
    delta_t = y - x
    secs = delta_t.total_seconds()
    t = Timer(secs, hello_world())
    t.start()


def hello_world():
    print("Generating report")
    generate_report()
    # ...


init_seconds()
```

```
nohup python cron_runner.py &
```


Ref

https://medium.com/better-programming/getting-started-with-elasticsearch-in-java-spring-boot-d981c32b60b
