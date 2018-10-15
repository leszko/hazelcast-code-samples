# Hazelcast embedded on Kubernetes

This is a sample Spring Boot application with embedded Hazelcast, which presents forming a Hazelcast cluster on Kubernetes.

This sample uses Kubernetes API for Hazelcast member discovery.

## 1. Build application and Docker image

To build your application, use Maven:
```bash
mvn clean package
```

Then, you can build Docker image with the use of `Dockerfile`.
```bash
docker build -t leszko/hazelcast-kubernetes-embedded-sample .
```

Please change `leszko` to your Docker Hub login.

Push the image into the registry.

```bash
docker push leszko/hazelcast-kubernetes-embedded-sample
```

## 2. Grant access to Kubernetes API

In order for the POD to use Kubernetes API, you need to create the given Role Binding.

```bash
kubectl apply -f rbac.yaml
```

## 3. Deploy application

Update `deployment.yaml` with the image you pushed to Docker Hub. Then, to deploy an application, run the following command:

```bash
kubectl apply -f deployment.yaml
```

## 4. Verify that Application works correctly

You can check that the Deployment and Service were created.

```
$ kubectl get all
NAME                                      READY     STATUS    RESTARTS   AGE
pod/hazelcast-embedded-57f84c545b-64tnk   1/1       Running   0          2m
pod/hazelcast-embedded-57f84c545b-jjhcs   1/1       Running   0          45s

NAME                         TYPE           CLUSTER-IP      EXTERNAL-IP      PORT(S)                         AGE
service/hazelcast-embedded   LoadBalancer   10.19.251.145   104.154.43.142   5701:32302/TCP,8080:31613/TCP   2m

NAME                                       DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE
deployment.extensions/hazelcast-embedded   2         2         2            2           2m

NAME                                                  DESIRED   CURRENT   READY     AGE
replicaset.extensions/hazelcast-embedded-57f84c545b   2         2         2         2m

NAME                                 DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/hazelcast-embedded   2         2         2            2           2m

NAME                                            DESIRED   CURRENT   READY     AGE
replicaset.apps/hazelcast-embedded-57f84c545b   2         2         2         2m
```

In the logs for PODs, you should see that the Hazelcast members formed a cluster.

```
$ kubectl logs pod/hazelcast-embedded-57f84c545b-jjhcs
 ...
 Members {size:2, ver:4} [
         Member [10.16.2.6]:5701 - 33076b61-e99d-46f2-b5c1-35e0e75f2311
         Member [10.16.2.8]:5701 - 9ba9bb61-6e34-460a-9208-c5a644490107 this
 ]
 ...
```

Then, you can access the application, by its `EXTERNAL-IP`.

![Verify Application](markdown/verify-application-1.png)

![Verify Application](markdown/verify-application-2.png)