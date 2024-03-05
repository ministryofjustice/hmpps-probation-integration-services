package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.entity.Category
import uk.gov.justice.digital.hmpps.entity.Level
import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.user.AuditUserRepository
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

@Component
@ConditionalOnProperty("seed.database")
class DataLoader(
    private val auditUserRepository: AuditUserRepository,
    private val em: EntityManager
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveAuditUser() {
        auditUserRepository.save(UserGenerator.AUDIT_USER)
    }

    @Transactional
    override fun onApplicationEvent(are: ApplicationReadyEvent) {
        em.saveAll(
            NSITypeGenerator.DTR,
            NSIStatusGenerator.INITIATED,
            ReferenceDataGenerator.ADDRESS_STATUS,
            ReferenceDataGenerator.DTR_SUB_TYPE,
            ProviderGenerator.DEFAULT_AREA,
            ProviderGenerator.DEFAULT_TEAM,
            ProviderGenerator.DEFAULT_STAFF,
            ProviderGenerator.DEFAULT_STAFF_USER,
            PersonGenerator.DEFAULT,
            PersonGenerator.DEFAULT_MANAGER,
            NSIGenerator.DEFAULT,
            NSIManagerGenerator.DEFAULT,
            AddressGenerator.DEFAULT,
            AppointmentGenerator.ATTENDANCE_TYPE,
            AppointmentGenerator.NON_ATTENDANCE_TYPE,
            AppointmentGenerator.ATTENDED_OUTCOME,
            AppointmentGenerator.NON_ATTENDED_OUTCOME,
            AppointmentGenerator.DEFAULT_LOCATION,
            RegistrationGenerator.MAPPA_TYPE
        )

        RegistrationGenerator.CATEGORIES.values.forEach { em.persist(it) }
        RegistrationGenerator.LEVELS.values.forEach { em.persist(it) }
        em.persist(
            RegistrationGenerator.generate(
                date = LocalDate.now().minusDays(30),
                category = RegistrationGenerator.CATEGORIES[Category.M1.name],
                level = RegistrationGenerator.LEVELS[Level.M2.name],
                reviewDate = LocalDate.now().plusDays(60)
            )
        )

        createAppointments(PersonGenerator.DEFAULT)

        createAppointmentData()
    }

    fun createAppointmentData() {
        val conflictPerson = PersonGenerator.CREATE_APPOINTMENT
        val conflictManager = PersonGenerator.generateManager(PersonGenerator.CREATE_APPOINTMENT)
        em.saveAll(
            AppointmentGenerator.APPOINTMENT_TYPE,
            conflictPerson,
            conflictManager,
            AppointmentGenerator.generate(
                conflictPerson,
                AppointmentGenerator.ATTENDANCE_TYPE,
                LocalDate.now().plusDays(7),
                ZonedDateTime.now().plusDays(7),
                ZonedDateTime.now().plusDays(7).plusHours(1),
                location = null,
                probationAreaId = conflictManager.probationAreaId,
                team = conflictManager.team,
                staff = conflictManager.staff
            )
        )
    }

    fun EntityManager.saveAll(vararg any: Any) = any.forEach { persist(it) }

    private fun createAppointments(person: Person) {
        dates().flatMap { date ->
            times().map {
                val start = ZonedDateTime.of(date, it, EuropeLondon)
                AppointmentGenerator.generate(
                    person,
                    AppointmentGenerator.ATTENDANCE_TYPE,
                    date,
                    start,
                    start.plusMinutes(30),
                    probationAreaId = ProviderGenerator.DEFAULT_AREA.id,
                    team = ProviderGenerator.DEFAULT_TEAM,
                    staff = ProviderGenerator.DEFAULT_STAFF,
                    location = if (start.minute == 30) AppointmentGenerator.DEFAULT_LOCATION else null,
                    description = if (start.minute == 0) {
                        "On the hour"
                    } else {
                        null
                    },
                    outcome = if (start.isAfter(ZonedDateTime.now())) {
                        null
                    } else {
                        AppointmentGenerator.ATTENDED_OUTCOME
                    }
                )
            }
        }.forEach { em.persist(it) }
    }

    private fun dates(): List<LocalDate> {
        return (-7L..14L).map { LocalDate.now().plusDays(it) }
    }

    private fun times(): List<LocalTime> = listOf(
        LocalTime.of(9, 0),
        LocalTime.of(10, 30),
        LocalTime.of(14, 30),
        LocalTime.of(17, 0),
        LocalTime.of(23, 45)
    )
}
