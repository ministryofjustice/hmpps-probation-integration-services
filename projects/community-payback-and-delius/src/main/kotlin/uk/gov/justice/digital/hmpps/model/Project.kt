package uk.gov.justice.digital.hmpps.model

import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkProject
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkProjectAvailability
import java.time.LocalDate

data class Project(
    val name: String,
    val code: String,
    val type: CodeName,
    val team: CodeName,
    val provider: CodeName,
    val location: ProjectAddress?,
    val beneficiary: Beneficiary,
    val hiVisRequired: Boolean,
    val expectedEndDateExclusive: LocalDate?,
    val actualEndDateExclusive: LocalDate?,
    val availability: List<ProjectAvailabilityDetails>,
) {
    constructor(entity: UnpaidWorkProject) : this(
        name = entity.name,
        code = entity.code,
        type = CodeName(entity.projectType.description, entity.projectType.code),
        team = CodeName(entity.team.description, entity.team.code),
        provider = CodeName(entity.team.provider.description, entity.team.provider.code),
        location = entity.placementAddress?.let { ProjectAddress(it) },
        beneficiary = Beneficiary(
            name = entity.beneficiary,
            contactName = entity.beneficiaryContactName,
            emailAddress = entity.beneficiaryEmailAddress,
            website = entity.beneficiaryUrl,
            telephoneNumber = entity.beneficiaryContactAddress?.telephoneNumber,
            location = entity.beneficiaryContactAddress?.let { ProjectAddress(it) }
        ),
        hiVisRequired = entity.hiVisRequired,
        expectedEndDateExclusive = entity.expectedEndDate,
        actualEndDateExclusive = entity.completionDate,
        availability = entity.availability.map { it.toProjectAvailabilityDetails() }
    )
}