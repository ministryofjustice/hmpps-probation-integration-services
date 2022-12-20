package uk.gov.justice.digital.hmpps

fun Any.set(field: String, value: Any) {
    val f = this::class.java.getDeclaredField(field)
    f.isAccessible = true
    f.set(this, value)
    f.isAccessible = false
}