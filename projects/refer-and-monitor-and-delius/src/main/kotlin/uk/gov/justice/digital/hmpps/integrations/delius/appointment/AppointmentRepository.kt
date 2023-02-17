package uk.gov.justice.digital.hmpps.integrations.delius.appointment

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.entity.Appointment
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.entity.AppointmentOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.entity.AppointmentType

interface AppointmentRepository : JpaRepository<Appointment, Long>
interface AppointmentTypeRepository : JpaRepository<AppointmentType, Long>
interface AppointmentOutcomeRepository : JpaRepository<AppointmentOutcome, Long>
