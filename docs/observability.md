# Observability (logs, metrics, and traces)

## Prerequisites

* prometheus server

## Metrics setup for springboot3

### Application configuration

1. Add spring-boot-starter-actuator and micrometer-registry-prometheus dependency to pom.xml
   micrometer-registry-prometheus is used to export metrics collected by Micrometer to Prometheus
2. Update the application.xml

```
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

3. 

### 

