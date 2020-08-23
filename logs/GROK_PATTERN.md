### GROK Patterns Examples


#### Log Type with JSON

Sample Log:
```
2020-08-19 11:38:31.651  INFO 54221 --- [nio-8080-exec-1] c.e.o.controller.HomeController          : GET Greeting { "customerId": "S9472394C00", "dob":"23022010"}
```

GROK Pattern:
```
%{DATESTAMP:mytimestamp}%{SPACE}%{WORD}%{SPACE}%{NUMBER} --- %{NOTSPACE} %{NOTSPACE}%{SPACE}:%{SPACE}%{WORD}%{SPACE}%{WORD}%{SPACE}%{GREEDYDATA:finaljson}
```

Sample Log:
```
d2d24ee7-2380-4044-8eb7-0aeffbd122ef APP/PROC/WEB/0 2020-08-18T05:51:16.659126547Z OUT 2020-08-18 05:51:16.658  INFO 14 --- [nio-8080-exec-4] ChassisLogger                            : Request headers: host=[test.cf.uat.dbs.com], user-agent=[IBSG], accept=[text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2], actionid=[actionId], authorization=[Bearer eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiYi9PNk92VnYxK3krV2dySDVVaTlXVGlvTHQwPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJiZDZkMjEwNi1jNGU1LTQxZTEtYTc3ZC1kMzI1MGFiZmE2ZDciLCJhdWRpdFRyYWNraW5nSWQiOiIxY2NiOWYwYi05NWY3LTQ2MjUtYTBiYS00YWNiOTM5YWY5YzIiLCJpc3MiOiJodHRwczovL3gwMXNjaWFtc2dhbTFhLnZzaS51YXQuZGJzLmNvbTo3NDQzL2FtL29hdXRoMi9hcGljbGllbnQiLCJ0b2tlbk5hbWUiOiJhY2Nlc3NfdG9rZW4iLCJ0b2tlbl90eXBlIjoiQmVhcmVyIiwiYXV0aEdyYWUiOiJjbGllbnRfY3JlZGVudGlhbHMiLCJzY29wZSI6WyJjaWFtIl0sImF1dGhfdGltZSI6MTU5NzcyNDY4NywicmVhbG0iOiIvYXBpY2xpZW50IiwiZXhwIjoxNTk3NzM5MDg3LCJpYXQiOjE1OTc3MjQ2ODcsImV4cGlyZXNfaW4iOjE0NDAwLCJqdGkiOiJmNDkzNTNjNi1kOGZhLTQ4NGUtODBjOS1iNjBhNTU5NWIxMDEifQ.CK0RtXMCN8op5xv5KacGwwzd3BFgm6FnrSI0RNGWFkHP9iOy8OgVlPGDrMoKGFkFyBnCdSNju-I7Hy8id5pctLfca9HT_Z_ZMR-rV9x5rQOzAVOQoRu-pZ2jzXd-2WNoJ042roYMaKsR6a3U55YrXR21cHXn3ZwnH26b_iMkm8PvAuplmJj18Qt8ajK9n0HUT6GRUDKknbUTfXxkA_Zr-9DKS9g_viDzTnKEXgoQoQCSrk4O0I2cB97W2hWdmvVBU1fnxfgdmN8ijNIwfr0NRPBlU8cyGrZQQbB5iLVFzNLW-nOlD3ZpXou4UYUB9FPLHSSrE6mgyZxblpbe_vOvZg], authtype=[2FA], b3=[8696cce20de3b8c2-8696cce20de3b8c2], channelid=[DIB], client_id=[104647989], content-type=[application/json], customerid=[00X7756779H], locale=[en], timeout=[500], x-b3-spanid=[8696cce20de3b8c2], x-b3-traceid=[8696cce20de3b8c2], x-cf-applicationid=[d2d24ee7-2380-4044-8eb7-0aeffbd122ef], x-cf-instanceid=[3c6df583-a23e-49c3-41ca-959d], x-cf-instanceindex=[0], x-correlationid=[104647989], x-version=[1.0.0], x-forwarded-proto=[https], x-request-start=[1597729876647], x-vcap-request-id=[510fa118-d316-4ef7-4b91-4c9fe11f2999]
```

GROK Pattern:
```
%{UUID:id} %{NOTSPACE} %{TIMESTAMP_ISO8601:mytimestamp} %{WORD:type} %{DATA} %{DATA} %{WORD:logtype} %{NUMBER:logid} --- \[%{USERNAME}\] %{WORD:test}%{SPACE}:%{SPACE}%{DATA:datatype}: host=\[%{HOSTNAME:host}\], user-agent=\[%{WORD:useragent}\], accept=\[%{DATA:accept}\], actionid=\[%{DATA:actionid}\], authorization=\[%{DATA:authorization}\], channelid=\[%{DATA:channelid}\], client_id=\[%{DATA:client_id}\], content-type=\[%{DATA:contentType}\], customerid=\[%{DATA:customerid}\], locale=\[%{DATA:locale}\], timeout=\[%{DATA:timeout}\], x-b3-spanid=\[%{DATA:spanid}\], x-b3-traceid=\[%{DATA:traceid}\], x-cf-applicationid=\[%{DATA}\], x-cf-instanceid=\[%{DATA}\], x-cf-instanceindex=\[%{DATA}\], x-correlationid=\[%{DATA}\], x-version=\[%{DATA:version}\], x-forwarded-proto=\[%{DATA}\], x-request-start=\[%{DATA}\], x-vcap-request-id=\[%{DATA}\]
```

Sample Log:
```
d2d24ee7-2380-4044-8eb7-0aeffbd122ef APP/PROC/WEB/0 2020-08-18T05:51:16.659757253Z OUT 2020-08-18 05:51:16.659 INFO 14 --- [nio-8080-exec-4] c.d.c.l.ChassisGrafanaLogger             : :{"timestamp_log":"2020-08-18 05:51:16,658","type":"payee-management-service","appCode":"MDSG","country":"SG","environment":"uat","serviceID":"GET_CustomerPayee","endpoint":"/payments/payees/payee-types/INTERBANK","request_type":"GET","msg_uid":"104647989","client_ip":"","statusCode":"","error classification":"","error_type":"","error_code":"","traceId":"8696cce20de3b8c2","spanId":"8696cce20de3b8c2","interface_map":"EAPI","functionalMap":"","externalMessageId":"","execution_time_milliseconds":"","api_type":"request","x-request-ref-id":"202008181351168696CC"}
```

GROK Pattern:
```
%{UUID:id} %{NOTSPACE} %{TIMESTAMP_ISO8601:mytimestamp} %{WORD:type} %{WORD:logtype} %{NUMBER:logid} --- \[%{USERNAME}\] %{DATA:test}: :%{GREEDYDATA:graphana_request_logger}
```

Sample Log:
```
d2d24ee7-2380-4044-8eb7-0aeffbd122ef APP/PROC/WEB/0 2020-08-18T05:51:16.659757253Z OUT 2020-08-18 05:51:16.659 INFO 14 --- [nio-8080-exec-4] c.d.c.l.ChassisGrafanaLogger             : :{"timestamp_log":"2020-08-18 05:51:16,658","type":"payee-management-service","appCode":"MDSG","country":"SG","environment":"uat","serviceID":"GET_CustomerPayee","endpoint":"/payments/payees/payee-types/INTERBANK","request_type":"GET","msg_uid":"104647989","client_ip":"","statusCode":"","error classification":"","error_type":"","error_code":"","traceId":"8696cce20de3b8c2","spanId":"8696cce20de3b8c2","interface_map":"EAPI","functionalMap":"","externalMessageId":"","execution_time_milliseconds":"","api_type":"response","x-request-ref-id":"202008181351168696CC"}
```

GROK Pattern:
```
%{UUID:id} %{NOTSPACE} %{TIMESTAMP_ISO8601:mytimestamp} %{WORD} %{DATA} %{DATA} %{WORD} %{NUMBER:logid} --- \[%{USERNAME}\] %{DATA:test}: :%{GREEDYDATA:graphana_request_logger}
```




##### References:

- [Available GROK Patterns](https://streamsets.com/documentation/datacollector/latest/help/datacollector/UserGuide/Apx-GrokPatterns/GrokPatterns_title.html)
- [GROK Pattern Debugger](https://grokdebug.herokuapp.com/)