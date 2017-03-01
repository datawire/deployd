# Service Descriptor to Terraform Mapping

A [deployd.yaml](docs/deployd.yaml) YAML document is an interface for developers to describe the high-level requirements of a service deployment. Deployd reads a service's `deployd.yaml` and maps the provided information into the appropriate combination of Kubernetes API calls to deploy or update the service.

## Terminology

| Term | Definition |
| ---- | ---------- |

## Document Goals and Non-Goals

**Goals**

- Explain how a Terraform module (e.g. PostgreSQL) has its input variables populated (e.g. VPC ID, allowed subnets, disk space).
- Explain how a Terraform module (e.g. PostgreSQL) has its output values given (e.g username, password, URL, port) to a service.
    
**Non-Goals**

- Mechanics of Terraform state management (locking, storage).
- Mechanics of Terraform module creation.

## Sources of information

Terraform modules have two sources of truth that are used during mapping. The first source of truth is a **World** definition, for example, the AWS network (VPC) to provision in, which subnets can be used and where security credentials come from are all pieces of information contained in a World (somehow). This information needs to be fed into Terraform during input mapping. The second source of information is customizable parameters that a developer can legally override, for example, the number of IOPS required by a database.

## Mapping Methodology

Terraform mapping is fairly straightforward because Terraform handles most of the heavy lifting.

```text
+--------------+
| World Config |--------+
+--------------+        |
                        |
                        |
+-------------------+   |    +-------------------------+    +--------------------------------+
| Deploment Request |---+--->]     mapping function    [--->| (n >= 0) Terraform Variables   |      
+-------------------+        |  existing  =>  desired  |    +--------------------------------+
                             +-------------------------+
```
