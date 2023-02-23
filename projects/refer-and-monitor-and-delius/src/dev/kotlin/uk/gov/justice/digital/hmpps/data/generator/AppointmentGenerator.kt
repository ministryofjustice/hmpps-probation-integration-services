package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.AppointmentOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.EnforcementAction
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person

object AppointmentGenerator {
    val APPT_TYPES = ContactType.Code.values().map { generateType(it.name) }.associateBy { it.code }
    val APPT_OUTCOMES = AppointmentOutcome.Code.values().map { generateOutcome(it.value) }.associateBy { it.code }
    val ENFORCEMENT_ACTIONS =
        EnforcementAction.Code.values().map { generateEnforcementAction(it.value) }.associateBy { it.code }

    val CRSAPT = generate(
        APPT_TYPES[ContactType.Code.CRSAPT.name]!!,
        notes = """
            |Service Delivery Appointment for Accommodation Referral f56c5f7c-632f-4cad-a1b3-693541cb5f22 with Prime Provider ProviderName
            |https://refer-monitor-intervention.service.justice.gov.uk/probation-practitioner/referrals/f56c5f7c-632f-4cad-a1b3-693541cb5f22/progress
        """.trimMargin(),
        id = 1824573421
    )

    fun generate(
        type: ContactType,
        outcome: AppointmentOutcome? = null,
        notes: String? = null,
        person: Person = PersonGenerator.DEFAULT,
        id: Long = IdGenerator.getAndIncrement()
    ) = Contact(
        person,
        type,
        outcome = outcome,
        notes = notes,
        id = id,
        providerId = 101,
        teamId = 102,
        staffId = 103
    )

    fun generateType(code: String, id: Long = IdGenerator.getAndIncrement()) = ContactType(code, id)
    fun generateOutcome(code: String, id: Long = IdGenerator.getAndIncrement()) = AppointmentOutcome(code, id)
    fun generateEnforcementAction(code: String, responseByPeriod: Long? = 7, id: Long = IdGenerator.getAndIncrement()) =
        EnforcementAction(code, responseByPeriod, id)
}
