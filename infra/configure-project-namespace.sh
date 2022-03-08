script_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd -P)

usage() {
  cat << EOF # 
Usage: configure-project-namespace.sh
Create the namespace "twitter-to-elastic" and copy required secrets from Kafka and ElasticSearch. 
Available options:
-h, --help      Print this help and exit
...
EOF
  exit
}

msg() {
  echo >&2 -e "${1-}"
}

die() {
  local msg=$1
  local code=${2-1} # default exit status 1
  msg "$msg"
  exit "$code"
}

parse_params() {
  # default values of variables set from params
  flag=0
  param=''

  while :; do
    case "${1-}" in
    -h | --help) usage ;;
    -?*) die "Unknown option: $1" ;;
    *) break ;;
    esac
    shift
  done

  args=("$@")

  return 0
}

parse_params "$@"

echo "Configure project namespace"
kubectl create namespace twitter-to-elastic
kubectl get secrets -n kafka kafka-cluster-cluster-ca-cert -o json | jq 'del(.metadata.ownerReferences)' | jq 'del(.metadata.namespace)' | kubectl apply -n twitter-to-elastic -f -
kubectl -n elasticsearch get secrets elasticsearch-cluster-es-elastic-user -oyaml | grep -v '^\s*namespace:\s' | kubectl apply -n twitter-to-elastic -f -
kubectl -n elasticsearch get secrets elasticsearch-cluster-es-http-certs-public -oyaml | grep -v '^\s*namespace:\s' | kubectl apply -n twitter-to-elastic -f -