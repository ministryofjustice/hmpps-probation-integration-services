package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.offender.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.RegisterType
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Registration
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData

object RegistrationGenerator {

    fun mappaRegistration(
        offenderId: Long,
        contact: Contact,
        category: ReferenceData,
        type: RegisterType
    ) = Registration(
        personId = offenderId,
        contact = contact,
        type = type,
        category = category,
        deRegistered = false,
        softDeleted = false
    )
}
