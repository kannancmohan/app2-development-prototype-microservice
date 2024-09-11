# Observability-Logging

Logging can be implemented

1. Using a scraping agent (eg grafana-promtail, Fluentd, FluentBit).
   
2. Using OTEL exporter to directly export logs to Log aggregation system like Loki, Elastic, Splunk etc
   ![High Level arch diagram](./images/springboot_observablity-tracing.jpg "Observability-Logging")

## Option 1: Using promtail agent to scrap logs from apps and pushing them to Loki
![High Level arch diagram](./images/springboot_observablity-logging-with-promtail.jpg "Observability-Logging with Promtail agent")

### Prerequisites

* Grafana
* Promtail
* Loki

### Application configuration

1. Add micrometer-tracing-bridge-otel to pom.xml

<table style='font-family:"Courier New", Courier, monospace; font-size:100%'>
    <tr>
        <th colspan="2">Dependencies</th>
    </tr>
    <tr>
        <th>Name</th>
        <th>Description</th>
    </tr>
    <tr>
        <td>opentelemetry-logback-mdc-1.0</td>
        <td>Used to add trace and span info to (standard)logs via Logback</td>
    </tr>
</table>

2. Update the logback.xml to /src/main/resources/

```
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} trace_id=%X{trace_id} span_id=%X{span_id} %msg%n</pattern>
    </encoder>
  </appender>
  <appender name="OTEL" class="io.opentelemetry.instrumentation.logback.mdc.v1_0.OpenTelemetryAppender">
    <appender-ref ref="STDOUT" />
  </appender>
  <root level="info">
    <appender-ref ref="OTEL" />
  </root>
</configuration>
```

### Checking app logs in grafana ui

```
login to grafana ui select 'Explore' from main menu
select 'Loki' as the source 
In 'Label filters' section, select 'app' from 'Select Label' drope-down and select your app-name from 'Select value' drope-down
Execute 'Run Query'
```

