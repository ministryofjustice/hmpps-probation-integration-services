package uk.gov.justice.digital.hmpps.data

import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.Exclusion
import uk.gov.justice.digital.hmpps.integrations.delius.user.Restriction
import uk.gov.justice.digital.hmpps.user.UserRepository

@Component
@Profile("dev", "integration-test")
class LimitedAccessDataLoader(
    private val userRepository: UserRepository,
    private val personRepository: PersonRepository,
    private val exclusionRepository: ExclusionRepository,
    private val restrictionRepository: RestrictionRepository
) {
    fun loadData() {
        userRepository.saveAll(listOf(UserGenerator.LIMITED_ACCESS_USER))
        personRepository.saveAll(listOf(PersonGenerator.EXCLUSION, PersonGenerator.RESTRICTION))

        exclusionRepository.save(LimitedAccessGenerator.EXCLUSION)
        restrictionRepository.save(LimitedAccessGenerator.RESTRICTION)
    }
}

interface ExclusionRepository : JpaRepository<Exclusion, Long>
interface RestrictionRepository : JpaRepository<Restriction, Long>
