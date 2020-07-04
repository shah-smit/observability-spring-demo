#Filebeat setting filebeat
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