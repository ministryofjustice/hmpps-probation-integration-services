package uk.gov.justice.digital.hmpps.integrations.delius.custody.history

import org.springframework.data.jpa.repository.JpaRepository

interface CustodyHistoryRepository : JpaRepository<CustodyHistory, Long>
