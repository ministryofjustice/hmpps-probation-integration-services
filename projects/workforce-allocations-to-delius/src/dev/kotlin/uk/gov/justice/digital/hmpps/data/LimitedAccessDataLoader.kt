package uk.gov.justice.digital.hmpps.data

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator.generateExclusion
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator.generateRestriction
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.ExclusionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.RestrictionRepository
import uk.gov.justice.digital.hmpps.user.AuditUserRepository

@Component
@ConditionalOnProperty("seed.database")
class LimitedAccessDataLoader(
    private val auditUserRepository: AuditUserRepository,
    private val personRepository: PersonRepository,
    private val exclusionRepository: ExclusionRepository,
    private val restrictionRepository: RestrictionRepository
) {
    fun loadData() {
        auditUserRepository.saveAll(listOf(UserGenerator.LIMITED_ACCESS_USER))
        personRepository.saveAll(
            listOf(
                PersonGenerator.EXCLUSION,
                PersonGenerator.RESTRICTION,
                PersonGenerator.RESTRICTION_EXCLUSION
            )
        )

        exclusionRepository.save(LimitedAccessGenerator.EXCLUSION)
        restrictionRepository.save(LimitedAccessGenerator.RESTRICTION)
        exclusionRepository.save(generateExclusion(person = PersonGenerator.RESTRICTION_EXCLUSION))
        restrictionRepository.save(generateRestriction(person = PersonGenerator.RESTRICTION_EXCLUSION))
    }
}
