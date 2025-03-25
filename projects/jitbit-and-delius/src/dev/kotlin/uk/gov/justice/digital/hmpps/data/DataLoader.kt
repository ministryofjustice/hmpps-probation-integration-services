package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.AddressGenerator
import uk.gov.justice.digital.hmpps.data.generator.LaoGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.entity.Exclusion
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.Restriction
import uk.gov.justice.digital.hmpps.repository.AddressRepository
import uk.gov.justice.digital.hmpps.repository.PersonRepository
import uk.gov.justice.digital.hmpps.user.AuditUserRepository

@Component
@ConditionalOnProperty("seed.database")
class DataLoader(
    private val auditUserRepository: AuditUserRepository,
    private val personRepository: PersonRepository,
    private val addressRepository: AddressRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val exclusionRepository: ExclusionRepository,
    private val restrictionRepository: RestrictionRepository,
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveAuditUser() {
        auditUserRepository.save(UserGenerator.AUDIT_USER)
        auditUserRepository.save(UserGenerator.LIMITED_ACCESS_USER)
    }

    override fun onApplicationEvent(are: ApplicationReadyEvent) {
        personRepository.saveAll(
            listOf(
                PersonGenerator.DEFAULT, PersonGenerator.BASIC, PersonGenerator.EXCLUSION,
                PersonGenerator.RESTRICTION, PersonGenerator.RESTRICTION_EXCLUSION
            )
        )
        referenceDataRepository.save(AddressGenerator.MAIN_STATUS)
        addressRepository.save(AddressGenerator.DEFAULT)
        exclusionRepository.saveAll(listOf(LaoGenerator.EXCLUSION, LaoGenerator.BOTH_EXCLUSION))
        restrictionRepository.saveAll(listOf(LaoGenerator.RESTRICTION, LaoGenerator.BOTH_RESTRICTION))
    }
}

interface ReferenceDataRepository : JpaRepository<ReferenceData, Long>
interface ExclusionRepository : JpaRepository<Exclusion, Long>
interface RestrictionRepository : JpaRepository<Restriction, Long>
