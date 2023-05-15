package uk.gov.justice.digital.hmpps.data

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator.generateExclusion
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator.generateRestriction
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.access.entity.Exclusion
import uk.gov.justice.digital.hmpps.integrations.delius.user.access.entity.Restriction
import uk.gov.justice.digital.hmpps.user.UserRepository

@Component
@ConditionalOnProperty("seed.database")
class LimitedAccessDataLoader(
    private val userRepository: UserRepository,
    private val personRepository: PersonRepository,
    private val exclusionRepository: ExclusionRepository,
    private val restrictionRepository: RestrictionRepository
) {
    fun loadData() {
        userRepository.saveAll(listOf(UserGenerator.LIMITED_ACCESS_USER))
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

interface ExclusionRepository : JpaRepository<Exclusion, Long>
interface RestrictionRepository : JpaRepository<Restriction, Long>
