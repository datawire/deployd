# Service Descriptor to Kubernetes Mapping

A [deployd.yaml](docs/deployd.yaml) YAML document is an interface for developers to describe 
the high-level requirements of a service deployment. Deployd reads a service's `deployd.yaml` 
and maps the provided information into the appropriate combination of Kubernetes API calls to 
deploy or update the service.

## Terminology

| Term | Definition |
| ---- | ---------- |

## Mapping Methodology

At a very high level the mapping function operates on the contents of `deployd.yaml` and the 
existing state of a deployment. In the context of this diagram "existing state" refers to a 
services current deployed state which may be nonexistent for a brand new service.

### Future: Alternative Mappings

This design allows the inputs to be mapped to alternative output operations (e.g. EC2 API calls) 
for later expansion.

### Future: Out of band apply 

While it is ideal if Deployd handle the process of applying the Kubernetes operations a user 
may not want this. The "Kubernetes operations" output could eventually become the basis for the 
deployment equivalent of an Abstract Syntax Tree ("AST") that could be converted into alternative 
formats (e.g. Docker Compose), or customizable and reusable scripts that can be plugged into 
another part of a deployment pipeline.

```text

+--------------+           +-------------------------+    +--------------------------------+
| deployd.yaml |------*--->]     mapping function    [--->| (n >= 0) Kubernetes operations |        
|              |      |    |  existing  =>  desired  |    +--------------------------------+       
+--------------+      |    +-------------------------+                             
                      |
                      |                                    
                      |                                   
                      |
+----------------+    |
| existing state |----+
+----------------+

```

## Mapping Algorithm

Given a service descriptor and inflight [Deployment Request](docs/deployment-request.md) there 
are two paths that can be followed. The first path is the mechanics for new service deployment 
while the second path is for existing service upgrades.

