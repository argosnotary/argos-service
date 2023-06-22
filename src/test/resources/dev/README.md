

# change initial keycloak config

```
docker run -p 8080:8080 \
           -w /opt/keycloak \
           -v $PWD/keycloak/import:/opt/keycloak/data/import \
           -e KEYCLOAK_ADMIN=admin \
           -e KEYCLOAK_ADMIN_PASSWORD=admin \
           quay.io/keycloak/keycloak:19.0.3 \
             --verbose \
             start-dev \
               -Dkeycloak.migration.action=import \
               -Dkeycloak.migration.provider=dir \
               -Dkeycloak.migration.dir=/opt/keycloak/data/import
```

define realms
saprovider
oauth-stub

redirects
http://localhost:8080/login/oauth2/code/oauth-stub
http://localhost:8080/login/oauth2/code/saprovider

```
# export realms
docker exec -ti xxx /opt/keycloak/bin/kc.sh export --dir /opt/keycloak/data/import/ --users realm_file
```