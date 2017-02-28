# Service Descriptor to Kubernetes Mapping

A [deployd.yaml](docs/deployd.yaml) YAML document is an interface for developers to describe the high-level requirements of a service deployment. Deployd reads a service's `deployd.yaml` and maps the provided information into the appropriate combination of Kubernetes API calls to deploy or update the service.

## Terminology

| Term | Definition |
| ---- | ---------- |

## Mapping Methodology

The responsibility of the mapping function 

```text

+--------------+           +-------------------------+    +--------------------------------+
| deployd.yaml |------*--->]     mapping function    [--->| (n >= 0) kubernetes operations |        
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

