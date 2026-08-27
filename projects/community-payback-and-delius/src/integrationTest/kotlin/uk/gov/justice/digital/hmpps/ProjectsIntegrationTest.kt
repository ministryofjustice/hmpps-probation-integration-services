package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import uk.gov.justice.digital.hmpps.advice.ErrorResponse
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator.OTHER_PROVIDER_TEAM
import uk.gov.justice.digital.hmpps.data.generator.UPWGenerator.DEFAULT_OFFICE_LOCATION
import uk.gov.justice.digital.hmpps.data.generator.UPWGenerator.SECOND_PROVIDER_OFFICE_LOCATION
import uk.gov.justice.digital.hmpps.data.generator.UPWGenerator.UPW_PROJECT_1
import uk.gov.justice.digital.hmpps.data.generator.UPWGenerator.UPW_PROJECT_AVAILABILITY_1
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkProjectRepository
import uk.gov.justice.digital.hmpps.model.CreateProjectBeneficiaryDetails
import uk.gov.justice.digital.hmpps.model.CreateProjectPlacementDetails
import uk.gov.justice.digital.hmpps.model.CreateProjectRequest
import uk.gov.justice.digital.hmpps.model.CreateProjectsRequest
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.json
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@SpringBootTest
@AutoConfigureMockMvc
class ProjectsIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val unpaidWorkProjectRepository: UnpaidWorkProjectRepository
) {
    @Test
    fun `can retrieve project details`() {
        mockMvc
            .get("/projects/${UPW_PROJECT_1.code}") { withToken() }
            .andExpect {
                status { is2xxSuccessful() }
                content {
                    json(
                        """
                        {
                          "name": "Default UPW Project",
                          "code": "N01P01",
                          "type": {
                            "name": "Individual Placement",
                            "code": "I"
                          },
                          "team": {
                            "name": "N01 UPW Team",
                            "code": "N01UPW"
                          },
                          "provider": {
                            "name": "N01 Provider",
                            "code": "N01"
                          },
                          "location": {
                            "streetName": "Test Street",
                            "addressNumber": "123",
                            "townCity": "Town",
                            "postCode": "AB12CD"
                          },
                          "beneficiary": {
                            "name": "Beneficiary",
                            "contactName": "Joe Bloggs",
                            "emailAddress": "joebloggs@example.com",
                            "website": "https://example.com",
                            "location": {
                              "streetName": "Test Street",
                              "addressNumber": "123",
                              "townCity": "Town",
                              "postCode": "AB12CD"
                            }
                          },
                          "hiVisRequired": false,
                          "expectedEndDateExclusive": "${UPW_PROJECT_1.expectedEndDate}",
                          "availability": [
                            {
                              "frequency": "Weekly",
                              "dayOfWeek": "MONDAY",
                              "startDateInclusive": "${UPW_PROJECT_AVAILABILITY_1.startDate}",
                              "endDateExclusive": "${UPW_PROJECT_AVAILABILITY_1.endDate}",
                              "startTime": "${UPW_PROJECT_AVAILABILITY_1.startTime!!.format(DateTimeFormatter.ISO_LOCAL_TIME)}",
                              "endTime": "${UPW_PROJECT_AVAILABILITY_1.endTime!!.format(DateTimeFormatter.ISO_LOCAL_TIME)}"
                            }
                          ]
                        }
                        """.trimIndent(), JsonCompareMode.STRICT
                    )
                }
            }
    }

    @Test
    fun `returns 404 for unknown project code`() {
        mockMvc
            .get("/projects/DOESNOTEXIST") { withToken() }
            .andExpect { status { isNotFound() } }
            .andReturn().response.contentAsJson<ErrorResponse>().also {
                assertThat(it.message).contains("Project with code of DOESNOTEXIST not found")
            }
    }

    @Test
    fun `returns 400 when project list is empty`() {
        mockMvc.post("/projects") {
            withToken()
            json = CreateProjectsRequest(emptyList())
        }.andExpect { status { isBadRequest() } }.andReturn().response.contentAsJson<ErrorResponse>().also {
            assertThat(it.message).isEqualTo("At least one project is required")
        }
    }

    @Test
    fun `returns 400 when required project field is blank`() {
        mockMvc.post("/projects") {
            withToken()
            json = CreateProjectsRequest(
                projects = listOf(
                    CreateProjectRequest(
                        providerCode = ProviderGenerator.DEFAULT_PROVIDER.code,
                        code = "",
                        name = "ETE Community Campus - Alison Courses",
                        teamCode = TeamGenerator.DEFAULT_UPW_TEAM.code,
                        projectTypeCode = ReferenceDataGenerator.ELEARNING_PROJECT_TYPE.code,
                        defaultPickupPointCode = DEFAULT_OFFICE_LOCATION.code,
                        startDate = LocalDate.now(),
                    )
                )
            )
        }.andExpect { status { isBadRequest() } }.andReturn().response.contentAsJson<ErrorResponse>().also {
            assertThat(it.message).contains("Project code must not be blank for project")
        }
    }

    @Test
    fun `returns 400 when request includes existing project codes`() {
        val existingCode = UPW_PROJECT_1.code
        val newCode = "N02BULK1"
        val request = CreateProjectsRequest(
            projects = listOf(
                CreateProjectRequest(
                    providerCode = UPW_PROJECT_1.team.provider.code,
                    code = existingCode,
                    name = "Test New Project",
                    teamCode = UPW_PROJECT_1.team.code,
                    projectTypeCode = UPW_PROJECT_1.projectType.code,
                    defaultPickupPointCode = DEFAULT_OFFICE_LOCATION.code,
                    hiVisRequired = true,
                    startDate = LocalDate.now(),
                ), CreateProjectRequest(
                    providerCode = ProviderGenerator.SECOND_PROVIDER.code,
                    code = newCode,
                    name = "ETE Community Campus - Health & Safety - Mandatory",
                    teamCode = OTHER_PROVIDER_TEAM.code,
                    projectTypeCode = ReferenceDataGenerator.ELEARNING_PROJECT_TYPE.code,
                    defaultPickupPointCode = SECOND_PROVIDER_OFFICE_LOCATION.code,
                    hiVisRequired = true,
                    reportToSite = true,
                    startDate = LocalDate.now(),
                    beneficiaryDetails = CreateProjectBeneficiaryDetails(
                        beneficiary = "Beneficiary Name",
                        contactName = "Beneficiary Contact",
                        telephoneNumber = "01234123456",
                    ),
                    placementDetails = CreateProjectPlacementDetails(
                        buildingNumber = "100",
                        streetName = "Main Street",
                        townCity = "Town",
                        postcode = "AB1 2CD",
                    ),
                )
            )
        )

        val countBefore = unpaidWorkProjectRepository.count()

        mockMvc.post("/projects") {
            withToken()
            json = request
        }.andExpect { status { isBadRequest() } }.andReturn().response.contentAsJson<ErrorResponse>().also {
            assertThat(it.message).isEqualTo("Project codes must be unique, existing projects with codes found: $existingCode")
        }

        assertThat(unpaidWorkProjectRepository.count()).isEqualTo(countBefore)
        assertThat(unpaidWorkProjectRepository.findByCode(newCode)).isNull()
        assertThat(unpaidWorkProjectRepository.findByCode(existingCode)).isNotNull
    }

    @Test
    fun `returns 400 when request contains duplicate project codes`() {
        val existingCode = UPW_PROJECT_1.code
        val newCode = "N02BULK2"
        val request = CreateProjectsRequest(
            projects = listOf(
                CreateProjectRequest(
                    providerCode = UPW_PROJECT_1.team.provider.code,
                    code = existingCode,
                    name = UPW_PROJECT_1.name,
                    teamCode = UPW_PROJECT_1.team.code,
                    projectTypeCode = UPW_PROJECT_1.projectType.code,
                    defaultPickupPointCode = DEFAULT_OFFICE_LOCATION.code,
                    startDate = LocalDate.now(),
                ),
                CreateProjectRequest(
                    providerCode = UPW_PROJECT_1.team.provider.code,
                    code = existingCode,
                    name = "Project 1",
                    teamCode = UPW_PROJECT_1.team.code,
                    projectTypeCode = UPW_PROJECT_1.projectType.code,
                    defaultPickupPointCode = DEFAULT_OFFICE_LOCATION.code,
                    startDate = LocalDate.now(),
                ),
                CreateProjectRequest(
                    providerCode = ProviderGenerator.SECOND_PROVIDER.code,
                    code = newCode,
                    name = "ETE Community Campus - Manual Handling - Mandatory",
                    teamCode = OTHER_PROVIDER_TEAM.code,
                    projectTypeCode = ReferenceDataGenerator.ELEARNING_PROJECT_TYPE.code,
                    defaultPickupPointCode = SECOND_PROVIDER_OFFICE_LOCATION.code,
                    startDate = LocalDate.now(),
                ),
                CreateProjectRequest(
                    providerCode = ProviderGenerator.SECOND_PROVIDER.code,
                    code = newCode,
                    name = "Project 2",
                    teamCode = OTHER_PROVIDER_TEAM.code,
                    projectTypeCode = ReferenceDataGenerator.ELEARNING_PROJECT_TYPE.code,
                    defaultPickupPointCode = SECOND_PROVIDER_OFFICE_LOCATION.code,
                    startDate = LocalDate.now(),
                ),
            )
        )

        val countBefore = unpaidWorkProjectRepository.count()

        mockMvc.post("/projects") {
            withToken()
            json = request
        }.andExpect { status { isBadRequest() } }.andReturn().response.contentAsJson<ErrorResponse>().also {
            assertThat(it.message).isEqualTo("Duplicate project codes in request: $existingCode, $newCode")
        }

        assertThat(unpaidWorkProjectRepository.count()).isEqualTo(countBefore)
        assertThat(unpaidWorkProjectRepository.findByCode(newCode)).isNull()
    }

    @Test
    fun `can create a project`() {
        val newCode = "N02BULK4"
        val request = CreateProjectsRequest(
            projects = listOf(
                CreateProjectRequest(
                    providerCode = ProviderGenerator.SECOND_PROVIDER.code,
                    code = newCode,
                    name = "ETE Community Campus - Manual Handling - Mandatory",
                    teamCode = OTHER_PROVIDER_TEAM.code,
                    projectTypeCode = ReferenceDataGenerator.ELEARNING_PROJECT_TYPE.code,
                    defaultPickupPointCode = SECOND_PROVIDER_OFFICE_LOCATION.code,
                    hiVisRequired = true,
                    reportToSite = true,
                    startDate = LocalDate.now(),
                    beneficiaryDetails = CreateProjectBeneficiaryDetails(
                        beneficiary = "Beneficiary Name",
                        contactName = "Beneficiary Contact",
                        telephoneNumber = "01234123456",
                    ),
                    placementDetails = CreateProjectPlacementDetails(
                        buildingNumber = "100",
                        streetName = "Main Street",
                        townCity = "Town",
                        postcode = "AB1 2CD",
                    ),
                )
            )
        )

        mockMvc.post("/projects") {
            withToken()
            json = request
        }.andExpect {
            status { isCreated() }
        }

        unpaidWorkProjectRepository.findByCode(newCode).also {
            assertThat(it).isNotNull
            assertThat(it!!.reportToSite).isTrue
            assertThat(it.placementAddress?.addressNumber).isEqualTo("100")
            assertThat(it.beneficiaryContactAddress?.telephoneNumber).isEqualTo("01234123456")
        }
    }

    @Test
    fun `can create multiple projects`() {
        val firstCode = "N02BULK5"
        val secondCode = "N02BULK6"
        val startDate = LocalDate.now()
        val expectedEndDate = startDate.plusDays(30)
        val completionDate = startDate.plusDays(60)
        val request = CreateProjectsRequest(
            projects = listOf(
                CreateProjectRequest(
                    providerCode = ProviderGenerator.SECOND_PROVIDER.code,
                    pduCode = PduGenerator.SECOND_PROVIDER_PDU.code,
                    localAdminUnitCode = LauGenerator.SECOND_PROVIDER_LOCAL_ADMIN_UNIT.code,
                    code = firstCode,
                    name = "Minimal Project",
                    teamCode = OTHER_PROVIDER_TEAM.code,
                    projectTypeCode = ReferenceDataGenerator.ELEARNING_PROJECT_TYPE.code,
                    defaultPickupPointCode = SECOND_PROVIDER_OFFICE_LOCATION.code,
                    startDate = startDate,
                ),
                CreateProjectRequest(
                    providerCode = ProviderGenerator.SECOND_PROVIDER.code,
                    pduCode = PduGenerator.SECOND_PROVIDER_PDU.code,
                    localAdminUnitCode = LauGenerator.SECOND_PROVIDER_LOCAL_ADMIN_UNIT.code,
                    code = secondCode,
                    name = "Fully Populated Project",
                    teamCode = OTHER_PROVIDER_TEAM.code,
                    projectTypeCode = ReferenceDataGenerator.ELEARNING_PROJECT_TYPE.code,
                    defaultPickupPointCode = SECOND_PROVIDER_OFFICE_LOCATION.code,
                    hiVisRequired = true,
                    reportToSite = true,
                    startDate = startDate,
                    expectedEndDate = expectedEndDate,
                    completionDate = completionDate,
                    beneficiaryDetails = CreateProjectBeneficiaryDetails(
                        beneficiary = "Beneficiary Full",
                        contactName = "Beneficiary Contact Full",
                        emailAddress = "beneficiary@example.com",
                        website = "https://beneficiary.example.com",
                        telephoneNumber = "02222222222",
                        buildingName = "Beneficiary Building",
                        buildingNumber = "22",
                        streetName = "Beneficiary Street",
                        townCity = "Beneficiary Town",
                        county = "Beneficiary County",
                        postcode = "BC1 2DE",
                        serviceLevelAgreement = true,
                        slaStartDate = startDate,
                        slaEndDate = startDate.plusDays(14),
                        contribution = true,
                        additionalDetails = "Beneficiary additional details",
                    ),
                    placementDetails = CreateProjectPlacementDetails(
                        locationDescription = "Location Description",
                        buildingName = "Placement Building",
                        buildingNumber = "300",
                        streetName = "Placement Street",
                        townCity = "Placement Town",
                        county = "Placement County",
                        postcode = "AB1 2EF",
                        contactName = "Placement Contact",
                        telephoneNumber = "03333333333",
                        emailAddress = "placement@example.com",
                        url = "https://placement.example.com",
                        placementNotes = "Placement notes",
                    ),
                ),
            )
        )

        mockMvc.post("/projects") {
            withToken()
            json = request
        }.andExpect {
            status { isCreated() }
        }

        val createdProjects = unpaidWorkProjectRepository.findByCodeIn(listOf(firstCode, secondCode))
        assertThat(createdProjects).hasSize(2)
        assertThat(createdProjects.map { it.code }).containsExactlyInAnyOrder(firstCode, secondCode)

        val minimalProject = createdProjects.single { it.code == firstCode }
        assertThat(minimalProject.availability).isEmpty()
        assertThat(minimalProject.name).isEqualTo("Minimal Project")
        assertThat(minimalProject.hiVisRequired).isFalse
        assertThat(minimalProject.reportToSite).isFalse
        assertThat(minimalProject.provider.code).isEqualTo(ProviderGenerator.SECOND_PROVIDER.code)
        assertThat(minimalProject.team.code).isEqualTo(OTHER_PROVIDER_TEAM.code)
        assertThat(minimalProject.team.localAdminUnit).isNotNull
        assertThat(minimalProject.team.localAdminUnit!!.code).isEqualTo(LauGenerator.SECOND_PROVIDER_LOCAL_ADMIN_UNIT.code)
        assertThat(minimalProject.team.localAdminUnit!!.probationDeliveryUnit).isNotNull
        assertThat(minimalProject.team.localAdminUnit!!.probationDeliveryUnit.code).isEqualTo(PduGenerator.SECOND_PROVIDER_PDU.code)
        assertThat(minimalProject.actualStartDate).isEqualTo(startDate)
        assertThat(minimalProject.expectedEndDate).isNull()
        assertThat(minimalProject.completionDate).isNull()
        assertThat(minimalProject.beneficiary).isNull()
        assertThat(minimalProject.beneficiaryContactName).isNull()
        assertThat(minimalProject.beneficiaryEmailAddress).isNull()
        assertThat(minimalProject.beneficiaryUrl).isNull()
        assertThat(minimalProject.beneficiaryAdditionalDetails).isNull()
        assertThat(minimalProject.beneficiaryContactAddress).isNull()
        assertThat(minimalProject.locationDescription).isNull()
        assertThat(minimalProject.placementAddress).isNull()
        assertThat(minimalProject.placementContactName).isNull()
        assertThat(minimalProject.placementEmailAddress).isNull()
        assertThat(minimalProject.placementUrl).isNull()
        assertThat(minimalProject.placementNotes).isNull()

        val fullProject = createdProjects.single { it.code == secondCode }
        assertThat(fullProject.availability).isEmpty()
        assertThat(fullProject.name).isEqualTo("Fully Populated Project")
        assertThat(fullProject.hiVisRequired).isTrue
        assertThat(fullProject.reportToSite).isTrue
        assertThat(fullProject.provider.code).isEqualTo(ProviderGenerator.SECOND_PROVIDER.code)
        assertThat(fullProject.team.code).isEqualTo(OTHER_PROVIDER_TEAM.code)
        assertThat(fullProject.team.localAdminUnit).isNotNull
        assertThat(fullProject.team.localAdminUnit!!.code).isEqualTo(LauGenerator.SECOND_PROVIDER_LOCAL_ADMIN_UNIT.code)
        assertThat(fullProject.team.localAdminUnit!!.probationDeliveryUnit).isNotNull
        assertThat(fullProject.team.localAdminUnit!!.probationDeliveryUnit.code).isEqualTo(PduGenerator.SECOND_PROVIDER_PDU.code)
        assertThat(fullProject.actualStartDate).isEqualTo(startDate)
        assertThat(fullProject.expectedEndDate).isEqualTo(expectedEndDate)
        assertThat(fullProject.completionDate).isEqualTo(completionDate)
        assertThat(fullProject.beneficiary).isEqualTo("Beneficiary Full")
        assertThat(fullProject.beneficiaryContactName).isEqualTo("Beneficiary Contact Full")
        assertThat(fullProject.beneficiaryEmailAddress).isEqualTo("beneficiary@example.com")
        assertThat(fullProject.beneficiaryUrl).isEqualTo("https://beneficiary.example.com")
        assertThat(fullProject.beneficiarySla).isTrue
        assertThat(fullProject.beneficiarySlaStartDate).isEqualTo(startDate)
        assertThat(fullProject.beneficiarySlaEndDate).isEqualTo(startDate.plusDays(14))
        assertThat(fullProject.beneficiaryContribution).isTrue
        assertThat(fullProject.beneficiaryAdditionalDetails).isEqualTo("Beneficiary additional details")
        assertThat(fullProject.beneficiaryContactAddress).isNotNull
        assertThat(fullProject.beneficiaryContactAddress!!.buildingName).isEqualTo("Beneficiary Building")
        assertThat(fullProject.beneficiaryContactAddress!!.addressNumber).isEqualTo("22")
        assertThat(fullProject.beneficiaryContactAddress!!.streetName).isEqualTo("Beneficiary Street")
        assertThat(fullProject.beneficiaryContactAddress!!.town).isEqualTo("Beneficiary Town")
        assertThat(fullProject.beneficiaryContactAddress!!.county).isEqualTo("Beneficiary County")
        assertThat(fullProject.beneficiaryContactAddress!!.postcode).isEqualTo("BC1 2DE")
        assertThat(fullProject.beneficiaryContactAddress!!.telephoneNumber).isEqualTo("02222222222")
        assertThat(fullProject.locationDescription).isEqualTo("Location Description")
        assertThat(fullProject.placementAddress).isNotNull
        assertThat(fullProject.placementAddress!!.buildingName).isEqualTo("Placement Building")
        assertThat(fullProject.placementAddress!!.addressNumber).isEqualTo("300")
        assertThat(fullProject.placementAddress!!.streetName).isEqualTo("Placement Street")
        assertThat(fullProject.placementAddress!!.town).isEqualTo("Placement Town")
        assertThat(fullProject.placementAddress!!.county).isEqualTo("Placement County")
        assertThat(fullProject.placementAddress!!.postcode).isEqualTo("AB1 2EF")
        assertThat(fullProject.placementAddress!!.telephoneNumber).isEqualTo("03333333333")
        assertThat(fullProject.placementContactName).isEqualTo("Placement Contact")
        assertThat(fullProject.placementEmailAddress).isEqualTo("placement@example.com")
        assertThat(fullProject.placementUrl).isEqualTo("https://placement.example.com")
        assertThat(fullProject.placementNotes).isEqualTo("Placement notes")
    }

    @Test
    fun `returns 400 when project code does not start with provider code`() {
        mockMvc.post("/projects") {
            withToken()
            json = CreateProjectsRequest(
                projects = listOf(
                    CreateProjectRequest(
                        providerCode = ProviderGenerator.DEFAULT_PROVIDER.code,
                        code = "N99BULK1",
                        name = "Bad Provider Prefix",
                        teamCode = TeamGenerator.DEFAULT_UPW_TEAM.code,
                        projectTypeCode = ReferenceDataGenerator.ELEARNING_PROJECT_TYPE.code,
                        defaultPickupPointCode = DEFAULT_OFFICE_LOCATION.code,
                        startDate = LocalDate.now(),
                    )
                )
            )
        }.andExpect { status { isBadRequest() } }.andReturn().response.contentAsJson<ErrorResponse>().also {
            assertThat(it.message).isEqualTo("Project code N99BULK1 must start with Provider Code ${ProviderGenerator.DEFAULT_PROVIDER.code}")
        }
    }

    @Test
    fun `returns 400 when completion date is before project start date`() {
        mockMvc.post("/projects") {
            withToken()
            json = CreateProjectsRequest(
                projects = listOf(
                    CreateProjectRequest(
                        providerCode = ProviderGenerator.SECOND_PROVIDER.code,
                        code = "N02BULK3",
                        name = "Bad Date",
                        teamCode = OTHER_PROVIDER_TEAM.code,
                        projectTypeCode = ReferenceDataGenerator.ELEARNING_PROJECT_TYPE.code,
                        defaultPickupPointCode = SECOND_PROVIDER_OFFICE_LOCATION.code,
                        startDate = LocalDate.now(),
                        completionDate = LocalDate.now().minusDays(1),
                    )
                )
            )
        }.andExpect { status { isBadRequest() } }.andReturn().response.contentAsJson<ErrorResponse>().also {
            assertThat(it.message).isEqualTo("Completion Date must be on or after Start Date for project code N02BULK3")
        }
    }

    @Test
    fun `returns 400 when pickup point belongs to a different provider`() {
        mockMvc.post("/projects") {
            withToken()
            json = CreateProjectsRequest(
                projects = listOf(
                    CreateProjectRequest(
                        providerCode = ProviderGenerator.SECOND_PROVIDER.code,
                        code = "N02BULK7",
                        name = "Mismatched Pickup Point",
                        teamCode = OTHER_PROVIDER_TEAM.code,
                        projectTypeCode = ReferenceDataGenerator.ELEARNING_PROJECT_TYPE.code,
                        defaultPickupPointCode = DEFAULT_OFFICE_LOCATION.code,
                        startDate = LocalDate.now(),
                    )
                )
            )
        }.andExpect { status { isBadRequest() } }.andReturn().response.contentAsJson<ErrorResponse>().also {
            assertThat(it.message).contains("Default Pickup Point Code ${DEFAULT_OFFICE_LOCATION.code} is not assigned to team ${OTHER_PROVIDER_TEAM.code} in provider ${ProviderGenerator.SECOND_PROVIDER.code}")
        }
    }

    @Test
    fun `returns 400 when provider code does not exist or is not selectable`() {
        mockMvc.post("/projects") {
            withToken()
            json = CreateProjectsRequest(
                projects = listOf(
                    CreateProjectRequest(
                        providerCode = "ZZZ",
                        code = "ZZZBULK8",
                        name = "Unknown Provider",
                        teamCode = OTHER_PROVIDER_TEAM.code,
                        projectTypeCode = ReferenceDataGenerator.ELEARNING_PROJECT_TYPE.code,
                        defaultPickupPointCode = SECOND_PROVIDER_OFFICE_LOCATION.code,
                        startDate = LocalDate.now(),
                    )
                )
            )
        }.andExpect { status { isBadRequest() } }.andReturn().response.contentAsJson<ErrorResponse>().also {
            assertThat(it.message).isEqualTo("Invalid Provider Code(s): ZZZ")
        }
    }

    @Test
    fun `returns 400 when pdu code is invalid for provider`() {
        mockMvc.post("/projects") {
            withToken()
            json = CreateProjectsRequest(
                projects = listOf(
                    CreateProjectRequest(
                        providerCode = ProviderGenerator.SECOND_PROVIDER.code,
                        pduCode = "BADPDU",
                        code = "N02BULK8",
                        name = "Invalid PDU",
                        teamCode = OTHER_PROVIDER_TEAM.code,
                        projectTypeCode = ReferenceDataGenerator.ELEARNING_PROJECT_TYPE.code,
                        defaultPickupPointCode = SECOND_PROVIDER_OFFICE_LOCATION.code,
                        startDate = LocalDate.now(),
                    )
                )
            )
        }.andExpect { status { isBadRequest() } }.andReturn().response.contentAsJson<ErrorResponse>().also {
            assertThat(it.message).isEqualTo("Invalid PDU Code BADPDU for provider ${ProviderGenerator.SECOND_PROVIDER.code}")
        }
    }

    @Test
    fun `returns 400 when local admin unit code is invalid for provider`() {
        mockMvc.post("/projects") {
            withToken()
            json = CreateProjectsRequest(
                projects = listOf(
                    CreateProjectRequest(
                        providerCode = ProviderGenerator.SECOND_PROVIDER.code,
                        localAdminUnitCode = "BADLAU",
                        code = "N02BULK9",
                        name = "Invalid Local Admin Unit",
                        teamCode = OTHER_PROVIDER_TEAM.code,
                        projectTypeCode = ReferenceDataGenerator.ELEARNING_PROJECT_TYPE.code,
                        defaultPickupPointCode = SECOND_PROVIDER_OFFICE_LOCATION.code,
                        startDate = LocalDate.now(),
                    )
                )
            )
        }.andExpect { status { isBadRequest() } }.andReturn().response.contentAsJson<ErrorResponse>().also {
            assertThat(it.message).isEqualTo("Invalid Local Admin Unit Code BADLAU for provider ${ProviderGenerator.SECOND_PROVIDER.code}")
        }
    }
}
