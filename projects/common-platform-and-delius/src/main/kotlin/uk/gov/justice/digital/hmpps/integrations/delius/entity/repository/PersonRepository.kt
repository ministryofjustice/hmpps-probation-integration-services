package uk.gov.justice.digital.hmpps.integrations.delius.entity.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Person

interface PersonRepository : JpaRepository<Person, Long>

