# Deployd

Deployd is a server that deploys and manages infrastructure and services either by listening to webhook events from integrations (e.g. GitHub, Travis etc) or on-demand by API / UI.

## Initial Design Constraints

Some initial design constraints:

1. Support either monolithic (many services to one VCS repository) or micro repository (one service to one VCS repository) setups.
2. Support infrastructure setup on Amazon or Google cloud.
3. **Developer** interface is a `Dockerfile` + YAML document.

## Other Considerations

1. **Operations** interface is intentionally left undefined because we just don't know. Initial implementation will propose and implement a design purely to make the system demoable.

## Core Concepts

The names of these concepts are TBD. Mostly just trying to come up with a "pleasant" domain model and language. This document doesn't really speak to the level of implementation any of these concepts will have in the initial prototype.

### Capabilities

[Worlds](#worlds) have multiple capabilities which define the operations the world can handle, for example, a "deploy:kubernetes" capability means the world can deploy services onto a Kubernetes cluster. An "aws" or "google" capability means the world knows how to provision and wire Amazon Web Services or Google Cloud Platform resources.

### World

A world is a named place that exposes [capabilities](#capability). Worlds also track the services and infrastructure contained within for the purpose of operational visibility into how the system is constructed.

### Resources

Worlds expose, manage and track resources. A resource is either a pre-canned infrastructure component such as a Kubernetes cluster, a service running on a cluster or an RDS database or a custom component defined by a System engineer (e.g. MongoDB).

### Resource Repositories

A resource repository is something managed by a systems engineer and used by a developer. It allows operators to define custom infrastructure definitions (e.g. Amazon RDS with customized runtime parameters such as disk size, compute and memory capacity) and then export a simple name like "postgres-rds-highmem" which is consumed in a developer's service YAML document.

Repositories can be inherited and shared and this would enable Datawire to expose lots of precanned infrastructure definitions while allowing systems engineers to reuse them.

### Entrypoints and Gates

Domain specific words for a public-internet facing load balancer and associated firewall rules (e.g. Elastic Load Balancer + Security Groups) without being tied to a particular Cloud Provider naming scheme.

## Interaction Models

### Webhook Driven

The system listens for incoming webhook events for various integrations (e.g. GitHub, Travis) and then reacts, for example, in the case of GitHub by cloning the repository and deploying a service.

### REST API

The system exposes a REST API and allows arbitrary creation / mgmt of resources within worlds.

## Prototype Implementation

### Milestone 1

The major theme of M1 is bootstrapping the system. It won't handle GitHub or Travis hooks initially but the internals of the server should be defined enough to handle that in M2.

1. Deployd is containerized and can be run on a Kubernetes cluster.
2. Deployd listens for incoming webhook events from the developers filesystem and deploys services to the same Kubernetes cluster based on the contents of the repositories `deployd.yaml`
3. Deployd knows how to talk to AWS for the purpose of provisioning named infrastructure
4. Deployd exposes primitive API(s) for seeing state of the kubernetes cluster, the "World" and learning what "slugs" can be put in the `deployd.yaml` file (RDS support)
