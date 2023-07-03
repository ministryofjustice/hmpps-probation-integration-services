package uk.gov.justice.digital.hmpps.data.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.caseview.CaseViewDisposal
import uk.gov.justice.digital.hmpps.integrations.delius.caseview.CaseViewEvent
import uk.gov.justice.digital.hmpps.integrations.delius.caseview.CaseViewMainOffence
import uk.gov.justice.digital.hmpps.integrations.delius.caseview.CaseViewOffence
import uk.gov.justice.digital.hmpps.integrations.delius.caseview.CaseViewPersonAddress
import uk.gov.justice.digital.hmpps.integrations.delius.caseview.CaseViewRequirementMainCategory

interface CaseViewPersonAddressRepository : JpaRepository<CaseViewPersonAddress, Long>
interface CaseViewOffenceRepository : JpaRepository<CaseViewOffence, Long>
interface CaseViewEventRepository : JpaRepository<CaseViewEvent, Long>
interface CaseViewDisposalRepository : JpaRepository<CaseViewDisposal, Long>
interface CaseViewMainOffenceRepository : JpaRepository<CaseViewMainOffence, Long>
interface CaseViewRequirementMainCategoryRepository : JpaRepository<CaseViewRequirementMainCategory, Long>
