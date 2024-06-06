package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.PrisonStaffTeam
import uk.gov.justice.digital.hmpps.entity.StaffTeamId

interface PrisonStaffTeamRepository : JpaRepository<PrisonStaffTeam, StaffTeamId>
