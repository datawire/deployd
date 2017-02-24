#!/usr/bin/env bash

NAMESPACE=d6e-system

kubectl get namespace "$NAMESPACE" &> /dev/null
if [ $? -eq 0 ]; then
    echo "Namespace '$NAMESPACE' already exists. Creation skipped!"
else
    kubectl apply -f src/kubernetes/deployd-namespace.yaml
fi

kubectl create secret generic deployd-config \
    --from-file=config.yaml \
    --namespace=d6e-system

kubectl apply -f src/kubernetes/deployd.yaml
