package uk.gov.justice.digital.hmpps

import kotlin.reflect.KProperty1

fun Any.set(field: String, value: Any?) {
    val f = this::class.java.getDeclaredField(field)
    f.isAccessible = true
    f.set(this, value)
    f.isAccessible = false
}

fun <T : Any, V : Any?> T.set(field: KProperty1<T, V>, value: V): T {
    this.set(field.name, value)
    return this
}
