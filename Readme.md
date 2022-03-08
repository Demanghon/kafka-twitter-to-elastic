# Twiter to Elastic   

The main purposes of this project is to fetch tweets from twitter and push them to an Elastic Search cluster. The data are transmit from Twitter to Elastic Search through Kafka.

## Requirements

To run the application you have to install a Kubernetes cluster with Ingress Nginx and kubecelt configured.

> If you haven't any k8s configured you can install [kind](https://kind.sigs.k8s.io/) and run the script *bash infra/create-kind-cluster.sh*.

## Configure cluster

The cluster needs some requirements as:
- Kafka operator
- Elastic Search operator

To configure it run the script:
```bash
bash infra/configure-k8s-cluster.sh
```

## Install Kafka cluster

```bash
kubectl apply -f infra/k8s/kafka-cluster.yml
```

To check when the Kafka cluster is ready you can run this following command:
```bash
kubectl wait kafka/kafka-cluster --for=condition=Ready --timeout=300s -n kafka
```

If you want to see the evolution of the deployment you can run :
```bash
kubectl get pods -n kafka -w 
```
## Install ElasticSearch cluster

```bash
kubectl apply -f infra/k8s/elasticsearch-cluster.yml
```

To monitor ElasticSearch cluster you can run this following command:
```bash
kubectl  get elasticsearch -n elasticsearch
```

```bash
NAME                    HEALTH   NODES   VERSION   PHASE   AGE
elasticsearch-cluster   green    3       8.0.0     Ready   16m
```
> Wait the Health is "green"

A default user named elastic is automatically created with the password stored in a Kubernetes secret:

```bash
kubectl get secret elasticsearch-cluster-es-elastic-user -o go-template='{{.data.elastic | base64decode}}' -n elasticsearch;echo
```

A Kibana instance is deployed and expose through an Ingress configure with the host https://kibana.dev.

>You have to configure your hostsfile to redirect kibana.dev host to your k8s cluster IP

# Configure Prometheus (Optional)

If you want to see the activites of kafka you can run the following configuration to deploy Prometheus and Grafana:

```bash
kubectl create namespace monitoring
kubectl apply -f infra/k8s/prometheus-additional.yml
kubectl apply -f infra/k8s/strimzi-pod-monitor.yml
kubectl apply -f infra/k8s/prometheus.yml
kubectl apply $(ls infra/k8s/grafana*.yml | awk ' { print " -f " $1 } ')
```

> An ingress is configured to the hosts grafana.dev and prometheus.dev

## Configure Twitter

A developer Twitter account and an Twitter project is necessary to fetch tweet through API. You have more information on https://developer.twitter.com/.

## Run the connectors

Now we can run the connectors to fecth tweets from Twitter and push them to ElasticSearch. We have two options:
- Manual connector: I developed a Twitter client and an ElasticSearch client
- Kafka connector: We used the Twitter Kafka Connector and the Elastic Serach Kafka connector

### Manual connector

Fetch the Bearer token from your Twitter project and create secret file :

```bash
kubectl create secret generic twitter-producer --from-literal=TWITTER_BEARER=<your Bearer token> --dry-run=client -oyaml > infra/k8s/twitter-producer-secret.yml


The stack is composed by :
- **twitter-consumer**: fetch data from Twitter Stream API and send them to "twitter" Kafka topic
- **elasticsearch-consumer**: subscribe to "twitter" Kafka topic and store tweets to "tiwtter" Elasticsearch index.

Before run the stack you have to configure it to create the namespace and configure secret required to connect to Kafka and Elasticsearch clusters.
```

### Kafka connector

We run KafkaConnect with [Strimizi](https://strimzi.io/docs/operators/latest/configuring.html#assembly-kafka-connect-str).

The first task is the creation of the image of the Kafka Connector cluster that embed the twitter and the elasticsearch kafka connector:

```bash
docker build -t localhost:5001/kafka-connect-cluster kafka-connect-cluster
docker push localhost:5001/kafka-connect-cluster:latest
```

after you can apply the Kafka connector configuration:
```bash
kubectl apply -f infra/k8s/kafka-connect-cluster.yml
```

> We use the local registry create with the kind cluster. If you use another registry you can update the file infra/k8s/kafka-connect-cluster.yml to update the [registry url](https://strimzi.io/docs/operators/latest/configuring.html#con-common-configuration-images-reference).