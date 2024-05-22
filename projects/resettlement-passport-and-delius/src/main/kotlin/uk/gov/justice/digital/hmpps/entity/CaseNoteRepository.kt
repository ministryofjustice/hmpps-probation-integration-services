package uk.gov.justice.digital.hmpps.entity

import org.springframework.data.jpa.repository.JpaRepository

interface CaseNoteRepository : JpaRepository<CaseNote, Long> {

    fun findByPersonIdAndTypeCode(offenderId: Long, code: String): List<CaseNote>
}

