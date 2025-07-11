package uk.gov.justice.digital.hmpps.data

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.*

interface ReferenceDataRepository : JpaRepository<ReferenceData, Long>
interface ProbationDeliveryUnitRepository : JpaRepository<ProbationDeliveryUnit, Long>
interface LocalAdminUnitRepository : JpaRepository<LocalAdminUnit, Long>
interface TeamRepository : JpaRepository<Team, Long>
interface StaffRepository : JpaRepository<Staff, Long>
interface ManagerRepository : JpaRepository<Manager, Long>
interface UserRepository : JpaRepository<User, Long>
interface ExclusionRepository : JpaRepository<Exclusion, Long>
interface RestrictionRepository : JpaRepository<Restriction, Long>