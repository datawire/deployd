# Deployd

[![Build Status](https://travis-ci.org/datawire/deployd.svg?branch=master)](https://travis-ci.org/datawire/deployd)
[![Docker Stars](https://img.shields.io/docker/stars/datawire/deployd.svg)](https://hub.docker.com/r/datawire/deployd)

Deploy micro services on Kubernetes and provision cloud-infrastructure automatically.

## Getting Started

### Prerequisites

1. An [Amazon Web Services ("AWS")](https://aws.amazon.com/) account and active AWS API credentials to use Deployd.
2. [Minikube](https://github.com/kubernetes/minikube) is installed and works locally.

### Deploying Locally

Deployd can be run locally for testing and experimentation! (rough outline, need more detail)

#### Start Minikube

Lines the start with `$>` are commands that should be run.

```bash
$> minikube start
Starting local Kubernetes cluster...

# wait for this output
Kubectl is now configured to use the cluster.
$>
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

```bash
$> cp ../../src/test/resources/worlds/example.json example.json

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
# Then save the file.

curl -X POST -H "Content-Type: application/json" --data "@example.json" http://192.168.99.100:30735/worlds
```

Good to go!

#### Deploy a Service!

### Using Locally

To be written

## Developer Setup

Please see the [Developer Manual](doc/developer.md).

## License

Project is open-source software licensed under Apache 2.0. Please see [License](LICENSE) for further details.
