package uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

interface PersonManagerRepository : JpaRepository<PersonManager, Long> {
    fun findByPersonId(personId: Long): PersonManager?
}

fun PersonManagerRepository.getByPersonId(personId: Long): PersonManager =
    findByPersonId(personId) ?: throw NotFoundException("PersonManager", "personId", personId)
