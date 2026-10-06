package uk.gov.justice.digital.hmpps.service

class TelemetryAggregator() {
    private val data = mutableMapOf<String, MutableList<String>>()

    fun add(key: String, value: String) {
        if (!data.containsKey(key)) {
            data[key] = mutableListOf()
        }
        data[key]?.apply { add(value) }
    }

    fun params() = data.map { it.key to it.value.sorted().joinToString(",", "[", "]") }.toMap()

    companion object {
        const val REGISTERED = "Registered"
        const val DEREGISTERED = "Deregistered"
        const val REVIEW_COMPLETED = "ReviewCompleted"
    }
}