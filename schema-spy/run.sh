#!/bin/bash

java -Xss4096k -Xmx2G -Xms2G -jar ./artifacts/schemaspy-6.1.0.jar -t orathin-service \
          -dp ./artifacts/ojdbc8.jar \
          -db "${DB}" \
          -host "${HOST}" \
          -port "${PORT}" \
          -s "${SCHEMA}" \
          -u "${USERNAME}" \
          -p "${PASSWORD}" \
          -cat "${SCHEMA}" \
          -I "^(^Z.*$|^.*[0-9]$|^SPG.*$|^R_SPG.*$|^PRF_.*$|^PERF_.*$|^MIS_.*$|^.*\\$.*$|^.*TRAINING.*$|^PDT_THREAD$|^CHANGE_CAPTURE$)$" \
          -vizjs \
          -o ./DeliusSchema
