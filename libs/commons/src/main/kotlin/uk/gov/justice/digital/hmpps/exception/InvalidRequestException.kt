package uk.gov.justice.digital.hmpps.exception

open class InvalidRequestException(message: String) : RuntimeException(message) {
    constructor(
        fieldName: String,
        value: Any
    ) : this("Invalid $fieldName of $value sent in payload")
}
