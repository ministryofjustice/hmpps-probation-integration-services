package uk.gov.justice.digital.hmpps.integrations.delius.allocations

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.INITIAL_OM_ALLOCATION
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.exception.NotActiveException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.exceptions.StaffNotInTeamException
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.TeamRepository
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationDetail
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader
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

    private val allocationDetail = ResourceLoader.file<AllocationDetail>("get-person-allocation-body")

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
    fun `unable to transfer to another provider`() {
        whenever(teamRepository.findByCode(allocationDetail.teamCode))
            .thenReturn(TeamGenerator.DEFAULT)

        val exception =
            assertThrows<IgnorableMessageException> {
                allocationValidator.initialValidations(
                    -99L,
                    allocationDetail
                )
            }
        assert(exception.message.contains("External transfer not permitted"))
    }

    @Test
    fun `team not active`() {
        val team = TeamGenerator.generate("code", endDate = ZonedDateTime.now().minusYears(5))
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
    fun `team active end dated in the future`() {
        val team = TeamGenerator.generate("code", endDate = ZonedDateTime.now().plusYears(5))
        val staff = StaffGenerator.DEFAULT
        whenever(teamRepository.findByCode(allocationDetail.teamCode))
            .thenReturn(team)
        whenever(
            referenceDataRepository.findByDatasetAndCode(
                allocationDetail.datasetCode,
                allocationDetail.code
            )
        )
            .thenReturn(INITIAL_OM_ALLOCATION)
        whenever(staffRepository.countTeamMembership(staff.id, team.id)).thenReturn(1)
        whenever(staffRepository.findByCode(allocationDetail.staffCode)).thenReturn(staff)
        assertDoesNotThrow {
            allocationValidator.initialValidations(
                ProviderGenerator.DEFAULT.id,
                allocationDetail
            )
        }
    }

    @Test
    fun `allocation reason not found`() {
        val team = TeamGenerator.DEFAULT
        whenever(teamRepository.findByCode(allocationDetail.teamCode))
            .thenReturn(team)
        whenever(
            referenceDataRepository.findByDatasetAndCode(
                allocationDetail.datasetCode,
                allocationDetail.code
            )
        ).thenReturn(null)
        val exception =
            assertThrows<NotFoundException> {
                allocationValidator.initialValidations(
                    ProviderGenerator.DEFAULT.id,
                    allocationDetail
                )
            }
        assert(exception.message!!.contains("${allocationDetail.datasetCode.value} with code of ${allocationDetail.code} not found"))
    }

    @Test
    fun `staff not found`() {
        val team = TeamGenerator.DEFAULT
        whenever(teamRepository.findByCode(allocationDetail.teamCode))
            .thenReturn(team)
        whenever(
            referenceDataRepository.findByDatasetAndCode(
                allocationDetail.datasetCode,
                allocationDetail.code
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
    fun `staff not active`() {
        val team = TeamGenerator.DEFAULT
        whenever(teamRepository.findByCode(allocationDetail.teamCode))
            .thenReturn(team)
        whenever(
            referenceDataRepository.findByDatasetAndCode(
                allocationDetail.datasetCode,
                allocationDetail.code
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
    fun `staff active end dated in the future`() {
        val team = TeamGenerator.DEFAULT
        whenever(teamRepository.findByCode(allocationDetail.teamCode))
            .thenReturn(team)
        whenever(
            referenceDataRepository.findByDatasetAndCode(
                allocationDetail.datasetCode,
                allocationDetail.code
            )
        )
            .thenReturn(INITIAL_OM_ALLOCATION)
        val futureEndDatedStaff = Staff(1L, "code", "old", "staff", "", ZonedDateTime.now().plusYears(5))
        whenever(staffRepository.countTeamMembership(futureEndDatedStaff.id, team.id)).thenReturn(1)
        whenever(staffRepository.findByCode(allocationDetail.staffCode)).thenReturn(futureEndDatedStaff)
        assertDoesNotThrow {
            allocationValidator.initialValidations(
                ProviderGenerator.DEFAULT.id,
                allocationDetail
            )
        }
    }

    @Test
    fun `staff not not in correct team`() {
        val team = TeamGenerator.DEFAULT
        val staff = StaffGenerator.DEFAULT
        whenever(teamRepository.findByCode(allocationDetail.teamCode))
            .thenReturn(team)
        whenever(
            referenceDataRepository.findByDatasetAndCode(
                allocationDetail.datasetCode,
                allocationDetail.code
            )
        )
            .thenReturn(INITIAL_OM_ALLOCATION)
        whenever(staffRepository.findByCode(allocationDetail.staffCode)).thenReturn(staff)
        whenever(staffRepository.countTeamMembership(staff.id, team.id)).thenReturn(0)
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
