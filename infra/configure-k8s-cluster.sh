script_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd -P)

usage() {
  cat << EOF # 
Usage: configure-k8s-cluster.sh
configure k8s cluster to install requirements as Kafka operator and ECK. 
WARNING: This script doesn't assume the installation of k8s.
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

echo "Installing strimiz operator"
kubectl create namespace kafka
kubectl apply -f https://strimzi.io/install/latest?namespace=kafka

echo "Install ECK"
kubectl create -f https://download.elastic.co/downloads/eck/2.0.0/crds.yaml
kubectl apply -f https://download.elastic.co/downloads/eck/2.0.0/operator.yaml

echo "Install monitoring tools"
kubectl create namespace monitoring
curl -s https://raw.githubusercontent.com/prometheus-operator/prometheus-operator/v0.51.2/bundle.yaml | sed -e '/[[:space:]]*namespace: [a-zA-Z0-9-]*$/s/namespace:[[:space:]]*[a-zA-Z0-9-]*$/namespace: monitoring/' | kubectl apply -n monitoring -f -