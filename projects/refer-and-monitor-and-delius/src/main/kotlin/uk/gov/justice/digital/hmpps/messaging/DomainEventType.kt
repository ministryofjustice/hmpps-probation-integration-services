package uk.gov.justice.digital.hmpps.messaging

sealed interface DomainEventType {
    val name: String

    object ActionPlanApproved : DomainEventType {
        override val name: String = "intervention.action-plan.approved"
    }

    object ActionPlanSubmitted : DomainEventType {
        override val name: String = "intervention.action-plan.submitted"
    }

    object InitialAppointmentSubmitted : DomainEventType {
        override val name: String = "intervention.initial-assessment-appointment.session-feedback-submitted"
    }

    object ReferralEnded : DomainEventType {
        override val name: String = "intervention.referral.ended"
    }

    object SessionAppointmentSubmitted : DomainEventType {
        override val name: String = "intervention.session-appointment.session-feedback-submitted"
    }

    data class Other(override val name: String) : DomainEventType

    companion object {
        private val types =
            listOf(
                ActionPlanApproved,
                ActionPlanSubmitted,
                InitialAppointmentSubmitted,
                ReferralEnded,
                SessionAppointmentSubmitted,
            ).associateBy { it.name }

        fun of(name: String) = types[name] ?: Other(name)
    }
}
