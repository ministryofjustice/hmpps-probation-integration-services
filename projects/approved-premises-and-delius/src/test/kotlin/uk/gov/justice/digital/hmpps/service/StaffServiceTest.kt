package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.ldap.core.LdapTemplate
import uk.gov.justice.digital.hmpps.data.generator.ApprovedPremisesGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.ApprovedPremisesRepository
import uk.gov.justice.digital.hmpps.integrations.delius.staff.StaffRepository

@ExtendWith(MockitoExtension::class)
internal class StaffServiceTest {
    @Mock
    lateinit var approvedPremisesRepository: ApprovedPremisesRepository

    @Mock
    lateinit var staffRepository: StaffRepository

    @Mock
    lateinit var ldapTemplate: LdapTemplate

    @InjectMocks
    lateinit var staffService: StaffService

    @Test
    fun `throws not found when approved premises does not exist`() {
        whenever(approvedPremisesRepository.existsByCodeCode("TEST")).thenReturn(false)

        val exception = assertThrows<NotFoundException> {
            staffService.getStaffInApprovedPremises("TEST", false, Pageable.unpaged())
        }
        assertThat(exception.message, equalTo("Approved Premises with code of TEST not found"))
    }

    @Test
    fun `maps and returns results`() {
        val approvedPremises = ApprovedPremisesGenerator.DEFAULT
        val staffEntities = listOf(
            StaffGenerator.generate(
                "Staff 1",
                teams = listOf(TeamGenerator.APPROVED_PREMISES_TEAM),
                approvedPremises = emptyList()
            ),
            StaffGenerator.generate(
                "Staff 2",
                teams = listOf(TeamGenerator.APPROVED_PREMISES_TEAM),
                approvedPremises = listOf(approvedPremises)
            )
        )
        whenever(approvedPremisesRepository.existsByCodeCode(approvedPremises.code.code)).thenReturn(true)
        whenever(
            staffRepository.findAllStaffLinkedToApprovedPremisesTeam(
                approvedPremises.code.code,
                Pageable.unpaged()
            )
        )
            .thenReturn(PageImpl(staffEntities))

        val results = staffService.getStaffInApprovedPremises(approvedPremises.code.code, false, Pageable.unpaged())

        assertThat(results.content, hasSize(2))
        assertThat(results.content.map { it.name.surname }, equalTo(listOf("Staff 1", "Staff 2")))
        assertThat(results.content.map { it.keyWorker }, equalTo(listOf(false, true)))
        assertThat(results.content[0].code, equalTo(staffEntities[0].code))
        assertThat(results.content[0].name.forename, equalTo(staffEntities[0].forename))
        assertThat(results.content[0].name.middleName, equalTo(staffEntities[0].middleName))
        assertThat(results.content[0].name.surname, equalTo(staffEntities[0].surname))
        assertThat(results.content[0].grade!!.code, equalTo(staffEntities[0].grade!!.code))
        assertThat(results.content[0].grade!!.description, equalTo(staffEntities[0].grade!!.description))
    }
}
