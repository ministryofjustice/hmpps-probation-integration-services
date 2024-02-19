package uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

interface PersonManagerRepository : JpaRepository<PersonManager, Long> {
    fun findByPersonIdAndActiveIsTrueAndSoftDeletedIsFalse(personId: Long): PersonManager?
}

fun PersonManagerRepository.getActiveManager(personId: Long) =
    findByPersonIdAndActiveIsTrueAndSoftDeletedIsFalse(personId) ?: throw NotFoundException(
        "PersonManager",
        "personId",
        personId
    )
