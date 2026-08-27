package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.entity.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.entity.getProjectTypesByCodeIn
import uk.gov.justice.digital.hmpps.entity.person.Address
import uk.gov.justice.digital.hmpps.entity.staff.LocalAdminUnitRepository
import uk.gov.justice.digital.hmpps.entity.staff.ProbationDeliveryUnitRepository
import uk.gov.justice.digital.hmpps.entity.staff.ProviderRepository
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
    private val providerRepository: ProviderRepository,
    private val localAdminUnitRepository: LocalAdminUnitRepository,
    private val probationDeliveryUnitRepository: ProbationDeliveryUnitRepository,
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
        if (projects.isEmpty()) {
            throw InvalidRequestException("At least one project is required")
        }

        // Validate mandatory fields for each project
        projects.forEach { project ->
            if (project.providerCode.isBlank()) {
                throw InvalidRequestException("Provider Code must not be blank for project ${project.code}")
            }
            if (project.teamCode.isBlank()) {
                throw InvalidRequestException("Team Code must not be blank for project ${project.code}")
            }
            if (project.projectTypeCode.isBlank()) {
                throw InvalidRequestException("Project Type Code must not be blank for project ${project.code}")
            }
            if (project.code.isBlank()) {
                throw InvalidRequestException("Project code must not be blank for project ${project.code}")
            }
            if (project.name.isBlank()) {
                throw InvalidRequestException("Project name must not be blank for project ${project.code}")
            }
            if (project.defaultPickupPointCode.isBlank()) {
                throw InvalidRequestException("Default Pickup Point Code must not be blank for project ${project.code}")
            }

            if (!project.code.startsWith(project.providerCode)) {
                throw InvalidRequestException(
                    "Project code ${project.code} must start with Provider Code ${project.providerCode}"
                )
            }
            if (project.expectedEndDate != null && project.expectedEndDate.isBefore(project.startDate)) {
                throw InvalidRequestException(
                    "Expected End Date must be on or after Start Date for project code ${project.code}"
                )
            }
            if (project.completionDate != null && project.completionDate.isBefore(project.startDate)) {
                throw InvalidRequestException(
                    "Completion Date must be on or after Start Date for project code ${project.code}"
                )
            }
            if (
                project.beneficiaryDetails.slaStartDate != null &&
                project.beneficiaryDetails.slaEndDate != null &&
                project.beneficiaryDetails.slaEndDate.isBefore(project.beneficiaryDetails.slaStartDate)
            ) {
                throw InvalidRequestException(
                    "SLA End Date must be on or after SLA Start Date for project code ${project.code}"
                )
            }
        }

        // Check for duplicate project codes submitted in request
        val duplicateCodes = projects
            .groupingBy { it.code }
            .eachCount()
            .filterValues { it > 1 }
            .keys
            .sorted()
        if (duplicateCodes.isNotEmpty()) {
            throw InvalidRequestException(
                "Duplicate project codes in request: ${duplicateCodes.joinToString(", ")}"
            )
        }

        // Check if projects already exist in database
        val existingCodes = unpaidWorkProjectRepository
            .findByCodeIn(projects.map { it.code })
            .map { it.code }
            .sorted()
        if (existingCodes.isNotEmpty()) {
            throw InvalidRequestException(
                "Project codes must be unique, existing projects with codes found: ${existingCodes.joinToString(", ")}"
            )
        }

        // Validate project types in request are part of selectable reference data
        val projectTypesByCode = referenceDataRepository.getProjectTypesByCodeIn(projects.map { it.projectTypeCode })
        val missingProjectTypeCodes = projects.map { it.projectTypeCode }.toSet() - projectTypesByCode.keys
        if (missingProjectTypeCodes.isNotEmpty()) {
            throw InvalidRequestException(
                "Invalid Project Type Code(s): ${missingProjectTypeCodes.sorted().joinToString(", ")}"
            )
        }

        // Validate providers exist and are selectable
        val providersByCode = providerRepository.findByCodeIn(projects.map { it.providerCode }.toSet())
            .filter { it.selectable }
            .associateBy { it.code }
        val missingProviderCodes = projects.map { it.providerCode }.toSet() - providersByCode.keys
        if (missingProviderCodes.isNotEmpty()) {
            throw InvalidRequestException(
                "Invalid Provider Code(s): ${missingProviderCodes.sorted().joinToString(", ")}"
            )
        }

        val teamsByCode = teamRepository.getByCodeIn(projects.map { it.teamCode })

        // Map to UnpaidWorkProject Entity
        return projects.map { project ->
            val team = teamsByCode.getValue(project.teamCode)
            val provider = providersByCode.getValue(project.providerCode)

            // Validate team constraints
            if (!team.unpaidWorkTeam) {
                throw InvalidRequestException("Team ${project.teamCode} is not an unpaid work team")
            }

            if (team.endDate?.isAfter(LocalDate.now()) == false) {
                throw InvalidRequestException("Team ${project.teamCode} is not active")
            }

            if (team.provider.id != provider.id) {
                throw InvalidRequestException(
                    "Team ${team.code} is not part of provider ${project.providerCode} for project code ${project.code}"
                )
            }

            project.pduCode?.takeIf { it.isNotBlank() }?.let { pduCode ->
                val requestedPdu = probationDeliveryUnitRepository
                    .findByCodeAndProviderCodeAndSelectableIsTrue(pduCode, provider.code)
                    ?: throw InvalidRequestException(
                        "Invalid PDU Code $pduCode for provider ${provider.code}"
                    )

                val teamPdu = team.localAdminUnit?.probationDeliveryUnit
                if (teamPdu == null || teamPdu.id != requestedPdu.id) {
                    throw InvalidRequestException(
                        "Team ${team.code} is not part of PDU $pduCode for project code ${project.code}"
                    )
                }
            }

            project.localAdminUnitCode?.takeIf { it.isNotBlank() }?.let { localAdminUnitCode ->
                val requestedLocalAdminUnit = localAdminUnitRepository
                    .findByCodeAndProbationDeliveryUnitProviderCodeAndSelectableIsTrue(
                        localAdminUnitCode,
                        provider.code
                    )
                    ?: throw InvalidRequestException(
                        "Invalid Local Admin Unit Code $localAdminUnitCode for provider ${provider.code}"
                    )

                val teamLocalAdminUnit = team.localAdminUnit
                if (teamLocalAdminUnit == null || teamLocalAdminUnit.id != requestedLocalAdminUnit.id) {
                    throw InvalidRequestException(
                        "Team ${team.code} is not part of Local Admin Unit $localAdminUnitCode for project code ${project.code}"
                    )
                }

                project.pduCode?.takeIf { it.isNotBlank() }?.let { pduCode ->
                    if (requestedLocalAdminUnit.probationDeliveryUnit.code != pduCode) {
                        throw InvalidRequestException(
                            "Local Admin Unit $localAdminUnitCode is not part of PDU $pduCode for project code ${project.code}"
                        )
                    }
                }
            }

            val pickupPoint = team.officeLocations
                .map { it.officeLocation }
                .filter { it.code == project.defaultPickupPointCode && it.provider.code == project.providerCode }
                .singleOrNull { it.endDate == null || it.endDate.isAfter(ZonedDateTime.now()) }
                ?: throw InvalidRequestException(
                    "Default Pickup Point Code ${project.defaultPickupPointCode} is not assigned to team ${team.code} in provider ${project.providerCode}"
                )

            // Create Unpaid Work Project Entity
            UnpaidWorkProject(
                provider = provider,
                team = team,
                projectType = projectTypesByCode.getValue(project.projectTypeCode),
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
    ) =
        if (buildingNumber != null || buildingName != null || county != null || postcode != null ||
            streetName != null || telephoneNumber != null || townCity != null
        ) {
            Address(
                addressNumber = buildingNumber,
                buildingName = buildingName,
                county = county,
                postcode = postcode,
                streetName = streetName,
                telephoneNumber = telephoneNumber,
                town = townCity,
            )
        } else {
            null
        }
}