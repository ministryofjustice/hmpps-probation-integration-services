#!/usr/bin/env bash
set -euo pipefail

pod_name=schemaspy

# Delete pod on script exit
function delete_pod() { kubectl delete pod "$pod_name"; }
trap delete_pod ERR SIGTERM SIGINT

# Start pod
kubectl run "$pod_name" \
  --image=schemaspy/schemaspy:6.2.4 \
  --restart=Never --stdin=true --tty=true \
  --overrides='{
    "spec": {
      "containers": [
        {
          "name": "schemaspy",
          "image": "schemaspy/schemaspy:6.2.4",
          "command": ["sh"],
          "stdin": true,
          "tty": true,
          "securityContext": { "runAsNonRoot": true, "runAsUser": 1000 },
          "resources": { "limits": { "cpu": "2000m", "memory": "4000Mi" } },
          "env": [ { "name": "JAVA_TOOL_OPTIONS", "value": "-Xss1g -XX:MaxRAMPercentage=75" } ]
        }
      ]
    }
  }' -- sh & sleep 5
kubectl wait --for=condition=ready pod "$pod_name"

# Upload Oracle driver
kubectl cp ./tools/schema-spy/ojdbc10.jar "$pod_name:/drivers_inc/ojdbc10.jar"

# Generate report
includes='^(^Z.*$|^.*[0-9]$|^PRF_.*$|^PERF_.*$|^MIS_.*$|^.*_MV$|^.*\\$.*$|^.*TRAINING.*$|^PDT_THREAD$|^CHANGE_CAPTURE$)$'
kubectl exec "$pod_name" -- /usr/local/bin/schemaspy -db "${DB}" -host "${HOST}" -port "${PORT}" -s "${SCHEMA}" -u "${USERNAME}" -p "${PASSWORD}" -cat "${SCHEMA}" -t orathin-service -I "$includes" -vizjs

# Download report
kubectl cp "$pod_name:/output" schema-spy-report

# Clean up
delete_pod