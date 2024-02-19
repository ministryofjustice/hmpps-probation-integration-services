package uk.gov.justice.digital.hmpps.integrations.common.entity.team

import org.springframework.data.jpa.repository.JpaRepository

interface TeamRepository : JpaRepository<Team, Long>
