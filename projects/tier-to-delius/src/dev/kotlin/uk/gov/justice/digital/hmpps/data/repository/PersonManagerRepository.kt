package uk.gov.justice.digital.hmpps.data

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonManager

interface PersonManagerRepository : JpaRepository<PersonManager, Long>
