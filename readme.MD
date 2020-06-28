## Observability Using ELK

The whole setup is on MacOS v10.15.5 (19F101)

###Phase 1

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



###Phase 2

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
      tags: ["observability"]
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