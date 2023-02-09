package uk.gov.justice.digital.hmpps.integrations.delius.contact

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.common.entity.contact.Contact

interface ContactRepository : JpaRepository<Contact, Long>
