package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.RegisterType
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Registration
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData

object RegistrationGenerator {

    fun mappaRegistration(
        offenderId: Long,
        category: ReferenceData,
        type: RegisterType
    ) = Registration(
        personId = offenderId,
        type = type,
        category = category,
        deRegistered = false,
        softDeleted = false
    )
}
