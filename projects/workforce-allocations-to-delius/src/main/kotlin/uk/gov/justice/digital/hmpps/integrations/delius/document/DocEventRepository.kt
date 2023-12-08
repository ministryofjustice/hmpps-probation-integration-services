package uk.gov.justice.digital.hmpps.integrations.delius.document

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.DocEvent

interface DocEventRepository : JpaRepository<DocEvent, Long> {
    fun findByPersonId(id: Long): List<DocEvent>
}
