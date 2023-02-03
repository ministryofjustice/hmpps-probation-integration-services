package uk.gov.justice.digital.hmpps.data.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.controller.casedetails.entity.Disposal
import uk.gov.justice.digital.hmpps.controller.casedetails.entity.MainOffence
import uk.gov.justice.digital.hmpps.controller.casedetails.entity.Offence

interface DisposalRepository : JpaRepository<Disposal, Long>
interface MainOffenceRepository : JpaRepository<MainOffence, Long>
interface OffenceRepository : JpaRepository<Offence, Long>
