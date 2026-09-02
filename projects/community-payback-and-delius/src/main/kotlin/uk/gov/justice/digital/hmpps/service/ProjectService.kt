package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.entity.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.entity.getProjectTypesByCodeIn
import uk.gov.justice.digital.hmpps.entity.person.Address
import uk.gov.justice.digital.hmpps.entity.staff.TeamRepository
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkProject
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkProjectRepository
import uk.gov.justice.digital.hmpps.exception.InvalidRequestException
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import uk.gov.justice.digital.hmpps.model.CreateProjectRequest
import uk.gov.justice.digital.hmpps.model.CreateProjectsRequest
import uk.gov.justice.digital.hmpps.model.Project
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.LocalDate
import java.time.ZonedDateTime

@Service
class ProjectService(
    private val unpaidWorkProjectRepository: UnpaidWorkProjectRepository,
    private val teamRepository: TeamRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val telemetryService: TelemetryService,
) {
    fun getProject(code: String) =
        unpaidWorkProjectRepository.findByCode(code)?.let { Project(it) }.orNotFoundBy("code", code)

    @Transactional
    fun createProjects(request: CreateProjectsRequest) =
        unpaidWorkProjectRepository.saveAll(validateRequest(request.projects))
            .onEach { telemetryService.trackEvent("ProjectCreated", it.telemetry()) }

    private fun validateRequest(projects: List<CreateProjectRequest>): List<UnpaidWorkProject> {
        require(projects.isNotEmpty()) { "At least one project is required" }

        // Check for duplicate project codes submitted in request
        projects.groupingBy { it.code }.eachCount().filterValues { it > 1 }.let { duplicates ->
            require(duplicates.isEmpty()) { "Duplicate project codes in request: ${duplicates.keys.sorted()}" }
        }

        // Check if projects already exist in database
        unpaidWorkProjectRepository.findByCodeIn(projects.map { it.code }).map { it.code }.let { existingCodes ->
            require(existingCodes.isEmpty()) {
                "Project codes must be unique, existing projects with codes found: ${existingCodes.sorted()}"
            }
        }

        // Validate project types in request are part of selectable reference data
        val projectTypesByCode = referenceDataRepository.getProjectTypesByCodeIn(projects.map { it.type.code })

        val teamsByCode = teamRepository.getByCodeIn(projects.map { it.team.code })

        // Map to UnpaidWorkProject Entity
        return projects.map { project ->
            val team = teamsByCode.getValue(project.team.code)
            val provider = team.provider

            // Validate project-level fields
            require(project.code.startsWith(provider.code)) {
                "Project code ${project.code} must start with Provider Code ${provider.code}"
            }
            require(project.expectedEndDate == null || !project.expectedEndDate.isBefore(project.startDate)) {
                "Expected End Date must be on or after Start Date for project code ${project.code}"
            }
            require(project.completionDate == null || !project.completionDate.isBefore(project.startDate)) {
                "Completion Date must be on or after Start Date for project code ${project.code}"
            }
            require(
                project.beneficiaryDetails.slaStartDate == null ||
                project.beneficiaryDetails.slaEndDate == null ||
                !project.beneficiaryDetails.slaEndDate.isBefore(project.beneficiaryDetails.slaStartDate)
            ) {
                "SLA End Date must be on or after SLA Start Date for project code ${project.code}"
            }

            // Validate team constraints
            require(team.unpaidWorkTeam) { "Team ${project.team.code} is not an unpaid work team" }
            require(team.endDate?.isAfter(LocalDate.now()) != false) { "Team ${project.team.code} is not active" }

            val pickupPoint = team.officeLocations
                .map { it.officeLocation }
                .filter { it.code == project.pickUpLocation.code && it.provider.code == provider.code }
                .singleOrNull { it.endDate == null || it.endDate.isAfter(ZonedDateTime.now()) }
                ?: throw InvalidRequestException(
                    "Pick Up Location Code ${project.pickUpLocation.code} is not assigned to team ${team.code} in provider ${provider.code}"
                )

            // Create Unpaid Work Project Entity
            UnpaidWorkProject(
                provider = provider,
                team = team,
                projectType = projectTypesByCode.getValue(project.type.code),
                code = project.code,
                name = project.name,
                pickupPointLocation = pickupPoint,
                hiVisRequired = project.hiVisRequired,

                beneficiary = project.beneficiaryDetails.beneficiary,
                beneficiaryContactName = project.beneficiaryDetails.contactName,
                beneficiaryEmailAddress = project.beneficiaryDetails.emailAddress,
                beneficiaryUrl = project.beneficiaryDetails.website,
                beneficiaryContactAddress = buildAddress(
                    buildingNumber = project.beneficiaryDetails.buildingNumber,
                    buildingName = project.beneficiaryDetails.buildingName,
                    county = project.beneficiaryDetails.county,
                    postcode = project.beneficiaryDetails.postcode,
                    streetName = project.beneficiaryDetails.streetName,
                    telephoneNumber = project.beneficiaryDetails.telephoneNumber,
                    townCity = project.beneficiaryDetails.townCity,
                ),
                beneficiarySla = project.beneficiaryDetails.serviceLevelAgreement,
                beneficiarySlaStartDate = project.beneficiaryDetails.slaStartDate,
                beneficiarySlaEndDate = project.beneficiaryDetails.slaEndDate,
                beneficiaryContribution = project.beneficiaryDetails.contribution,
                beneficiaryAdditionalDetails = project.beneficiaryDetails.additionalDetails,

                locationDescription = project.placementDetails.locationDescription,
                placementAddress = buildAddress(
                    buildingNumber = project.placementDetails.buildingNumber,
                    buildingName = project.placementDetails.buildingName,
                    county = project.placementDetails.county,
                    postcode = project.placementDetails.postcode,
                    streetName = project.placementDetails.streetName,
                    telephoneNumber = project.placementDetails.telephoneNumber,
                    townCity = project.placementDetails.townCity,
                ),
                placementContactName = project.placementDetails.contactName,
                placementEmailAddress = project.placementDetails.emailAddress,
                placementUrl = project.placementDetails.url,
                placementNotes = project.placementDetails.placementNotes,

                reportToSite = project.reportToSite,
                actualStartDate = project.startDate,
                expectedEndDate = project.expectedEndDate,
                completionDate = project.completionDate,
                availability = emptyList(),
            )
        }
    }

    private fun buildAddress(
        buildingNumber: String?,
        buildingName: String?,
        county: String?,
        postcode: String?,
        streetName: String?,
        telephoneNumber: String?,
        townCity: String?,
    ) = Address(
        addressNumber = buildingNumber,
        buildingName = buildingName,
        county = county,
        postcode = postcode,
        streetName = streetName,
        telephoneNumber = telephoneNumber,
        town = townCity,
    )
}