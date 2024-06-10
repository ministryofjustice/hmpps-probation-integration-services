package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType.Code
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.EnforcementAction
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.Nsi
import java.time.LocalDate
import java.time.ZonedDateTime

object ContactGenerator {
    val TYPES = Code.entries.map {
        when (it) {
            Code.CRSAPT, Code.CRSSAA -> generateType(it.value, nationalStandards = true, attendance = true)
            else -> generateType(it.value)
        }
    }.associateBy { it.code }
    val OUTCOMES = ContactOutcome.Code.entries.map {
        when (it) {
            ContactOutcome.Code.COMPLIED, ContactOutcome.Code.SENT_HOME, ContactOutcome.Code.APPOINTMENT_KEPT -> generateOutcome(
                it.value,
                attendance = true,
                compliantAcceptable = true
            )

            ContactOutcome.Code.FAILED_TO_COMPLY -> generateOutcome(
                it.value,
                attendance = true,
                compliantAcceptable = false
            )

            ContactOutcome.Code.FAILED_TO_ATTEND -> generateOutcome(
                it.value,
                attendance = false,
                compliantAcceptable = false
            )

            ContactOutcome.Code.RESCHEDULED_SERVICE_REQUEST -> generateOutcome(
                it.value,
                attendance = false,
                compliantAcceptable = true
            )

            ContactOutcome.Code.WITHDRAWN -> generateOutcome(it.value, attendance = false, compliantAcceptable = true)
            else -> generateOutcome(it.value)
        }
    }.associateBy { it.code }
    val ENFORCEMENT_ACTION = generateEnforcementAction(
        EnforcementAction.Code.REFER_TO_PERSON_MANAGER.value,
        TYPES[Code.REFER_TO_PERSON_MANAGER.value]!!
    )

    var CRSAPT_NON_COMPLIANT = generate(
        type = TYPES[Code.CRSAPT.value]!!,
        notes = """
            |Service Delivery Appointment for Accommodation Referral FE4536C with Prime Provider ProviderName
            |https://refer-monitor-intervention.service.justice.gov.uk/probation-practitioner/referrals/f56c5f7c-632f-4cad-a1b3-693541cb5f22/progress
        """.trimMargin(),
        nsi = NsiGenerator.WITHDRAWN,
        rarActivity = true
    )

    var CRSAPT_COMPLIANT = generate(
        date = LocalDate.now(),
        type = TYPES[Code.CRSAPT.value]!!,
        notes = """
            |Service Delivery Appointment for Accommodation Referral FE4536C with Prime Provider ProviderName
            |https://refer-monitor-intervention.service.justice.gov.uk/probation-practitioner/referrals/f56c5f7c-632f-4cad-a1b3-693541cb5f22/progress
        """.trimMargin(),
        nsi = NsiGenerator.WITHDRAWN,
        rarActivity = true,
        externalReference = "urn:hmpps:interventions-appointment:48911ad2-1213-4bd3-8312-3824dc29f131"
    )

    var CRSAPT_NOT_ATTENDED = generate(
        type = TYPES[Code.CRSAPT.value]!!,
        date = LocalDate.now().minusDays(2),
        notes = """
            |Service Delivery Appointment for Accommodation Referral FE4536C with Prime Provider ProviderName
            |https://refer-monitor-intervention.service.justice.gov.uk/probation-practitioner/referrals/89a3f79c-f12b-43de-9616-77ae19813cfe/progress
        """.trimMargin(),
        nsi = NsiGenerator.WITHDRAWN,
        rarActivity = true
    )

    var CRSAPT_NO_SESSION = generate(
        type = TYPES[Code.CRSAPT.value]!!,
        date = LocalDate.now().minusDays(3),
        notes = """
            |Service Delivery Appointment for Accommodation Referral FE4536C with Prime Provider ProviderName
            |https://refer-monitor-intervention.service.justice.gov.uk/probation-practitioner/referrals/b408f920-a6e8-49c6-877d-6f1fa9309032/progress
        """.trimMargin(),
        nsi = NsiGenerator.WITHDRAWN,
        rarActivity = true,
        externalReference = "urn:hmpps:interventions-appointment:c8801fa4-4487-4b38-9169-efabd4be98c9"
    )

    fun generate(
        type: ContactType,
        date: LocalDate = LocalDate.now().minusDays(1),
        startTime: ZonedDateTime = ZonedDateTime.now().minusDays(1),
        endTime: ZonedDateTime = startTime.plusMinutes(45),
        nsi: Nsi? = null,
        outcome: ContactOutcome? = null,
        notes: String? = null,
        rarActivity: Boolean? = null,
        person: Person = PersonGenerator.DEFAULT,
        externalReference: String? = null,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Contact(
        person,
        type,
        date = date,
        startTime = startTime,
        endTime = endTime,
        nsiId = nsi?.id,
        eventId = nsi?.eventId,
        id = id,
        providerId = ProviderGenerator.INTENDED_PROVIDER.id,
        teamId = ProviderGenerator.INTENDED_TEAM.id,
        staffId = ProviderGenerator.INTENDED_STAFF.id,
        rarActivity = rarActivity,
        externalReference = externalReference,
        softDeleted = softDeleted
    ).addNotes(notes).apply { this.outcome = outcome }

    fun generateType(
        code: String,
        nationalStandards: Boolean = false,
        attendance: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = ContactType(code, nationalStandards, attendance, id)

    fun generateOutcome(
        code: String,
        attendance: Boolean? = null,
        compliantAcceptable: Boolean? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = ContactOutcome(code, attendance, compliantAcceptable, id)

    fun generateEnforcementAction(
        code: String,
        contactType: ContactType,
        responseByPeriod: Long? = 7,
        description: String = "Description of $code",
        id: Long = IdGenerator.getAndIncrement()
    ) = EnforcementAction(code, description, responseByPeriod, contactType, id)
}
