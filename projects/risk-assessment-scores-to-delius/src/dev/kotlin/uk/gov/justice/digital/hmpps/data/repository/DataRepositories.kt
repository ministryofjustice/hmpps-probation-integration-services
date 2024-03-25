package uk.gov.justice.digital.hmpps.data.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Dataset
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.entity.DisposalType
import uk.gov.justice.digital.hmpps.integrations.delius.entity.MainOffence
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Offence

interface DatasetRepository : JpaRepository<Dataset, Long>
interface OffenceRepository : JpaRepository<Offence, Long>
interface DisposalRepository : JpaRepository<Disposal, Long>
interface DisposalTypeRepository : JpaRepository<DisposalType, Long>
interface MainOffenceRepository : JpaRepository<MainOffence, Long>
