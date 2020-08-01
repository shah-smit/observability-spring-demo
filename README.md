## Observability Using ELK

The whole setup is on MacOS v10.15.5 (19F101)

### Table of Content

- [Phase 1](#Phase-1) `SpringBoot <> LogStash <> Elastic Search <> Kibana`
- [Phase 2](#Phase-2) `SpringBoot <> Filebeat <> LogStash <> Elastic Search <> Kibana`
- [Phase 3](#Phase-3) `SpringBoot <> Filebeat / Kafka <> LogStash <> Elastic Search <> Kibana`
- [Phase 4](#Phase-4) `Enchanged Phase 3`
- [Phase 5](#Phase-5) `SpringBoot <> Filebeat / Kafka <> LogStash <> Elastic Search #1 / Elastic Search #2  <> Kibana`
- [Phase 6](#Phase-6) `Extracting fields from log line using Substring`


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

