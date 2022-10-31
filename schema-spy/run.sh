#!/bin/bash

java -version
java -jar ./artifacts/schemaspy-6.1.0.jar -t orathin-service \
          -dp ./artifacts/ojdbc8.jar \
          -db "${DB}" \
          -host "${HOST}" \
          -port "${PORT}" \
          -s "${SCHEMA}" \
          -u "${USERNAME}" \
          -p "${PASSWORD}" \
          -cat "${SCHEMA}" \
          -I "^(^Z.*$|^.*[0-9]$|^SPG.*$|^PRF_.*$|^MIS_.*$|^.*\\$.*$)$" \
          -vizjs \
          -o ./DeliusSchema
