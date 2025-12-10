## Build image

docker build -t shipping-service:latest .\shipping-service\

## Switch from minikube to docker kubernetes
minikube stop # Hentikan program minikube
minikube delete --all purge # Ngehapus kubernetes yang di minikube, klo nggga bentrok


# Delete all deployments in the current namespace
kubectl delete deployment --all

# Delete all resources in current namespace
kubectl delete all --all

# Or be more specific
kubectl delete deployment,service,configmap,secret --all

kubectl get deployments
kubectl get pods
kubectl get services

# Deploy shipping service
kubectl apply -f deployment.yaml