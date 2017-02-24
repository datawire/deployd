#!/usr/bin/env bash

NAMESPACE=d6e-system

kubectl delete -f src/kubernetes/deployd-namespace.yaml
kubectl get -f src/kubernetes/deployd-namespace.yaml &> /dev/null

while [ $? -eq 0 ]; do
    sleep 1
    kubectl get -f src/kubernetes/deployd-namespace.yaml
done

kubectl delete -f src/kubernetes/deployd.yaml