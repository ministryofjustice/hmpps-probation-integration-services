package uk.gov.justice.digital.hmpps.integrations.delius.allocations

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.ResourceLoader
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.INITIAL_OM_ALLOCATION
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.exception.ConflictException
import uk.gov.justice.digital.hmpps.exception.NotActiveException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.exceptions.StaffNotInTeamException
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Team
import uk.gov.justice.digital.hmpps.integrations.delius.provider.TeamRepository
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
class AllocationValidatorTest {
    @Mock
    private lateinit var staffRepository: StaffRepository

    @Mock
    private lateinit var teamRepository: TeamRepository

    @Mock
    private lateinit var referenceDataRepository: ReferenceDataRepository

    @InjectMocks
    private lateinit var allocationValidator: AllocationValidator

    private val allocationDetail = ResourceLoader.allocationBody("get-person-allocation-body")

    @Test
    fun `team not found`() {
        whenever(teamRepository.findByCode(allocationDetail.teamCode))
            .thenReturn(null)
        val exception =
            assertThrows<NotFoundException> {
                allocationValidator.initialValidations(
                    ProviderGenerator.DEFAULT.id,
                    allocationDetail
                )
            }
        assert(exception.message!!.contains("Team with code of ${allocationDetail.teamCode} not found"))
    }

    @Test
    fun `Unable to transfer to another provider`() {
        whenever(teamRepository.findByCode(allocationDetail.teamCode))
            .thenReturn(TeamGenerator.DEFAULT)

        val exception =
            assertThrows<ConflictException> {
                allocationValidator.initialValidations(
                    -99L,
                    allocationDetail
                )
            }
        assert(exception.message!!.contains("Cannot transfer from provider -99 to ${TeamGenerator.DEFAULT.providerId}"))
    }

    @Test
    fun `team not active`() {
        val team = Team(1L, "code", 1L, "team", ZonedDateTime.now().minusYears(5))
        whenever(teamRepository.findByCode(allocationDetail.teamCode))
            .thenReturn(team)
        val exception =
            assertThrows<NotActiveException> {
                allocationValidator.initialValidations(
                    ProviderGenerator.DEFAULT.id,
                    allocationDetail
                )
            }
        assert(exception.message!!.contains("Team with code of ${team.code} not active"))
    }

    @Test
    fun `Team active end dated in the future`() {
        val team = Team(1L, "code", 1L, "team", ZonedDateTime.now().plusYears(5))
        val staff = StaffGenerator.DEFAULT
        whenever(teamRepository.findByCode(allocationDetail.teamCode))
            .thenReturn(team)
        whenever(
            referenceDataRepository.findByDatasetAndCode(
                allocationDetail.datasetCode, allocationDetail.code
            )
        )
            .thenReturn(INITIAL_OM_ALLOCATION)
        whenever(staffRepository.verifyTeamMembership(staff.id, team.id)).thenReturn(true)
        whenever(staffRepository.findByCode(allocationDetail.staffCode)).thenReturn(staff)
        assertDoesNotThrow {
            allocationValidator.initialValidations(
                ProviderGenerator.DEFAULT.id,
                allocationDetail
            )
        }
    }

    @Test
    fun `Allocation reason not found`() {
        val team = TeamGenerator.DEFAULT
        whenever(teamRepository.findByCode(allocationDetail.teamCode))
            .thenReturn(team)
        whenever(
            referenceDataRepository.findByDatasetAndCode(
                allocationDetail.datasetCode, allocationDetail.code
            )
        ).thenReturn(null)
        val exception =
            assertThrows<NotFoundException> {
                allocationValidator.initialValidations(
                    ProviderGenerator.DEFAULT.id,
                    allocationDetail
                )
            }
        assert(exception.message!!.contains("$allocationDetail.datasetCode.value with code ${allocationDetail.code} not found"))
    }

    @Test
    fun `Staff not found`() {
        val team = TeamGenerator.DEFAULT
        whenever(teamRepository.findByCode(allocationDetail.teamCode))
            .thenReturn(team)
        whenever(
            referenceDataRepository.findByDatasetAndCode(
                allocationDetail.datasetCode, allocationDetail.code
            )
        )
            .thenReturn(INITIAL_OM_ALLOCATION)
        whenever(staffRepository.findByCode(allocationDetail.staffCode)).thenReturn(null)
        val exception =
            assertThrows<NotFoundException> {
                allocationValidator.initialValidations(
                    ProviderGenerator.DEFAULT.id,
                    allocationDetail
                )
            }
        assert(exception.message!!.contains("Staff with code of ${allocationDetail.staffCode} not found"))
    }

    @Test
    fun `Staff not active`() {
        val team = TeamGenerator.DEFAULT
        whenever(teamRepository.findByCode(allocationDetail.teamCode))
            .thenReturn(team)
        whenever(
            referenceDataRepository.findByDatasetAndCode(
                allocationDetail.datasetCode, allocationDetail.code
            )
        )
            .thenReturn(INITIAL_OM_ALLOCATION)
        val oldStaff = Staff(1L, "code", "old", "staff", "", ZonedDateTime.now().minusYears(5))
        whenever(staffRepository.findByCode(allocationDetail.staffCode)).thenReturn(oldStaff)
        val exception =
            assertThrows<NotActiveException> {
                allocationValidator.initialValidations(
                    ProviderGenerator.DEFAULT.id,
                    allocationDetail
                )
            }
        assert(exception.message!!.contains("Staff with code of ${oldStaff.code} not active"))
    }

    @Test
    fun `Staff active end dated in the future`() {
        val team = TeamGenerator.DEFAULT
        whenever(teamRepository.findByCode(allocationDetail.teamCode))
            .thenReturn(team)
        whenever(
            referenceDataRepository.findByDatasetAndCode(
                allocationDetail.datasetCode, allocationDetail.code
            )
        )
            .thenReturn(INITIAL_OM_ALLOCATION)
        val futureEndDatedStaff = Staff(1L, "code", "old", "staff", "", ZonedDateTime.now().plusYears(5))
        whenever(staffRepository.verifyTeamMembership(futureEndDatedStaff.id, team.id)).thenReturn(true)
        whenever(staffRepository.findByCode(allocationDetail.staffCode)).thenReturn(futureEndDatedStaff)
        assertDoesNotThrow {
            allocationValidator.initialValidations(
                ProviderGenerator.DEFAULT.id,
                allocationDetail
            )
        }
    }

    @Test
    fun `Staff not not in correct team`() {
        val team = TeamGenerator.DEFAULT
        val staff = StaffGenerator.DEFAULT
        whenever(teamRepository.findByCode(allocationDetail.teamCode))
            .thenReturn(team)
        whenever(
            referenceDataRepository.findByDatasetAndCode(
                allocationDetail.datasetCode, allocationDetail.code
            )
        )
            .thenReturn(INITIAL_OM_ALLOCATION)
        whenever(staffRepository.findByCode(allocationDetail.staffCode)).thenReturn(staff)
        whenever(staffRepository.verifyTeamMembership(staff.id, team.id)).thenReturn(false)
        val exception =
            assertThrows<StaffNotInTeamException> {
                allocationValidator.initialValidations(
                    ProviderGenerator.DEFAULT.id,
                    allocationDetail
                )
            }
        assert(exception.message!!.contains("Staff ${staff.code} not in Team ${team.code}"))
    }
}
