# app-development-prototype-microservice

        Springboot3 microservice implementation using api first approach. 
        Project uses OpenApi(openapi-generator-maven-plugin) to generate client and server stub for given openapi spec.

## supported versions

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

## To completely disable springboot-security

* Remove "spring-boot-starter-security" & "spring-security-test" from pom.xml
* delete the SecurityConfig and SecurityConfigIntegrationTest
* delete CorsProperty and its reference in class and application.xml

## Keycloak setup (optional) for manual test

### Install keycloak(version: 25.0.4)

```
docker run -p 8080:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:25.0.4 start-dev
```

### Configure Keycloak

#### Create a Realm

* In the [Keycloak Admin Console](http://localhost:8080/admin) , click on "Create Realm" and give it a name (e.g., app2-realm).

#### Create a Client(Clients are applications and services that can request authentication of a user)

* Go to the Clients section and click on "Create"
* Set the Client ID (e.g., app2-client), Client Protocol to openid-connect
* Set the Authentication flow. select "Standard Flow" and "Direct access grants"

#### create roles necessary for the project

* Go to "Realm Roles" section and click on "Create role"
* create roles (e.g., app2admin-role, app2user-role)

#### Create Users and Assign Roles

* Go to the Users section and create users(test-user,test-admin).
* Assign appropriate roles to the newly created users by editing the user, going to the Role Mappings tab, and assigning the appropriate role(s)

#### Check user login

* Login to [Keycloak Account console](http://localhost:8080/realms/app2-realm/account)

### Manually check authentication and authorization

#### Generate access token

```
curl --location --request POST 'http://localhost:8080/realms/app2-realm/protocol/openid-connect/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'username=test-user' \
--data-urlencode 'password=test-user' \
--data-urlencode 'grant_type=password' \
--data-urlencode 'client_id=app2-client'
```

#### Access restricted endpoint using bearer token

```
curl -X 'GET' \
  'http://localhost:8881/users?limit=10' \
  -H 'accept: application/json' \
  -H 'Authorization: Bearer <access-token>'

```

