package uk.gov.justice.digital.hmpps.data.entity

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.contact.Enforcement

interface EnforcementRepository : JpaRepository<Enforcement, Long>