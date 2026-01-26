package uk.gov.justice.digital.hmpps.utils

object Extensions {
    inline fun <K, reified V> Map<K, V>.reportMissing(required: Set<K>) = also {
        val missing = required - keys
        require(missing.isEmpty()) { "Invalid ${V::class.simpleName}: $missing" }
    }
}