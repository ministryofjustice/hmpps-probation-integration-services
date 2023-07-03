package uk.gov.justice.digital.hmpps.integrations.delius.release

import org.springframework.data.jpa.repository.JpaRepository

interface ReleaseRepository : JpaRepository<Release, Long>
