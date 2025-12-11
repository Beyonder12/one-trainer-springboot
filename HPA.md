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

# Deploy extensions metrics untuk mengumpulkan data resource(cpu, memory, etc)
kubectl apply -f component-metrics.yaml

# Deploy autoscalling
kubectl apply -f deployment-autoscalling-shipping-service.yaml

# OS yang akan menjadi sumber lonjakan requests infinity
kubectl run load-generator --image=busybox --restart=Never -- /bin/sh -c "while true; do wget -q -O- http://shipping-service:8083/api/v1/shippings/hello; done"

kubectl delete pod  load-generator