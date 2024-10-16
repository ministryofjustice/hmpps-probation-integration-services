package uk.gov.justice.digital.hmpps.integrations.delius.entity.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Team

interface PersonManagerRepository : JpaRepository<PersonManager, Long>

interface TeamRepository : JpaRepository<Team, Long> {
    fun findByCode(code: String): Team
}

interface StaffRepository : JpaRepository<Staff, Long> {
    fun findByCode(code: String): Staff
}