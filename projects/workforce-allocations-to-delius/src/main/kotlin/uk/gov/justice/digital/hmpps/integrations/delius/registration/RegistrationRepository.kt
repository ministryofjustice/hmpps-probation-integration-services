package uk.gov.justice.digital.hmpps.integrations.delius.registration

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.api.model.RegisterFlag
import uk.gov.justice.digital.hmpps.integrations.delius.registration.entity.Registration

interface RegistrationRepository : JpaRepository<Registration, Long> {
    fun findAllByPersonCrn(crn: String): List<Registration>

    @EntityGraph(attributePaths = ["person", "registerType.flag"])
    fun findAllByPersonCrnAndRegisterTypeFlagCodeIn(crn: String, flags: List<String>): List<Registration>
}

fun RegistrationRepository.getFlagsForCrn(crn: String, flags: List<String> = RegisterFlag.entries.map { it.code }) =
    findAllByPersonCrnAndRegisterTypeFlagCodeIn(crn, flags)