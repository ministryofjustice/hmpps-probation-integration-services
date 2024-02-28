package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.OVERVIEW
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Person
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

object ContactGenerator {

    val APPT_CT_1 = generateContactType("C089", true, "Alcohol Key Worker Session (NS)")
    val OTHER_CT = generateContactType("XXXX", false, "Non attendance contact type")
    val APPT_CT_2 = generateContactType("CODI", true, "Initial Appointment on Doorstep (NS)")
    val APPT_CT_3 = generateContactType("CODC", true, "Planned Doorstep Contact (NS)")

    val PREVIOUS_APPT_CONTACT = generateContact(
        OVERVIEW,
        APPT_CT_1,
        ZonedDateTime.of(LocalDateTime.now().minusHours(1), ZoneId.of("Europe/London"))
    )
    val FIRST_NON_APPT_CONTACT = generateContact(
        OVERVIEW,
        OTHER_CT,
        ZonedDateTime.of(LocalDateTime.now().plusHours(1), ZoneId.of("Europe/London"))
    )
    val FIRST_APPT_CONTACT = generateContact(
        OVERVIEW,
        APPT_CT_2,
        ZonedDateTime.of(LocalDateTime.now().plusHours(2), ZoneId.of("Europe/London"))
    )
    val NEXT_APPT_CONTACT = generateContact(
        OVERVIEW,
        APPT_CT_3,
        ZonedDateTime.of(LocalDateTime.now().plusHours(3), ZoneId.of("Europe/London"))
    )

    fun generateContact(
        person: Person,
        contactType: ContactType,
        startTime: ZonedDateTime,
        rarActivity: Boolean? = null,
        attended: Boolean? = null,
        complied: Boolean? = null,
        requirementId: Long? = null
    ) = Contact(
        IdGenerator.getAndIncrement(),
        person.id,
        contactType,
        startTime,
        startTime,
        rarActivity,
        attended,
        complied,
        requirementId
    )

    private fun generateContactType(code: String, attendance: Boolean, description: String) =
        ContactType(IdGenerator.getAndIncrement(), code, attendance, description)
}

