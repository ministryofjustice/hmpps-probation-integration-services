package uk.gov.justice.digital.hmpps.integrations.delius.recall

import org.springframework.data.jpa.repository.JpaRepository

interface RecallRepository : JpaRepository<Recall, Long>
