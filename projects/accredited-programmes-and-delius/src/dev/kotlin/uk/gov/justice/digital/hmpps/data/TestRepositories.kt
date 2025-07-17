package uk.gov.justice.digital.hmpps.data

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.*
import uk.gov.justice.digital.hmpps.entity.sentence.*
import uk.gov.justice.digital.hmpps.entity.sentence.custody.Custody
import uk.gov.justice.digital.hmpps.entity.sentence.custody.KeyDate
import uk.gov.justice.digital.hmpps.entity.sentence.custody.Release
import uk.gov.justice.digital.hmpps.entity.staff.*

interface ReferenceDataRepository : JpaRepository<ReferenceData, Long>
interface ProbationDeliveryUnitRepository : JpaRepository<ProbationDeliveryUnit, Long>
interface LocalAdminUnitRepository : JpaRepository<LocalAdminUnit, Long>
interface TeamRepository : JpaRepository<Team, Long>
interface StaffRepository : JpaRepository<Staff, Long>
interface ManagerRepository : JpaRepository<Manager, Long>
interface UserRepository : JpaRepository<User, Long>
interface ExclusionRepository : JpaRepository<Exclusion, Long>
interface RestrictionRepository : JpaRepository<Restriction, Long>
interface DisposalTypeRepository : JpaRepository<DisposalType, Long>
interface DisposalRepository : JpaRepository<Disposal, Long>
interface CustodyRepository : JpaRepository<Custody, Long>
interface ReleaseRepository : JpaRepository<Release, Long>
interface ContactTypeRepository : JpaRepository<ContactType, Long>
interface ContactRepository : JpaRepository<Contact, Long>
interface KeyDateRepository : JpaRepository<KeyDate, Long>
interface PssRequirementMainCategoryRepository : JpaRepository<PssRequirementMainCategory, Long>
interface PssRequirementSubCategoryRepository : JpaRepository<PssRequirementSubCategory, Long>
interface PssRequirementRepository : JpaRepository<PssRequirement, Long>
interface LicenceConditionMainCategoryRepository : JpaRepository<LicenceConditionMainCategory, Long>
interface LicenceConditionRepository : JpaRepository<LicenceCondition, Long>
interface RequirementMainCategoryRepository : JpaRepository<RequirementMainCategory, Long>
interface RequirementRepository : JpaRepository<Requirement, Long>