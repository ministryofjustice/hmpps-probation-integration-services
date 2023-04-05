package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
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
    val TYPES = Code.values().map {
        when (it) {
            Code.CRSAPT, Code.CRSSAA -> generateType(it.value, true)
            else -> generateType(it.value)
        }
    }.associateBy { it.code }
    val OUTCOMES = ContactOutcome.Code.values().map {
        when (it) {
            ContactOutcome.Code.COMPLIED -> generateOutcome(it.value, attendance = true, compliantAcceptable = true)
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
        nsi = NsiGenerator.END_PREMATURELY,
        rarActivity = true
    )

    var CRSAPT_COMPLIANT = generate(
        type = TYPES[Code.CRSAPT.value]!!,
        notes = """
            |Service Delivery Appointment for Accommodation Referral FE4536C with Prime Provider ProviderName
            |https://refer-monitor-intervention.service.justice.gov.uk/probation-practitioner/referrals/f56c5f7c-632f-4cad-a1b3-693541cb5f22/progress
        """.trimMargin(),
        nsi = NsiGenerator.END_PREMATURELY,
        rarActivity = true
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
        id: Long = IdGenerator.getAndIncrement()
    ) = Contact(
        person,
        type,
        date = date,
        startTime = startTime,
        endTime = endTime,
        nsiId = nsi?.id,
        eventId = nsi?.eventId,
        notes = notes,
        id = id,
        providerId = ProviderGenerator.INTENDED_PROVIDER.id,
        teamId = ProviderGenerator.INTENDED_TEAM.id,
        staffId = ProviderGenerator.INTENDED_STAFF.id,
        rarActivity = rarActivity
    ).apply { this.outcome = outcome }

    fun generateType(code: String, nationalStandards: Boolean = false, id: Long = IdGenerator.getAndIncrement()) =
        ContactType(code, nationalStandards, id)

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
