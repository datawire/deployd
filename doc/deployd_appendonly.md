# Append-only Kubernetes Algorithm

A deployment strategy that never removes previous versions of a deployment but instead continues to "append" new deployments. This strategy is excellent for pure business-logic services and backing services where backwards and forwards compatibility between persistence schema changes can be maintained.

## For all releases...

1. Create a new Deployment and set labels as (release = $N)
2. Wait for Deployment.release = $N to start.
3. Update Deployment.release = $N and set label (latest = TRUE)
4. Update Deployment.release = ($N - 1) and set label (latest = FALSE)
5. Create a new Service named "$name-$release" and set selector (release = $N)
6. Create a new Service named "$name-latest" and set selector   (latest = TRUE)

If the service is meant to be public then you'll need an Ingress resource. One rule should always point at the $name-latest service while independent rules can be created per release.

The Append-only strategy can be coupled with a ContinuousDownscale strategy that overtime lowers the number of
of running containers based on usage information.