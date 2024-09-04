# app2-development-prototype-microservice

        Springboot3 microservice implementation using api first approach. 
        Project uses OpenApi(openapi-generator-maven-plugin) to generate client and server stub for given openapi spec.

## App tested with

        java 21 -(Corretto-21.0.3.9.1)
        maven 3.5.4+ -(3.9.7)
        spring-boot 3.3.2

## Project convention

### Git commit message

Git commit should follow the [conventionalcommits](https://www.conventionalcommits.org/en/v1.0.0/#summary) convention
There is a git pre-commit hook(commit-msg) configured to enforce this convention

### Code style:

The project uses spotless-maven-plugin to enforce style check on the following
* Java : Uses google java coding style
* POM :  enforce style check on pom.xml
* Markdown(*.md) : enforce style check on *.md files

Execute './mvnw spotless:check' to view code style violations and use './mvnw spotless:apply' to  manually apply coding style

## Project IDE initial setup

//TODO

## Project Setup and Configuring

### Setting the package name for the generated openapi client and server code

Add the following properties to the pom.xml
eg:

```
<generate-openapi-server-code.package>app2.development.prototype.microservice.server</generate-openapi-server-code.package>
<generate-openapi-client-code.package>app2.development.prototype.microservice.client</generate-openapi-client-code.package>
```

### Disabling OpenApi Client/Server code generation

Example for disabling client code generation, add the following in pom.xml

```
<plugin>
        <groupId>org.openapitools</groupId>
        <artifactId>openapi-generator-maven-plugin</artifactId>
        <executions>
                <!-- disable generation of openapi client code -->
                <execution>
                <id>generate-openapi-client-code</id>
                <phase/>
                </execution>
        </executions>
</plugin>
```

### Build and Start the application

#### Build application

```
./mvnw clean install

./mvnw clean install -Dspotless.skip=true # [To skip spotless check]

./mvnw clean install -Dskip.integration.test=true # [To skip integration test]
```

#### Code Format

```
./mvnw spotless:check [To view check style violations]
./mvnw spotless:apply [To apply check style fix to files]
```

#### Run application

```
./mvnw spring-boot:run
# [Remote debugging]
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
```

### Generate and Push Container Image

```
./mvnw clean install -U -Pjkube-build-push -Djkube.docker.username=<your-dockerhub-username> -Djkube.docker.password=<your-dockerhub-password>
```

eg: ./mvnw clean install -U -Pjkube-build-push -Djkube.docker.username=kannan2024 -Djkube.docker.password=1234

To manually pull and run container-image using docker

```
docker pull kannan2024/app2-development-prototype-microservice
docker run -d -p 8881:8881 kannan2024/app2-development-prototype-microservice:latest 
```

## OpenApi spec

        http://localhost:8881/v3/api-docs

## swagger ui endpoint

        http://localhost:8881/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config

## spring actuator

        http://localhost:8881/actuator/health

