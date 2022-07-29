package uk.gov.justice.digital.hmpps.data.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffTeam
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffTeamId

interface StaffTeamRepository : JpaRepository<StaffTeam, StaffTeamId>
