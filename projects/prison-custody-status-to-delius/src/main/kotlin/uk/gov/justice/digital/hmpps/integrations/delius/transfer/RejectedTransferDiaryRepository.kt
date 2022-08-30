package uk.gov.justice.digital.hmpps.integrations.delius.transfer

import org.springframework.data.jpa.repository.JpaRepository

interface RejectedTransferDiaryRepository : JpaRepository<RejectedTransferDiary, Long>
