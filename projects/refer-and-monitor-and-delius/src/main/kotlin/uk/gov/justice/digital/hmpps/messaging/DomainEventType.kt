package uk.gov.justice.digital.hmpps.messaging

sealed interface DomainEventType {
    val name: String

    object InitialAppointmentSubmitted : DomainEventType {
        override val name: String = "intervention.initial-assessment-appointment.session-feedback-submitted"
    }

    object SessionAppointmentSubmitted : DomainEventType {
        override val name: String = "intervention.session-appointment.session-feedback-submitted"
    }

    data class Other(override val name: String) : DomainEventType

    companion object {
        private val types = listOf(InitialAppointmentSubmitted, SessionAppointmentSubmitted).associateBy { it.name }
        fun of(name: String) = types[name] ?: Other(name)
    }
}
