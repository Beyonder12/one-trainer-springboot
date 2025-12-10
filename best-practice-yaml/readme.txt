DEPLOY Command

first build each service
docker build -t gateway-service ./gateway-service
docker build -t product-service ./product-service
docker build -t user-service ./user-service
docker build -t order-service ./order-service

then deploy the service to pod kubernetes
kubectl apply -f deployment.yaml

delete
kubectl delete deployment product-service
kubectl delete service product-service