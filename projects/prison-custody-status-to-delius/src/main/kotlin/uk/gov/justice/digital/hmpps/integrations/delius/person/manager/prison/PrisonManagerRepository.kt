package uk.gov.justice.digital.hmpps.integrations.delius.person.manager.prison

import org.springframework.data.jpa.repository.JpaRepository

interface PrisonManagerRepository : JpaRepository<PrisonManager, Long> {
    fun findFirstByPersonIdAndActiveTrueOrderByDateDesc(personId: Long): PrisonManager?
}
