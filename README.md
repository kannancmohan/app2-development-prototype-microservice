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

## To completely disable springboot-security

* Remove "spring-boot-starter-security" & "spring-security-test" from pom.xml
* delete the SecurityConfig and SecurityConfigIntegrationTest
* delete CorsProperty and its reference in class and application.xml

## (optional) Keycloak setup for testing locally

### Install keycloak(version: 25.0.4) using docker

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

* Go to the Users section and create users(test-User1@test.com,test-Admin1@test.com).
* Assign appropriate roles to the newly created users by editing the user, going to the Role Mappings tab, and assigning the appropriate role(s)

#### Check user login

* Login to [Keycloak Account console](http://localhost:8080/realms/app2-realm/account)

## (optional) Zitadel setup for testing locally

### Install Zitadel(version: 2.49.1) using docker

```
# Download the docker compose example configuration.
wget https://raw.githubusercontent.com/zitadel/zitadel/main/docs/docs/self-hosting/deploy/docker-compose.yaml

# Run the database and application containers.
docker compose up --detach
```

### Default zitadel admin user

http://localhost:8080/ui/console
username: zitadel-admin@zitadel.localhost
password: Password1!

### Configure Zitadel

#### Create a new project and application in zitadel

1. Select " Projects Tab" and click "Create new project"
2. Add a new name for the project eg: homelab
3. In the newly created project page make sure the following 2 items are enabled and click "save"

   ```
   Assert Roles on Authentication
   Check authorization on Authentication
   ```
4. In the same project page click the icon "New" to create a new application
5. Add a name for the new application. eg: webapp and click continue
6. In the authentication method selections page, select  "CODE" and click continue
7. [Optional] add the "redirectUrl" and click continue (eg redirect url for testing: https://localhost)
8. Click "create" to finalize the configurations
9. A new window pops with the clientId and clientSecret. copy the values to safe location
10. In the newly created application page , select "Token settings" from the right panel
11. In the token settings sections, select "JWT" from 'Auth Token Type' dropdown
12. Make sure the following 3 items are enabled and click "save"

    ```
    Add user roles to the access token
    User roles inside ID Token
    User Info inside ID Token
    ```

#### create new roles in zitadel

1. Select the project you have created previously and click the "roles" setting from right panel
2. Select "New" and create a new role with the details for new role
   eg:

   ```
   Key: administrator
   Display name: administrator
   Group: administrator
   ```

   you can create two roles administrator and users

#### create users and assign user roles

1. Select "users" tab from the top navigation and click "new" to create a new user
2. Fill the user details and click create (eg users: test-User1@test.com and test-Admin1@test.com)
3. To add users to role , navigate back to project page and select "Authorizations" from the right panel
4. Click new and add the select user and click continue and select the role for the user and click "save"

#### Check user login

* Login to [User console](http://localhost:8080/ui/console)

## Manually check authentication and authorization

### For keycloak Auth Server

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

### For Zitadel Auth Server

#### Generate access token using postman

* create a new request and select 'Authorization' tab
* select 'OAuth2' as type from dropdown
* Configure the following in "Configure New Token" section

  ```
  Callback URL: https://localhost
  Auth URL: https://zitadel.dev.local/oauth/v2/authorize
  Access Token URL: https://zitadel.dev.local/oauth/v2/token
  Client ID: <your-client-id>
  Client Secret: <your-client-secret>
  Scope: openid profile email
  ```

#### Access restricted endpoint using bearer token

```
curl -X 'GET' \
  'http://localhost:8881/users?limit=10' \
  -H 'accept: application/json' \
  -H 'Authorization: Bearer <access-token>'

```

