# Deployd

[![Build Status](https://travis-ci.org/datawire/deployd.svg?branch=master)](https://travis-ci.org/datawire/deployd)
[![Docker Stars](https://img.shields.io/docker/stars/datawire/deployd.svg)](https://hub.docker.com/r/datawire/deployd)

Every micro service has its own unique set of runtime configuration: which database to use, whether to deploy in an auto-scaling group, and so forth.

* Deployd enables *developers* to specify the runtime configuration in a YAML file.
* Deployd enables *ops engineers* to define the runtime configuration parameters exposed to developers

These two capabilities enable developers and operations to automatically provision and deploy a microservice to a specific cloud environment using deployd.

Deployd is currently designed for Kubernetes.

## WARNING: Prototype Software!

This is prototype software! There is little to no tests and the APIs **WILL** change as exploratory development continues. There is no guarantee this project will continue either.

## Getting Started

### Prerequisites

1. An [Amazon Web Services ("AWS")](https://aws.amazon.com/) account and active AWS API credentials to use Deployd.
2. [Minikube](https://github.com/kubernetes/minikube) is installed and works locally.

### Deploying Locally

Deployd can be run locally for testing and experimentation!

#### Start Minikube

Lines the start with `$>` are commands that should be run.

```bash
$> minikube start
Starting local Kubernetes cluster...

# wait for this output
Kubectl is now configured to use the cluster.
```

#### Deploy Deployd

Starting and stopping Deployd is painless:

```bash
# startup the server
bin/setup-all.sh

# shutdown the server
bin/teardown-all.sh
```

#### Create a World

A world is a named place that exposes cloud providers (e.g. AWS) and ultimately [capabilities](#docs/design.md). Worlds also track the services and infrastructure contained within for the purpose of operational visibility into how the system is constructed.

Worlds are expected to be created by an operations focused member of your team and most developers unless operating as both developer and operator are not likely to touch the "Worlds" functionality very often.

```bash
$> cp src/test/resources/worlds/world.json ./world.json

# open example.json in your favorite editor and find these two keys in the JSON
#
# "accessKey": null
# "secretKey": null
#
# edit them to reflect your AWS API credential values, for example:
#
# "accessKey": "foobar"
# "secretKey": "bazbot"
#
# next find the `network` object.
#
# "network" {
#   "id" null,
#   "subnets": []
# }
#
# edit `id` with an existing VPC identifier then update `subnets` with at least one subnet ID in the
# given VPC, for example:
#
# "network" {
#   "id": "vpc-ABCXYZ",
#   "subnets": ["subnet-123456A"]
# }

# Upload the world configuration to the Deployd server
curl -X POST -H "Content-Type: application/json" --data "@world.json" http://127.0.0.1/worlds
```

Good to go!

#### Deploy a Service!

**NOTE:** Prototype! This is a very straitjacketed example but it should illustrate the concepts and is reproducible with a little luck ;)

Let's deploy a really simple Hello, World! app. There's already one living in the [hack/hello](hack/hello) directory.

```bash

cd hack/
virtualenv venv
. venv/bin/activate
pip install -Ur requirements.txt

bin/dipap.py package hello
bin/dipap.py push hello.tar.gz
```

## Developer Setup

Please see the [Developer Manual](doc/developer.md).

## License

Project is open-source software licensed under Apache 2.0. Please see [License](LICENSE) for further details.