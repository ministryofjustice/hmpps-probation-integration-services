package uk.gov.justice.digital.hmpps.custody.date.contact

import org.springframework.data.jpa.repository.JpaRepository

interface ContactRepository : JpaRepository<Contact, Long>
