package uk.gov.justice.digital.hmpps.exception

open class NotFoundException(message: String) : RuntimeException(message) {
    constructor(
        entity: String,
        field: String,
        value: Any
    ) : this("$entity with $field of $value not found")

    companion object {
        inline fun <reified T> T?.orNotFoundBy(key: String, value: Any): T =
            this ?: throw NotFoundException(T::class.simpleName ?: "Object", key, value)
    }
}
