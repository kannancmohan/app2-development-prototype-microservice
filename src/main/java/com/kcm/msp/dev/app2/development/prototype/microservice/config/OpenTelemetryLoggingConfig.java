package com.kcm.msp.dev.app2.development.prototype.microservice.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.semconv.ResourceAttributes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Slf4j
@Configuration
// @ConditionalOnProperty("external.logging.enabled")
public class OpenTelemetryLoggingConfig {
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
        Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, applicationName));
    final SdkLoggerProviderBuilder builder =
        SdkLoggerProvider.builder().setResource(Resource.getDefault().merge(springResource));
    logRecordProcessors.orderedStream().forEach(builder::addLogRecordProcessor);
    return builder.build();
  }

  @Bean
  public LogRecordProcessor otelLogRecordProcessor() {
    return BatchLogRecordProcessor.builder(
            OtlpGrpcLogRecordExporter.builder()
                .setEndpoint("http://localhost:9095/v1/logs") // grpc enpoint of external log server
                .build())
        .build();
  }
}
