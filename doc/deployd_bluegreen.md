# Blue-Green Kubernetes Algorithm

The workflow of a Blue-Green swap is that given an initial version start deployment as BLUE then when all pods are healthy configure a Service to point at Blue.

## For v1:

1. Create a new DeploymentRequest and set labels as (color = BLUE, access = EXTERNAL)
2. Wait for Blue to start
3. Create a new Service and set selector (access = EXTERNAL)
4. Create a new Service and set selector (color = GREEN) - This allows us to talk to the Green instances for test purposes.

## For v2, v3.., vX

1. Create a new DeploymentRequest and set labels as (color = GREEN, access = INTERNAL)
2. Wait for Green to start.
3. Update the access label on Green (access = EXTERNAL)
4. Update the access label on Blue  (access = INTERNAL)
