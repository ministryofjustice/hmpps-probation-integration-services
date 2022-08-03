package uk.gov.justice.digital.hmpps.exception

class NotActiveException(entity: String, field: String, value: Any) :
    RuntimeException("$entity with $field of $value not active")
