package uk.gov.justice.digital.hmpps.data.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffWithTeams

interface StaffWithTeamsRepository : JpaRepository<StaffWithTeams, Long>