# Spring Boot, Consult and Vault Demo

## Prerequisites

> This demo has been tested on Openshift 4.2.

* [OpenShift CLI](https://docs.openshift.com/container-platform/4.2/cli_reference/openshift_cli/getting-started-cli.html#cli-installing-cli_cli-developer-commands)
* [git CLI^](https://git-scm.com/downloads)
* [Helm](https://github.com/helm/helm)
* [Vault](https://www.vaultproject.io/docs/install/index.html)
* [Maven](https://maven.apache.org/install.html)

## Install HashiCorp Consul on Openshift

Clone the consul-helm project and set up the configuration.

```
git clone https://github.com/hashicorp/consul-helm.git
cd consul-helm
cat <<EOF > helm-consul-values.yaml
# Choose an optional name for the datacenter
global:
  datacenter: minidc
# Enable the Consul Web UI via a NodePort
ui:
  service:
    type: 'NodePort'
# Enable Connect for secure communication between nodes
connectInject:
  enabled: true
client:
  enabled: true
  grpc: true
# Use only one Consul server for local development
server:
  replicas: 1
  bootstrapExpect: 1
  disruptionBudget:
    enabled: true
    maxUnavailable: 0
EOF
```

Then, deploy Consul on OpenShift

```
oc login
oc new-project hashicorp-consul
oc adm policy add-scc-to-user privileged -z redhat-consul-server -n hashicorp-consul
helm install redhat . -f helm-consul-values.yaml -n hashicorp-consul
oc expose svc/redhat-consul-server -n hashicorp-consul
```

Finally, check your deployment by getting the Consul URL with the following command and entering it in your WebBrowser

```
echo http://$(oc get route redhat-consul-server -n hashicorp-consul -o jsonpath='{.spec.host}{"\n"}')
```

## Install HashiCorp Vault on Openshift

Deploy Vault on OpenShift

```
oc login
oc new-project hashicorp-vault
oc adm policy add-scc-to-user anyuid -z vault-sa -n hashicorp-vault
oc apply -f https://raw.githubusercontent.com/mcouliba/spring-consult-vault-demo/master/openshift/vault-deployment.yaml -n hashicorp-vault
oc create route reencrypt vault --port=8200 --service=vault -n hashicorp-vault
```

Then, check your deployment by getting the Consul URL with the following command and entering it in your WebBrowser

```
echo https://$(oc get route vault -n hashicorp-vault -o jsonpath='{.spec.host}{"\n"}')
```

Next, initialize Vault
```
export VAULT_ADDR=https://$(oc get route vault -n hashicorp-vault -o jsonpath='{.spec.host}{"\n"}')
vault operator init -tls-skip-verify -key-shares=1 -key-threshold=1
```

You should have the following output

```
Unseal Key 1: 85owGwPfHlgx/fod7ONDnha75Kg9Nrgz4pjtLayeKTc=

Initial Root Token: s.hwdzCUcVdjy3fOmp0gkC1xeP
[...]
```

Unseal Vault and login
```
vault operator unseal -tls-skip-verify <Unseal Key 1>
vault login <Initial Root Token>
```

As result, you obtain

```
Success! You are now authenticated. The token information displayed below
is already stored in the token helper. You do NOT need to run "vault login"
again. Future Vault requests will automatically use this token.
```

Finally, let's create a token for our application

```
export VAULT_TOKEN=<Initial Root Token>
vault token create -id="00000000-0000-0000-0000-000000000000" -policy="root"
```

## Configure Application Secrets within Vault

Create different application secrets containing **message.greeting** value

```
vault kv put secret/japanese-service message.greeting='Kon''nichiwa, watashinonamaeha ${spring.application.name}! Hajimemashite!'
vault kv put secret/english-service message.greeting='Hello, my name is ${spring.application.name}! Nice to meet you!'
vault kv put secret/italian-service message.greeting='Ciao, mi chiamo ${spring.application.name}! Piacere di conoscerti!'
vault kv put secret/french-service message.greeting='Bonjour, je m''appelle ${spring.application.name}! Content de te rencontrer!'
```

## Run the demo

Clone the current project

```
git clone https://github.com/mcouliba/spring-consult-vault-demo.git
cd spring-consult-vault-demo
```

Update the **message-service/src/main/resources/bootstrap.yaml** file as following to connect our application to Consul and Vault Server:

```
spring:
  cloud:
    consul:
      host: redhat-consul-server.hashicorp-consul.svc
      port: 8500
      discovery:
        healthCheckPath: /actuator/health
        healthCheckInterval: 20s
    vault:
      authentication: TOKEN
      scheme: https
      host: <OpenShift Vault Host>
      port: 443
      token: 00000000-0000-0000-0000-000000000000
```

> **OpenShift Vault Host** = $(oc get route vault -n hashicorp-vault -o jsonpath='{.spec.host}{"\n"}')


Create the application project/namespace

```
oc login 
oc new-project sbcv-demo
oc policy add-role-to-user view -n sbcv-demo -z default
```

Build the **message-service** image

```
cd message-service
oc new-build java --name=message-service --binary=true -n sbcv-demo
./mvnw clean package
oc start-build message-service --from-file=target/message-service-0.0.1-SNAPSHOT.jar --follow
```

Then, deploy the **japanese-service** application

```
oc new-app sbcv-demo/message-service:latest --name=japanese-service  --env=spring.application.name=japanese-service -n sbcv-demo -l app=demo,app.kubernetes.io/instance=japanese-service,app.kubernetes.io/name=java,app.kubernetes.io/part-of=demo
oc expose svc/japanese-service
```

Next, test your service

```
curl -w "\n" http://$(oc get route japanese-service -n sbcv-demo -o jsonpath='{.spec.host}{"\n"}')/api/greeting
```

Finally, deploy the other applications (italian-service, english-service and french-service)

```
oc new-app sbcv-demo/message-service:latest --name=italian-service  --env=spring.application.name=italian-service -n sbcv-demo -l app=demo,app.kubernetes.io/instance=italian-service,app.kubernetes.io/name=java,app.kubernetes.io/part-of=demo
oc expose svc/italian-service
```

```
oc new-app sbcv-demo/message-service:latest --name=english-service  --env=spring.application.name=english-service -n sbcv-demo -l app=demo,app.kubernetes.io/instance=english-service,app.kubernetes.io/name=java,app.kubernetes.io/part-of=demo
oc expose svc/english-service
```

```
oc new-app sbcv-demo/message-service:latest --name=french-service  --env=spring.application.name=french-service -n sbcv-demo -l app=demo,app.kubernetes.io/instance=french-service,app.kubernetes.io/name=java,app.kubernetes.io/part-of=demo
oc expose svc/french-service
```
