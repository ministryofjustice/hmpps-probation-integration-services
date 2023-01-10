package uk.gov.justice.digital.hmpps.data.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.MainOffence
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.Offence

interface OffenceRepository : JpaRepository<Offence, Long>

interface MainOffenceRepository : JpaRepository<MainOffence, Long>
