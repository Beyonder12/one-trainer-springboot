# Apply ConfigMaps
kubectl apply -f configmap-dev.yaml
kubectl apply -f configmap-staging.yaml
kubectl apply -f configmap-prod.yaml

# View ConfigMap in specific namespace
kubectl get configmap -n dev
kubectl get configmap -n staging
kubectl get configmap -n prod

# Deploy your app to specific namespace
kubectl apply -f deployment.yaml -n dev
kubectl apply -f deployment.yaml -n staging
kubectl apply -f deployment.yaml -n prod