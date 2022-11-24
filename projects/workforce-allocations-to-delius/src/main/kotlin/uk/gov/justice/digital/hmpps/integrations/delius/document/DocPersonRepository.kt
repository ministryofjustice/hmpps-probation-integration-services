package uk.gov.justice.digital.hmpps.integrations.delius.document

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.DocPerson

interface DocPersonRepository : JpaRepository<DocPerson, Long> {
    fun findByCrn(crn: String): DocPerson?

    fun findByCrnAndPreconDocId(crn: String, preconDocId: String): DocPerson?
}
