package uk.gov.justice.digital.hmpps.data.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.Contact

interface ContactDevRepository : JpaRepository<Contact, Long> {
    fun findByPersonId(personId: Long): Contact
}
