package uk.gov.justice.digital.hmpps.integrations.common.entity.person

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

interface PersonWithManagerRepository : JpaRepository<PersonWithManager, Long> {
    fun findByCrnAndSoftDeletedIsFalse(crn: String): PersonWithManager?
}

fun PersonWithManagerRepository.getByCrnAndSoftDeletedIsFalse(crn: String): PersonWithManager =
    findByCrnAndSoftDeletedIsFalse(crn) ?: throw NotFoundException("Person", "crn", crn)
