package uk.gov.justice.digital.hmpps.data

import UserGenerator
import jakarta.annotation.PostConstruct
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.generator.AppointmentGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.AppointmentOutcomeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.AppointmentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.AppointmentTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.EnforcementActionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.user.UserRepository

@Component
@Profile("dev", "integration-test")
class DataLoader(
    private val userRepository: UserRepository,
    private val appointmentTypeRepository: AppointmentTypeRepository,
    private val appointmentOutcomeRepository: AppointmentOutcomeRepository,
    private val enforcementActionRepository: EnforcementActionRepository,
    private val personRepository: PersonRepository,
    private val appointmentRepository: AppointmentRepository
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveUserToDb() {
        userRepository.save(UserGenerator.APPLICATION_USER)
    }

    @Transactional
    override fun onApplicationEvent(are: ApplicationReadyEvent) {
        appointmentTypeRepository.saveAll(AppointmentGenerator.APPT_TYPES.values)
        appointmentOutcomeRepository.saveAll(AppointmentGenerator.APPT_OUTCOMES.values)
        enforcementActionRepository.saveAll(AppointmentGenerator.ENFORCEMENT_ACTIONS.values)

        personRepository.saveAndFlush(PersonGenerator.DEFAULT)
        appointmentRepository.save(AppointmentGenerator.CRSAPT)
    }
}
