package uk.gov.justice.digital.hmpps.integrations.delius.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.CaseNoteNomisType

interface CaseNoteNomisTypeRepository : JpaRepository<CaseNoteNomisType, String>
