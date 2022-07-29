package uk.gov.justice.digital.hmpps.integrations.delius.provider

import org.springframework.data.jpa.repository.JpaRepository

interface TeamRepository : JpaRepository<Team, Long> {
    fun findByCodeAndProviderId(code: String, providerId: Long): Team?
}
