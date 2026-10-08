#!/bin/bash
set -euo pipefail

# Workaround for the jdbc_streaming plugin not supporting statement_filepath.
# See https://github.com/logstash-plugins/logstash-integration-jdbc/issues/51

embed_incremental_statement() {
  local pipeline_dir="$1"
  local conf_file="$2"
  local pipeline_type="$3"
  local sql escaped_sql

  sql=$(tr '\n' ' ' < "${pipeline_dir}/statement.sql")
  sql=${sql//:batch_size/0}
  sql=${sql//:sql_last_value/0}

  case "$pipeline_type" in
    person)
      sql=${sql//:offender_id/?}
      ;;
    contact)
      sql=${sql//:full_load_contact_threshold/-1}
      sql=${sql//:contact_id/?}
      ;;
    *)
      echo "Unknown pipeline type: ${pipeline_type}" >&2
      exit 1
      ;;
  esac

  escaped_sql=$(printf '%s' "$sql" | sed 's/[\\&@]/\\&/g')
  sed -i "s@\${INCREMENTAL_STATEMENT_SQL}@${escaped_sql}@" "$conf_file"
}

replace_named_parameters() {
  sed -i -E 's/\B:\w+/?/g' "$1"
}

embed_incremental_statement /pipelines/person /pipelines/person/logstash-incremental.conf person
embed_incremental_statement /pipelines/contact-keyword /pipelines/contact-keyword/logstash-incremental.conf contact
embed_incremental_statement /pipelines/contact-semantic /pipelines/contact-semantic/logstash-incremental.conf contact

replace_named_parameters /pipelines/person/statement.sql
replace_named_parameters /pipelines/contact-keyword/statement.sql
replace_named_parameters /pipelines/contact-semantic/statement.sql

