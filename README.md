# Spring Boot, Consult and Vault Demo

## Install HashiCorp Consul on Openshift

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

```
oc login
oc new-project hashicorp-consul
oc adm policy add-scc-to-user privileged -z redhat-consul-server -n hashicorp-consul
helm install redhat . -f helm-consul-values.yaml -n hashicorp-consul
oc expose svc/redhat-consul-server -n hashicorp-consul
```

## Install HashiCorp Vault on Openshift

```
oc login
oc new-project hashicorp-vault
oc adm policy add-scc-to-user anyuid -z vault-sa -n hashicorp-vault
oc apply -f https://raw.githubusercontent.com/lbroudoux/secured-fruits-catalog-k8s/master/k8s/vault-deployment.yml -n hashicorp-vault
oc create route reencrypt vault --port=8200 --service=vault -n hashicorp-vault
```

```
export VAULT_ADDR=https://$(oc get route vault -n hashicorp-vault --no-headers -o custom-columns=HOST:.spec.host)
vault operator init -tls-skip-verify -key-shares=1 -key-threshold=1
```

```
Unseal Key 1: qA7kjOPajVCDBmWsuHUcC5kht/CA+rnhWEDpXTelMRE=

Initial Root Token: s.wQ1TgNsVzsogCGn9jtjxDfBB
[...]
```

```
vault operator unseal -tls-skip-verify qA7kjOPajVCDBmWsuHUcC5kht/CA+rnhWEDpXTelMRE=
vault login s.wQ1TgNsVzsogCGn9jtjxDfBB
```

```
Success! You are now authenticated. The token information displayed below
is already stored in the token helper. You do NOT need to run "vault login"
again. Future Vault requests will automatically use this token.
```

## Run the demo

```
$ oc login 
$ oc new-project sbcv-demo
```
