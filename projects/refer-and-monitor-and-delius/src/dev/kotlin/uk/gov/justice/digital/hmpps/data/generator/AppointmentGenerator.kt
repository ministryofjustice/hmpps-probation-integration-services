package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.entity.Appointment
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.entity.AppointmentOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.entity.AppointmentType
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.entity.EnforcementAction
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person

object AppointmentGenerator {
    val APPT_TYPES = AppointmentType.Code.values().map { generateType(it.name) }.associateBy { it.code }
    val APPT_OUTCOMES = AppointmentOutcome.Code.values().map { generateOutcome(it.value) }.associateBy { it.code }
    val ENFORCEMENT_ACTIONS =
        EnforcementAction.Code.values().map { generateEnforcementAction(it.value) }.associateBy { it.code }

    val CRSAPT = generate(
        APPT_TYPES[AppointmentType.Code.CRSAPT.name]!!,
        notes = """
            |Service Delivery Appointment for Accommodation Referral f56c5f7c-632f-4cad-a1b3-693541cb5f22 with Prime Provider ProviderName
            |https://refer-monitor-intervention.service.justice.gov.uk/probation-practitioner/referrals/f56c5f7c-632f-4cad-a1b3-693541cb5f22/progress
        """.trimMargin(),
        id = 1824573421
    )

    fun generate(
        type: AppointmentType,
        outcome: AppointmentOutcome? = null,
        notes: String? = null,
        person: Person = PersonGenerator.DEFAULT,
        id: Long = IdGenerator.getAndIncrement()
    ) = Appointment(
        person,
        type,
        outcome,
        notes,
        id = id
    )

    fun generateType(code: String, id: Long = IdGenerator.getAndIncrement()) = AppointmentType(code, id)
    fun generateOutcome(code: String, id: Long = IdGenerator.getAndIncrement()) = AppointmentOutcome(code, id)
    fun generateEnforcementAction(code: String, responseByPeriod: Long? = 7, id: Long = IdGenerator.getAndIncrement()) =
        EnforcementAction(code, responseByPeriod, id)
}
