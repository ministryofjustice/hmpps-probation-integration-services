package uk.gov.justice.digital.hmpps.integrations.delius.custody

import org.springframework.data.jpa.repository.JpaRepository

interface CustodyRepository : JpaRepository<Custody, Long>
