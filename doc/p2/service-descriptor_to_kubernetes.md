# Service Descriptor to Kubernetes Mapping

A [deployd.yaml](docs/deployd.yaml) YAML document is an interface for developers to describe 
the high-level requirements of a service deployment. Deployd reads a service's `deployd.yaml` 
and maps the provided information into the appropriate combination of Kubernetes API calls to 
deploy or update the service.

## Terminology

| Term | Definition |
| ---- | ---------- |
| Backend  | A named and exposed port on a service (e.g. rest-api:5001 means the REST API is exposed on port 5001). |
| Frontend | An abstraction for accessing one or more backends from a single point. |

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

## Mapping Algorithms

Given a service descriptor and inflight [Deployment Request](docs/deployment-request.md) there 
are two paths that can be followed. The first path is the mechanics for new service deployment 
while the second path is for existing service upgrades.

## Networking: Frontends

Under the descriptors `networking` object there is a `frontend` object. The "frontend" is an abstract concept that **DOES NOT** necessarily map 1:1 with an equivalent Kubernetes object such as `v1.Service` or `v1.Ingress` because once a services deployment model is accounted for by the mapper then the system may need to create additional objects to facilitate orchestrating the particular technique.

### Case: No Frontend

An internal service is not exposed outside of Kubernetes. Sometimes it is desirable to have a service that has no frontend because you do not want Kubernetes to handle routing traffic to backends.

**Service Descriptor (relevant chunk only)**

```yaml
frontend:
    type: none
```

**Kubernetes**

```json
    {
        "kind": "Service",
        "apiVersion": "v1",
        
        "metadata": {
            "namespace": "${service.name}",
            "name": "${service.name}-${request.version}",
            "labels": { 
                "service": "${service.name}-${request.version}" 
            }
        },
        
        "spec": {
            "type": "None",
            "clusterIP": "None"
        }
    }
```

### Case: Internal

An internal service is not exposed outside of Kubernetes. An internal frontend is equivalent to having a load balancer in front of set of services.

```yaml
frontend:
  type: internal
  ports:
    - target: rest-api
      port: 80
```

**Kubernetes**

**NOTE**: `v1.Service.spec.selector` is mapped based on the type of update strategy for the deployment.

```json
    {
        "kind": "Service",
        "apiVersion": "v1",
        
        "metadata": {
            "namespace": "${service.name}",
            "name": "${service.name}",
            "labels": { 
                "service": "${service.name}" 
            }
        },
        
        "selector": {  },
        
        "spec": {
            "type": "ClusterIP",
            "ports": [
                {
                    "name": "rest-api",
                    "protocol": "${ backends['rest-api'].protocol }",
                    "port": 80,
                    "targetPort": "${ backends['rest-api'].name }"
                }
            ]
        }
    }
```

### Case: External

An external service is one that is exposed on a internet-addressable IP address for each cluster node. The mapping for an external frontend is identical to [internal](#case-internal) except `v1.Service.spec.type = NodePort`.

### Case: External:LoadBalanced

An external service is one that is exposed on a internet-addressable IP address for each cluster node. The mapping for an external frontend is identical to [internal](#case-internal) except `v1.Service.spec.type = LoadBalanced`.

## Networking: Backends

Under the descriptors `networking` object there is a `backends` list. Backends describe what ports should be exposed on the services container (the "backend"). 

Backend objects are mapped to Kubernetes `v1.PodSpec.Container[$service.name].ports` entries (`v1.ContainerPort`):

Consider a service that requires three open ports on the container:

**Service Descriptor (relevant chunk only)**

```yaml
backends:
    - name: rest-api
      protocol: tcp
      port: 5001
      
    - name: admin-api
      port: 5002
      
    - name: heartbeat
      protocol: udp
      port: 5003
```

**Kubernetes (relevant chunk only)**

```json
[
    {
        "name": "rest-api",
        "protocol": "tcp",
        "port": 5001
    },
    {
        "name": "admin-api",
        "protocol": "tcp",
        "port": 5002
    },
    {
        "name": "heartbeat",
        "protocol": "udp",
        "port": 5003
    }
]
```

### Validation

A couple validations need to be performed:

| Validation | Reason? |
| ---------- | ------- |
| The `name` must conform to IANA service name rules `regex = [a-z0-9]([a-z0-9-]*[a-z0-9])*` | Kubernetes requirement |
| The `protocol` can be only "tcp" or "udp" | Kubernetes (and likely others) requirement |
| The `port` must be in range 1..65535 | Networking Requirement |

## New Service

Given the following things:

- A deployment request `$request`
- A service descriptor `$service`
- A Kubernetes cluster `$k8s` without `$service.name in $k8s`
- An empty collection of tasks `$deploymentTasks` 

### 1. Create a Kubernetes `v1.Namespace` object for the service and add it to `$deploymentTasks`

Create a namespace manifest `$namespace`:

```json
{
    "kind"       : "Namespace",
    "apiVersion" : "v1",
    "metadata"   : {"name": "${service.name}"}
}
```

Then add `Create($namespace)` to `$deploymentTasks`

### 2. Determine Deployment Strategy

There are three built-in deployment strategies in Deployd.

| Strategy    | Description |
| ------------| ----------- |
| Append-Only | Given `<N>` instances of `$service.currentVersion` then run `$request.newVersion` in parallel without removing `$service.currentVersion`. |
| Blue-Green  | Given `<N>` instances of `$service.currentVersion` in color-group `GREEN` then create a parallel color-group `BLUE` with `$request.newVersion`. Once `BLUE` is "ready" switch the frontends target from `GREEN` to `BLUE`. |
| Rolling     | Given `<N>` instances of `$service.anyVersion` then migrate each instance from `$service.anyVersion` to `$request.newVersion` by updating `(N >= 1)` at a time. |

#### 2a. Append-Only Mapping

Append-Only strategy mapping creates a new Kubernetes `v1.Service` and a new `v1beta1.Deployment` object for each unique `$request` that is received regardless of whether any other pairs exist already for `${service.name}`. A discussion of the good, bad, and ugly aspects of this strategy can be read in [Append-Only Deployment](docs/deployment_append-only.md).

The Append-Only strategy is **very advanced** and while conceptually simple has a number of edge-cases that make it a challenge to actually map.

1. Create a `v1.Service` object named `${service.name}-${request.version}` in `$namespace`:

    ```json
    {
        "kind": "Service",
        "apiVersion": "v1",
        
        "metadata": {
            "namespace": "${service.name}",
            "name": "${service.name}-${request.version}",
            "labels": { 
                "service": "${service.name}-${request.version}" 
            }
        },
        
        "spec": {
            "type": ""
        }
    }
    ```
    
2. Create a `v1beta1.Deployment` object `${service.name}-${request.version}` in `$namespace`:

    ```json
        
    ```

#### 2b. Blue-Green Mapping

Blue-Green strategy mapping manages two pairs of Kubernetes `v1.Service` and `v1beta1.Deployment` objects.

#### 2c. Rolling Mapping

Rolling strategy mapping manages a single pair of Kubernetes `v1.Service` and `v1beta1.Deployment` objects. A creation involves generating the `v1.Service` object and configuring the `v1.Service.spec.selector` to point at `v1beta1.Deployment`. The mechanism for update involves updating the relevant fields (e.g. Docker tag) on the single `v1beta1.Deployment`. Kubernetes handles the actual mechanics of rolling the containers automatically.

#### 3. Generate a

## Existing Service

- A deployment request `$request`
- A service descriptor `$service`
- A Kubernetes cluster `$k8s` with `N >= 1` `$service.name in $k8s`
- An empty collection of tasks `$deploymentTasks`

### 1. Check to see if the 

### 2. Determine the Deployment Strategy



































