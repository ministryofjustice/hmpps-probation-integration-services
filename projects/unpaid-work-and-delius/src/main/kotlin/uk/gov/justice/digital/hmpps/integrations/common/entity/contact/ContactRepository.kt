package uk.gov.justice.digital.hmpps.integrations.common.entity.contact

import org.springframework.data.jpa.repository.JpaRepository

interface ContactRepository : JpaRepository<Contact, Long>
