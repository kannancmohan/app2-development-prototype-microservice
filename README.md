# app-development-prototype-microservice

        springboot3 microservice implementation using api first approach

## supported versions

        java 22 -(Corretto-22.0.2.9.1)
        maven 3.5.4+ -(3.9.7)
        spring-boot 3.3.2

## Project convention

### Git commit message

Git commit should follow the [conventionalcommits](https://www.conventionalcommits.org/en/v1.0.0/#summary) convention
There is a git pre-commit hook(commit-msg) configured to enforce this convention

### Java code style:

should follow google java coding style and project uses spotless-maven-plugin to enforce this

## Project IDE initial setup

## Project setup

        Add the following properties for genearting open-api client and server code 
        1. generate-openapi-server-code.package  - for defining java package for the openapi generated server code
        2. generate-openapi-client-code.package  - for defining java package for the openapi generated client code

## Build and Start the application

        Build application 
            * ./mvnw clean install
            * ./mvnw clean install -Dspotless.skip=true [To skip spotless check]
            * ./mvnw clean install -Dskip.integration.test=true [To skip integration test]
        Code Format
            * ./mvnw spotless:check [To view check style violations]
            * ./mvnw spotless:apply [To apply check style fix to files]
        Run application using 
            * ./mvnw spring-boot:run

## OpenApi spec

        http://localhost:8881/v3/api-docs

## swagger ui endpoint

        http://localhost:8881/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config

## spring actuator

        http://localhost:8881/actuator/health

