package uk.gov.justice.digital.hmpps.integrations.delius.offender

import org.springframework.data.jpa.repository.JpaRepository

interface OffenderRepository : JpaRepository<Offender, Long>
