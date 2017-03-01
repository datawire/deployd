# Service Descriptor to Terraform Mapping

A [deployd.yaml](docs/deployd.yaml) YAML document is an interface for developers to describe the high-level requirements of a service deployment. Deployd reads a service's `deployd.yaml` and maps the provided information into the appropriate combination of Kubernetes API calls to deploy or update the service.

## Terminology

| Term | Definition |
| ---- | ---------- |

## Goals and Non-Goals

**Goals**

- How a Terraform module (e.g. PostgreSQL) has its input variables populated (e.g. VPC ID, allowed subnets, disk space).
- How a Terraform module (e.g. PostgreSQL) has its output values given (e.g username, password, URL, port) to a service.
    
**Non-Goals**

- Mechanics of Terraform state management (locking, storage).
- Mechanics of Terraform module creation.

## Sources of information

Deployd feeds Terraform mappable information from two sources of truth.

1. World Information

   The first source of truth is a **World** definition, for example, the AWS network (VPC) to provision in, which subnets can be used and where security credentials come from are all pieces of information contained in a World (somehow).

2. Deployment Parameters

   The second source of information is the Terraform module to use (e.g. "postgresql-v96") and then customizable parameters that a developer can legally override, for example, the number of IOPS required by a database. This information is sourced from the service descriptor associated with the incoming deployment request.

## Mapping Methodology

Terraform mapping is straightforward because Terraform handles most of the heavy lifting and there is little to no configuration generation that needs to occur. Further, the steps to apply a change to a Terraform setup are well understood.

```text
+--------------+
| World Config |---------+
+--------------+         |
                         |
                         |
+--------------------+   |    +-------------------------+    +--------------------------------+
| Deployment Request |---+--->]     mapping function    [--->| (n >= 0) Terraform Variables   |      
+--------------------+        |  existing  =>  desired  |    +--------------------------------+
                              +-------------------------+
```

## Terraform 

Terraform modules are composed of four primary things:

1. Input variables

   Input variables are pieces of information that a module requires before Terraform can plan or apply it. Think of them like function arguments. Input variables are used to provide information such as VPC or subnet identifiers, disk size and other various configuration options.
   
   In our system we will be mostly retrieving values to feed to input variables. This information can come from a World or the deployment request.
   
2. Data sources

   Data sources are information retrieval systems. For example, a data source can be used to query AWS for AMI identifiers. 

3. Resources

   Resources are the pieces of infrastructure being provisioned (or deleted). It's useful to think of resources as the equivalent of a function body. Terraform tries its best to make resource management *safe* via idempotent operations.
   
4. Output values

   Output values are the results of module being run, for example, after creating a database the URL, username and password for the database are candidate outputs.
   
   In our system we will be mostly sending output values to containers as environment variables or configuration files.

## Input Mapping

## Output Mapping

