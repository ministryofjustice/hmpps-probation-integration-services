package uk.gov.justice.digital.hmpps.service

sealed interface DomainEventType {
    val name: String

    data object LicenceActivated : DomainEventType {
        override val name: String = "create-and-vary-a-licence.licence.activated"
    }

    data class Other(override val name: String) : DomainEventType

    companion object {
        private val types = listOf(LicenceActivated)
            .associateBy { it.name }

        fun of(name: String) = types[name] ?: Other(name)
    }
}
