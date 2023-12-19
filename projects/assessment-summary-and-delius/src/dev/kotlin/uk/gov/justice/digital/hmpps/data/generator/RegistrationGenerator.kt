package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.RegisterType
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Registration
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import uk.gov.justice.digital.hmpps.service.Risk
import java.time.LocalDate

object RegistrationGenerator {

    val TYPES = Risk.entries.map {
        generateType(
            it.code, colour = when (it) {
                Risk.V -> "Red"
                Risk.H -> "Orange"
                Risk.M -> "Amber"
                Risk.L -> "Green"
            }
        )
    }.associateBy { it.code }

    fun generate(
        personId: Long,
        date: LocalDate,
        type: RegisterType,
        contact: Contact,
        teamId: Long = ProviderGenerator.DEFAULT_TEAM_ID,
        staffId: Long = ProviderGenerator.DEFAULT_STAFF_ID,
        nextReviewDate: LocalDate? = type.reviewPeriod?.let { date.plusMonths(it) },
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Registration(personId, date, contact, teamId, staffId, type, nextReviewDate, softDeleted, id)

    fun generateType(
        code: String,
        description: String = "Description of $code",
        flag: ReferenceData = ReferenceDataGenerator.DEFAULT_FLAG,
        registrationContactType: ContactType? = ContactGenerator.TYPES[ContactType.Code.REGISTRATION.value],
        reviewContactType: ContactType? = ContactGenerator.TYPES[ContactType.Code.REGISTRATION_REVIEW.value],
        reviewPeriod: Long? = 6,
        colour: String? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = RegisterType(code, description, flag, registrationContactType, reviewPeriod, reviewContactType, colour, id)
}