package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.ROSH_FLAG
import uk.gov.justice.digital.hmpps.enum.RiskOfSeriousHarmType
import uk.gov.justice.digital.hmpps.enum.RiskType
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.RegisterType
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Registration
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import java.time.LocalDate

object RegistrationGenerator {

    val TYPES = listOf(
        RiskOfSeriousHarmType.entries.map { generateType(it.code, it.colour, flag = ROSH_FLAG) },
        RiskType.entries.map { generateType(it.code, "Amber", "Risk to ${it.name.lowercase()}") }
    ).flatten().associateBy { it.code }

    val RiskOfSeriousHarmType.colour
        get() = when (this) {
            RiskOfSeriousHarmType.V -> "Red"
            RiskOfSeriousHarmType.H -> "Orange"
            RiskOfSeriousHarmType.M -> "Amber"
            RiskOfSeriousHarmType.L -> "Green"
        }

    fun generate(
        personId: Long,
        date: LocalDate,
        contact: Contact,
        type: RegisterType,
        level: ReferenceData? = null,
        teamId: Long = ProviderGenerator.DEFAULT_TEAM_ID,
        staffId: Long = ProviderGenerator.DEFAULT_STAFF_ID,
        nextReviewDate: LocalDate? = type.reviewPeriod?.let { date.plusMonths(it) },
        notes: String? = null,
        softDeleted: Boolean = false,
    ) = Registration(personId, date, contact, teamId, staffId, type, level, nextReviewDate, notes, softDeleted)

    fun generateType(
        code: String,
        colour: String,
        description: String = "Description of $code",
        flag: ReferenceData = ReferenceDataGenerator.SAFEGUARDING_FLAG,
        registrationContactType: ContactType? = ContactGenerator.TYPES[ContactType.Code.REGISTRATION.value],
        reviewContactType: ContactType? = ContactGenerator.TYPES[ContactType.Code.REGISTRATION_REVIEW.value],
        reviewPeriod: Long? = 6,
        id: Long = IdGenerator.getAndIncrement()
    ) = RegisterType(code, description, flag, registrationContactType, reviewPeriod, reviewContactType, colour, id)
}