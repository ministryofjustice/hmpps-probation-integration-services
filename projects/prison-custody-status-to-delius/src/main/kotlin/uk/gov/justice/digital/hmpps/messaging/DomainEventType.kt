package uk.gov.justice.digital.hmpps.messaging

sealed interface DomainEventType {
    val name: String

    data object IdentifierAdded : DomainEventType {
        override val name: String = "probation-case.prison-identifier.added"
    }

    data object IdentifierUpdated : DomainEventType {
        override val name: String = "probation-case.prison-identifier.updated"
    }

    data object PrisonerReceived : DomainEventType {
        override val name: String = "prison-offender-events.prisoner.received"
    }

    data object PrisonerReleased : DomainEventType {
        override val name: String = "prison-offender-events.prisoner.released"
    }

    data class Other(override val name: String) : DomainEventType

    companion object {
        private val types =
            listOf(IdentifierAdded, IdentifierUpdated, PrisonerReceived, PrisonerReleased)
                .associateBy { it.name }

        fun of(name: String) = types[name] ?: Other(name)
    }
}
