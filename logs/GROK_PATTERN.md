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






##### References:

- [Available GROK Patterns](https://streamsets.com/documentation/datacollector/latest/help/datacollector/UserGuide/Apx-GrokPatterns/GrokPatterns_title.html)
- [GROK Pattern Debugger](https://grokdebug.herokuapp.com/)