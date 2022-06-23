package uk.gov.justice.digital.hmpps.integrations.delius.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.StaffTeam
import uk.gov.justice.digital.hmpps.integrations.delius.entity.StaffTeamId

interface StaffTeamRepository : JpaRepository<StaffTeam, StaffTeamId>
