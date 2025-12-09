package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.sentence.component.transfer.RejectedTransferDiary

interface RejectedTransferDiaryRepository : JpaRepository<RejectedTransferDiary, Long>
