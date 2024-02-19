package uk.gov.justice.digital.hmpps.messaging

sealed interface EventProcessingResult {
    val properties: Map<String, String>

    data class Success(val eventType: DomainEventType, override val properties: Map<String, String> = mapOf()) :
        EventProcessingResult

    data class Failure(val exception: Exception, override val properties: Map<String, String> = mapOf()) :
        EventProcessingResult

    data class Rejected(val exception: Exception, override val properties: Map<String, String> = mapOf()) :
        EventProcessingResult
}
