# Observability-Logging

Logging can be implemented

Option 1: Using a scraping agent (eg grafana-promtail, Fluentd, FluentBit).

Option 2: Using OTEL exporter to directly export logs to Log aggregation system like Loki, Elastic, Splunk etc

Note: This code base implements option1 and a sample implementation for option2 can be found in [github branch](https://github.com/kannancmohan/app2-development-prototype-microservice/tree/opentele-logging-external)

## Option 1: Using promtail agent to scrap logs from apps and pushing them to Loki

![High Level arch diagram](./images/springboot_observablity-logging-with-promtail.jpg "Observability-Logging with Promtail agent")

### Prerequisites

* Grafana
* Promtail
* Loki

### Application configuration

1. Add opentelemetry-logback-mdc-1.0 to pom.xml

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

2. Update /src/main/resources/logback.xml

```
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} trace_id=%X{trace_id} span_id=%X{span_id} %msg%n</pattern>
    </encoder>
  </appender>

  <appender name="OTEL_STDOUT" class="io.opentelemetry.instrumentation.logback.mdc.v1_0.OpenTelemetryAppender">
    <appender-ref ref="STDOUT" />
  </appender>
  <root level="info">
    <appender-ref ref="OTEL_STDOUT" />
  </root>
</configuration>
```

### Checking logs in grafana ui

```
login to grafana ui select 'Explore' from main menu
select 'Loki' as the source 
In 'Label filters' section, select 'app' from 'Select Label' drope-down and select your app-name from 'Select value' drope-down
Execute 'Run Query'
```

## Option 2: Using OTEL exporter to push logs to Loki

![High Level arch diagram](./images/springboot_observablity-logging-with-otlp-exporter.jpg "Observability-Logging")

### Prerequisites

* Grafana
* Loki version 3.0.0 or higher(with native OTEL support)

### Application configuration

1. Add opentelemetry-logback-appender-1.0 to pom.xml

<table style='font-family:"Courier New", Courier, monospace; font-size:100%'>
    <tr>
        <th colspan="2">Dependencies</th>
    </tr>
    <tr>
        <th>Name</th>
        <th>Description</th>
    </tr>
    <tr>
        <td>opentelemetry-logback-appender-1.0</td>
        <td>A Logback appender that automatically sends logs to an OpenTelemetry-compatible backend</td>
    </tr>
    <tr>
        <td>(Optional- Add if already missing in pom)opentelemetry-exporter-otlp</td>
        <td>Used to export telemetry data(traces, metrics & logs) to OpenTelemetry Collector or directly to a backend that supports otlp</td>
    </tr>
    <tr>
        <td>(Optional- Add this when exporting only logs)opentelemetry-exporter-otlp-logs</td>
        <td>Used to export logs to OpenTelemetry Collector or directly to a backend log server that supports otlp</td>
    </tr>

</table>

2. Add custom configuration

```
  @Bean
  public OpenTelemetry openTelemetry(
      final SdkLoggerProvider sdkLoggerProvider,
      final SdkTracerProvider sdkTracerProvider,
      final ContextPropagators contextPropagators) {
    final var openTelemetrySdk =
        OpenTelemetrySdk.builder()
            .setLoggerProvider(sdkLoggerProvider)
            .setTracerProvider(sdkTracerProvider)
            .setPropagators(contextPropagators)
            .build();
    OpenTelemetryAppender.install(openTelemetrySdk);
    return openTelemetrySdk;
  }

  @Bean
  public SdkLoggerProvider otelSdkLoggerProvider(
      final Environment environment, final ObjectProvider<LogRecordProcessor> logRecordProcessors) {
    final var applicationName = environment.getProperty("spring.application.name", "application");
    final Resource springResource =
        Resource.create(Attributes.of(AttributeKey.stringKey("service.name"), applicationName));
    final SdkLoggerProviderBuilder builder =
        SdkLoggerProvider.builder().setResource(Resource.getDefault().merge(springResource));
    logRecordProcessors.orderedStream().forEach(builder::addLogRecordProcessor);
    return builder.build();
  }

  @Bean
  public LogRecordProcessor otelLogRecordProcessor() {
    // use OtlpHttpLogRecordExporter or OtlpHttpLogRecordExporter based on backend system
    return BatchLogRecordProcessor.builder(
            OtlpHttpLogRecordExporter.builder()
                .setEndpoint(
                    "http://localhost:3100/otlp/v1/logs") // enpoint of external log server
                .build())
        .build();
  }
```

3. Update /src/main/resources/logback.xml to include OTEL_EXTERNAL appender

```
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <appender name="OTEL_EXTERNAL" class="io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender">
    <captureExperimentalAttributes>true</captureExperimentalAttributes>
    <captureKeyValuePairAttributes>true</captureKeyValuePairAttributes>
  </appender>
  <root level="info">
    <appender-ref ref="OTEL_STDOUT" />
    <appender-ref ref="OTEL_EXTERNAL" />
  </root>
</configuration>
```

### Checking logs in grafana ui

```
login to grafana ui select 'Explore' from main menu
select 'Loki' as the source 
In 'Label filters' section, select 'app' from 'Select Label' drope-down and select your app-name from 'Select value' drope-down
Execute 'Run Query'
```

