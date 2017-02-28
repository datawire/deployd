# Service Descriptor to Kubernetes Mapping

A [deployd.yaml](docs/deployd.yaml) YAML document is an interface for developers to describe 
the high-level requirements of a service deployment. Deployd reads a service's `deployd.yaml` 
and maps the provided information into the appropriate combination of Kubernetes API calls to 
deploy or update the service.

## Terminology

| Term | Definition |
| ---- | ---------- |

## Mapping Methodology

At a very high level the mapping function operates on the contents of `deployd.yaml`, the 
existing state of a deploymentl, and an incoming deployment request which is the trigger to begin 
a deployment. In the context of this diagram "existing state" refers to a services current 
deployed state which may be nonexistent for a brand new service.

```text
+--------------------+
| deployment request |---+
+--------------------+   |
                         |
                         |
                         |
+--------------+         |    +-------------------------+    +--------------------------------+
| deployd.yaml |---------*--->]     mapping function    [--->| (n >= 0) Kubernetes operations |      
+--------------+         |    |  existing  =>  desired  |    +--------------------------------+   
                         |    +-------------------------+                             
                         |                                        
                         |
+----------------+       |
| existing state |-------+
+----------------+
```

### Future: Alternative Mappings

This design allows the inputs to be mapped to alternative output operations (e.g. EC2 API calls) 
for later expansion.

### Future: Out of band apply 

While it is ideal if Deployd handle the process of applying the Kubernetes operations a user 
may not want this. The "Kubernetes operations" output could eventually become the basis for the 
deployment equivalent of an Abstract Syntax Tree ("AST") that could be converted into alternative 
formats (e.g. Docker Compose), or customizable and reusable scripts that can be plugged into 
another part of a deployment pipeline.

## Mapping Algorithm

Given a service descriptor and inflight [Deployment Request](docs/deployment-request.md) there 
are two paths that can be followed. The first path is the mechanics for new service deployment 
while the second path is for existing service upgrades.

### New Service

Given the following things:

- A deployment request `$request`
- A service descriptor `$service`
- A Kubernetes cluster `$k8s` without `$service.name in $k8s`
- An empty collection of tasks `$deploymentTasks` 

#### 1. Create a Kubernetes Namespace object for the service and add it to `$deploymentTasks`

Create a namespace manifest `$namespace`:

```json
{
    "kind"       : "Namespace"
    "apiVersion" : "v1",
    "metadata"   : {"name": "${service.name}"}
}
```

Then add `$namespace` to `$deploymentTasks`

#### 2. Determine Deployment Strategy

There are three built-in deployment strategies in Deployd.

| Strategy    | Description |
| ------------| ----------- |
| Append-Only | Given `<N>` instances of `$service.currentVersion` then run `$request.newVersion` 
in parallel without removing `$service.currentVersion`. |
| Blue-Green  | Given `<N>` instances of `$service.currentVersion` in color-group `GREEN` then 
create a parallel color-group `BLUE` with `$request.newVersion`. Once `BLUE` is "ready" switch the 
frontends target from `GREEN` to `BLUE`. |
| Rolling     | Given `<N>` instances of $service then migrate each instance from 
`$service.currentVersion` to `$request.newVersion` by updating `(N >= 1)` at a time. |

#### 2a. Append-Only Mapping

#### 2b. Blue-Green Mapping

#### 2c. Rolling Mapping

#### 3. Generate a

### Existing Service
