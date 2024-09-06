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
  metrics:
    distribution:
      percentiles-histogram:
        http:
          server:
            requests: 'true'
    tags:
      application: app2-microservice
```

3. To expose metrics to prometheus(k8s managed)
   create a ServiceMonitor resource in infra/k8s/helm/templates

```
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: app2-microservice-monitor
  labels:
    release: app2-microservice
spec:
  jobLabel: app2-microservice
  selector:
    matchLabels:
      app: app2-microservice
  endpoints:
    - port: http # Name of the port defined in Service resource
      path: /actuator/prometheus
      interval: 30s
  namespaceSelector:
    matchNames:
      - app2-microservice
```

### Troubleshooting & Testing the metrics in prometheus ui

1. to verify if app is available as a target for prometheus to scrap

```
login to prometheus ui and navigate to Status>Targets
From dropdown select the app and click "show more" to check for status and for any error's
```

2. check service discovery if app is present

```
login to prometheus ui and navigate to Status> Service Discovery
Search the app and click "show more" to check for Discovered Labels and Target Labels
```

3. Check if metrics value using Graph Query

```
login to prometheus ui and navigate to Graph
Use query "http_server_requests_seconds_count{application="app2-microservice"}" and select Execute
You should see some result
```

### Check metrics in grafana ui

```
login to grafana ui select 'Explore' from main menu
select '' as the source 
In the label filters drope-down select 'application' 
After selecting 'application' the adjacent drope-down will list the available apps
select your app name and execute click 'Run Query' to see results  
```

