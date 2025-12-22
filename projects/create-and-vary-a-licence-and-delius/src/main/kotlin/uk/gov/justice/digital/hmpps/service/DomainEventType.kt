package uk.gov.justice.digital.hmpps.service

sealed interface DomainEventType {
    val name: String

    data object LicenceActivated : DomainEventType {
        override val name: String = "create-and-vary-a-licence.licence.activated"
    }

    data object PRRDLicenceActivated : DomainEventType {
        override val name: String = "create-and-vary-a-licence.prrd-licence.activated"
    }

    data object TimeServedLicenceActivated : DomainEventType {
        override val name: String = "create-and-vary-a-licence.time-served-licence.activated"
    }

    data class Other(override val name: String) : DomainEventType

    companion object {
        private val types = listOf(LicenceActivated, PRRDLicenceActivated, TimeServedLicenceActivated)
            .associateBy { it.name }

        fun of(name: String): DomainEventType = types[name] ?: Other(name)
    }
}
