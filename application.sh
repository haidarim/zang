#!/bin/bash


if [ "$1" == "install" ]; then
    sudo dnf install kubectl

    kubectl version --client

    sudo dnf install kind # local k8n, dev only
    kind version

    sudo dnf install helm
    helm version

elif [ "$1" == "create" ]; then # create cluster
    kind create cluster --name zang

elif [ "$1" == "get-nodes" ]; then
    kubectl get nodes
fi


