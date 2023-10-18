package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator.EXCLUDED_CASE
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator.FULL_ACCESS_USER
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator.LIMITED_ACCESS_USER
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator.RESTRICTED_CASE
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator.UNLIMITED_ACCESS
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.entity.Exclusion
import uk.gov.justice.digital.hmpps.entity.LimitedAccessPerson
import uk.gov.justice.digital.hmpps.entity.Restriction
import uk.gov.justice.digital.hmpps.entity.UserAccessRepository
import uk.gov.justice.digital.hmpps.user.AuditUserRepository

@Component
@ConditionalOnProperty("seed.database")
class DataLoader(
    private val auditUserRepository: AuditUserRepository,
    private val userAccessRepository: UserAccessRepository,
    private val limitedAccessPersonRepository: LimitedAccessPersonRepository,
    private val exclusionRepository: ExclusionRepository,
    private val restrictionRepository: RestrictionRepository
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveAuditUser() {
        auditUserRepository.save(UserGenerator.AUDIT_USER)
    }

    override fun onApplicationEvent(are: ApplicationReadyEvent) {
        listOf(FULL_ACCESS_USER, LIMITED_ACCESS_USER).apply(userAccessRepository::saveAll)
        listOf(UNLIMITED_ACCESS, EXCLUDED_CASE, RESTRICTED_CASE).apply(limitedAccessPersonRepository::saveAll)
        exclusionRepository.save(LimitedAccessGenerator.generateExclusion(EXCLUDED_CASE))
        restrictionRepository.save(LimitedAccessGenerator.generateRestriction(RESTRICTED_CASE))
    }
}

interface LimitedAccessPersonRepository : JpaRepository<LimitedAccessPerson, Long>
interface ExclusionRepository : JpaRepository<Exclusion, Long>
interface RestrictionRepository : JpaRepository<Restriction, Long>
