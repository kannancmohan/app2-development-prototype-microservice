## To completely disable/remove springboot-security

* Remove "spring-boot-starter-security" & "spring-security-test" from pom.xml
* delete the SecurityConfig.java and SecurityConfigIntegrationTest.java
* delete CorsProperty.java and its reference in class and application.xml
* Optionally delete the auth references(addSecuritySchemes) in SpringdocOpenApiConfig.java

## To completely disable/remove authorization using jwt bearer token

* Remove "spring-boot-starter-oauth2-resource-server" & "spring-cloud-contract-wiremock" from pom.xml
* remove oauth2ResourceServer method call and the corresponding custom methods that it use from SecurityConfig.java
* delete JwtUtil and the Jwt tests from SecurityConfigIntegrationTest.java
* Optionally delete the auth references(addSecuritySchemes) in SpringdocOpenApiConfig.java

## Keycloak setup for testing jwt bearer auth locally (optional)

### Install keycloak(version: 25.0.4) using docker

```
docker run --detach -p 8080:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:25.0.4 start-dev
```

### Configure Keycloak

#### Create a Realm

* In the [Keycloak Admin Console](http://localhost:8080/admin) , click on "Create Realm" and give it a name (e.g., homelab).

#### Create a Client(Clients are applications and services that can request authentication of a user)

* Go to the Clients section and click on "Create"
* Set the Client ID (e.g., restclient), Client Protocol to openid-connect
* Set the Authentication flow. select "Standard Flow" and "Direct access grants"

#### create roles necessary for the project

* Go to "Realm Roles" section and click on "Create role"
* create roles (e.g., ADMIN_ROLE, USER_ROLE)

#### Create Users and Assign Roles

* Go to the Users section and create users(test-User1@test.com,test-Admin1@test.com).
* Assign appropriate roles to the newly created users by editing the user, going to the Role Mappings tab, and assigning the appropriate role(s)
* Set user password from Credential tab

#### Check user login

* Login to [Keycloak Account console](http://localhost:8080/realms/homelab/account)

## Zitadel setup for testing jwt bearer auth locally (optional)

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
5. Add a name for the new application. eg: restclient and click continue
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
3. Create two roles (eg: ADMIN_ROLE, USER_ROLE)
   eg:

   ```
   Key: ADMIN_ROLE
   Display name: admin
   ```

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
curl --location --request POST 'http://localhost:8080/realms/homelab/protocol/openid-connect/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'username=test-User1@test.com' \
--data-urlencode 'password=test-User1@test.com' \
--data-urlencode 'grant_type=password' \
--data-urlencode 'client_id=restclient'
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

