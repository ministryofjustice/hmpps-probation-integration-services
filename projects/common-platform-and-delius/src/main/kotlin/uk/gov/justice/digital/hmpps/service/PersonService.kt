package uk.gov.justice.digital.hmpps.service

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.SqlOutParameter
import org.springframework.jdbc.core.simple.SimpleJdbcCall
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Equality
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.entity.repository.*
import java.sql.Types
import java.time.LocalDateTime

@Service
class PersonService(
    jdbcTemplate: JdbcTemplate,
    auditedInteractionService: AuditedInteractionService,
    private val personRepository: PersonRepository,
    private val courtRepository: CourtRepository,
    private val equalityRepository: EqualityRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val teamRepository: TeamRepository,
    private val staffRepository: StaffRepository,
    private val referenceDataRepository: ReferenceDataRepository,
) : AuditableService(auditedInteractionService) {

    private val generateCrn = SimpleJdbcCall(jdbcTemplate)
        .withCatalogName("offender_support_api")
        .withProcedureName("getNextCRN")
        .withoutProcedureColumnMetaDataAccess()
        .declareParameters(
            SqlOutParameter("CRN", Types.VARCHAR)
        )

    @Transactional
    fun insertPerson(person: Person, courtCode: String) = audit(BusinessInteractionCode.INSERT_PERSON) { audit ->
        // Person record
        val savedPerson = personRepository.save(person)

        val courtLinkedProvider = courtRepository.findByNationalCourtCode(courtCode).probationArea
        val initialAllocation = referenceDataRepository.initialAllocationReason()
        val unallocatedTeam = teamRepository.findByCode(courtLinkedProvider.code + "UAT")
        val unallocatedStaff = staffRepository.findByCode(unallocatedTeam.code + "U")

        // Person manager record
        val manager = PersonManager(
            person = savedPerson,
            staff = unallocatedStaff,
            team = unallocatedTeam,
            provider = courtLinkedProvider,
            softDeleted = false,
            active = true,
            allocationReason = initialAllocation,
            staffEmployeeID = unallocatedStaff.id,
            trustProviderTeamId = unallocatedTeam.id,
            allocationDate = LocalDateTime.of(1900, 1, 1, 0, 0)

        )
        personManagerRepository.save(manager)

        // Equality record
        val equality = Equality(
            id = null,
            personId = savedPerson.id!!,
            softDeleted = false,
        )

        equalityRepository.save(equality)

        audit["offenderId"] = savedPerson.id
    }

    fun generateCrn(): String {
        return generateCrn.execute()["CRN"] as String
    }
}