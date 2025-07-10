package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import jakarta.transaction.Transactional
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.TestData.ETHNICITY
import uk.gov.justice.digital.hmpps.data.TestData.EXCLUSION
import uk.gov.justice.digital.hmpps.data.TestData.GENDER
import uk.gov.justice.digital.hmpps.data.TestData.LAU
import uk.gov.justice.digital.hmpps.data.TestData.MANAGER
import uk.gov.justice.digital.hmpps.data.TestData.PDU
import uk.gov.justice.digital.hmpps.data.TestData.PERSON
import uk.gov.justice.digital.hmpps.data.TestData.RESTRICTION
import uk.gov.justice.digital.hmpps.data.TestData.STAFF
import uk.gov.justice.digital.hmpps.data.TestData.TEAM
import uk.gov.justice.digital.hmpps.data.TestData.USER
import uk.gov.justice.digital.hmpps.data.TestData.USER_WITH_LIMITED_ACCESS
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.repository.PersonRepository
import uk.gov.justice.digital.hmpps.user.AuditUser
import uk.gov.justice.digital.hmpps.user.AuditUserRepository

@Component
@ConditionalOnProperty("seed.database")
class DataLoader(
    private val auditUserRepository: AuditUserRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val probationDeliveryUnitRepository: ProbationDeliveryUnitRepository,
    private val localAdminUnitRepository: LocalAdminUnitRepository,
    private val teamRepository: TeamRepository,
    private val staffRepository: StaffRepository,
    private val personRepository: PersonRepository,
    private val managerRepository: ManagerRepository,
    private val userRepository: UserRepository,
    private val exclusionRepository: ExclusionRepository,
    private val restrictionRepository: RestrictionRepository,
) : ApplicationListener<ApplicationReadyEvent> {
    @PostConstruct
    fun saveAuditUser() {
        auditUserRepository.save(AuditUser(id(), "AccreditedProgrammesAndDelius"))
    }

    @Transactional
    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        referenceDataRepository.save(GENDER)
        referenceDataRepository.save(ETHNICITY)
        probationDeliveryUnitRepository.save(PDU)
        localAdminUnitRepository.save(LAU)
        teamRepository.save(TEAM)
        staffRepository.save(STAFF)
        personRepository.save(PERSON)
        managerRepository.save(MANAGER)
        userRepository.save(USER)
        userRepository.save(USER_WITH_LIMITED_ACCESS)
        userRepository.flush()
        exclusionRepository.save(EXCLUSION)
        restrictionRepository.save(RESTRICTION)
    }
}