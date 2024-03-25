package uk.gov.justice.digital.hmpps.integrations.tier

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.audit.service.OptimisationTables
import uk.gov.justice.digital.hmpps.data.generator.ContactTypeGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataSetGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.contact.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.management.ManagementTier
import uk.gov.justice.digital.hmpps.integrations.delius.management.ManagementTierId
import uk.gov.justice.digital.hmpps.integrations.delius.management.ManagementTierRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.staff.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.team.TeamRepository
import uk.gov.justice.digital.hmpps.messaging.telemetryProperties
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime
import java.time.ZonedDateTime.now

@ExtendWith(MockitoExtension::class)
internal class TierServiceTest {
    @Mock
    lateinit var personRepository: PersonRepository

    @Mock
    lateinit var referenceDataRepository: ReferenceDataRepository

    @Mock
    lateinit var managementTierRepository: ManagementTierRepository

    @Mock
    lateinit var contactRepository: ContactRepository

    @Mock
    lateinit var staffRepository: StaffRepository

    @Mock
    lateinit var teamRepository: TeamRepository

    @Mock
    lateinit var contactTypeRepository: ContactTypeRepository

    @Mock
    lateinit var telemetryService: TelemetryService

    @Mock
    lateinit var optimisationTables: OptimisationTables

    @InjectMocks
    lateinit var tierService: TierService

    private val tierScore = ReferenceDataGenerator.generate("someTierCode", ReferenceDataSetGenerator.TIER)
    private val changeReason = ReferenceDataGenerator.generate("ATS", ReferenceDataSetGenerator.TIER_CHANGE_REASON)
    private val tierCalculation = TierCalculation(tierScore.code, "someCalculationId", now())
    private val person = PersonGenerator.generate("someCrn")

    @Test
    fun `should log to telemetry and not throw exception when person not found`() {
        assertDoesNotThrow {
            tierService.updateTier(person.crn, tierCalculation)
        }

        verify(telemetryService).trackEvent("PersonNotFound", tierCalculation.telemetryProperties(person.crn))
    }

    @Test
    fun `should throw exception when reference data not found`() {
        whenever(personRepository.findByCrnAndSoftDeletedIsFalse(person.crn)).thenReturn(person)

        val exception = assertThrows<NotFoundException> {
            tierService.updateTier(person.crn, tierCalculation)
        }

        assertEquals("TIER with code of UsomeTierCode not found", exception.message)
    }

    @Test
    fun `should throw exception when change reason not found`() {
        whenever(personRepository.findByCrnAndSoftDeletedIsFalse(person.crn)).thenReturn(person)
        whenever(
            referenceDataRepository.findByCodeAndSetName(
                "U${tierScore.code}",
                ReferenceDataSetGenerator.TIER.name
            )
        )
            .thenReturn(tierScore)

        val exception = assertThrows<NotFoundException> {
            tierService.updateTier(person.crn, tierCalculation)
        }

        assertEquals("TIER CHANGE REASON with code of ATS not found", exception.message)
    }

    @Test
    fun `should throw exception when PersonManager not found`() {
        person.managers.clear()
        whenever(personRepository.findByCrnAndSoftDeletedIsFalse(person.crn)).thenReturn(person)
        whenever(
            referenceDataRepository.findByCodeAndSetName(
                "U${tierScore.code}",
                ReferenceDataSetGenerator.TIER.name
            )
        )
            .thenReturn(tierScore)
        whenever(referenceDataRepository.findByCodeAndSetName("ATS", ReferenceDataSetGenerator.TIER_CHANGE_REASON.name))
            .thenReturn(changeReason)

        val exception = assertThrows<NotFoundException> { tierService.updateTier(person.crn, tierCalculation) }

        assertEquals("PersonManager with crn of someCrn not found", exception.message)
    }

    @Test
    fun `should ignore identical updates`() {
        person.currentTier = tierScore.id
        whenever(personRepository.findByCrnAndSoftDeletedIsFalse(person.crn)).thenReturn(person)
        whenever(
            referenceDataRepository.findByCodeAndSetName(
                "U${tierScore.code}",
                ReferenceDataSetGenerator.TIER.name
            )
        )
            .thenReturn(tierScore)
        whenever(referenceDataRepository.findByCodeAndSetName("ATS", ReferenceDataSetGenerator.TIER_CHANGE_REASON.name))
            .thenReturn(changeReason)

        tierService.updateTier(person.crn, tierCalculation)
        verify(managementTierRepository, never()).save(any())
        verify(contactRepository, never()).save(any())
        verify(personRepository, never()).save(any())
        verify(telemetryService).trackEvent("UnchangedTierIgnored", tierCalculation.telemetryProperties(person.crn))
    }

    @Test
    fun `should only update the person tier for most recent change`() {
        val currentTierDate = now()
        val updatedTierScore = ReferenceDataGenerator.generate("someOtherTierCode", ReferenceDataSetGenerator.TIER)
        whenever(personRepository.findByCrnAndSoftDeletedIsFalse(person.crn)).thenReturn(person)
        whenever(
            referenceDataRepository.findByCodeAndSetName(
                "U${updatedTierScore.code}",
                ReferenceDataSetGenerator.TIER.name
            )
        )
            .thenReturn(updatedTierScore)
        whenever(referenceDataRepository.findByCodeAndSetName("ATS", ReferenceDataSetGenerator.TIER_CHANGE_REASON.name))
            .thenReturn(changeReason)
        whenever(managementTierRepository.findFirstByIdPersonIdOrderByIdDateChangedDesc(person.id))
            .thenReturn(
                ManagementTier(
                    id = ManagementTierId(person.id, tierScore.id, currentTierDate),
                    tierChangeReasonId = changeReason.id
                )
            )
        whenever(staffRepository.findByCode(StaffGenerator.DEFAULT.code)).thenReturn(StaffGenerator.DEFAULT)
        whenever(teamRepository.findByCode(TeamGenerator.DEFAULT.code)).thenReturn(TeamGenerator.DEFAULT)
        whenever(contactTypeRepository.findByCode(ContactTypeGenerator.TIER_UPDATE.code)).thenReturn(
            ContactTypeGenerator.TIER_UPDATE
        )

        tierService.updateTier(
            person.crn,
            tierCalculation.copy(
                tierScore = updatedTierScore.code,
                calculationDate = currentTierDate.minusDays(1)
            )
        )
        verify(managementTierRepository).save(any())
        verify(contactRepository).save(any())
        verify(personRepository, never()).save(any())
    }

    @Test
    fun `should save tier update to repository`() {
        val tierCalculationDate = ZonedDateTime.of(2022, 10, 11, 12, 0, 0, 0, EuropeLondon)
        val currentTierDate = tierCalculationDate.minusDays(1)
        val updatedTierScore = ReferenceDataGenerator.generate("someOtherTierCode", ReferenceDataSetGenerator.TIER)
        whenever(personRepository.findByCrnAndSoftDeletedIsFalse(person.crn)).thenReturn(person)
        whenever(
            referenceDataRepository.findByCodeAndSetName(
                "U${updatedTierScore.code}",
                ReferenceDataSetGenerator.TIER.name
            )
        )
            .thenReturn(updatedTierScore)
        whenever(referenceDataRepository.findByCodeAndSetName("ATS", ReferenceDataSetGenerator.TIER_CHANGE_REASON.name))
            .thenReturn(changeReason)
        whenever(managementTierRepository.findFirstByIdPersonIdOrderByIdDateChangedDesc(person.id))
            .thenReturn(
                ManagementTier(
                    id = ManagementTierId(person.id, tierScore.id, currentTierDate),
                    tierChangeReasonId = changeReason.id
                )
            )
        whenever(staffRepository.findByCode(StaffGenerator.DEFAULT.code)).thenReturn(StaffGenerator.DEFAULT)
        whenever(teamRepository.findByCode(TeamGenerator.DEFAULT.code)).thenReturn(TeamGenerator.DEFAULT)
        whenever(contactTypeRepository.findByCode(ContactTypeGenerator.TIER_UPDATE.code)).thenReturn(
            ContactTypeGenerator.TIER_UPDATE
        )

        tierService.updateTier(
            person.crn,
            tierCalculation.copy(
                calculationDate = tierCalculationDate,
                tierScore = updatedTierScore.code
            )
        )

        val managementTierArgumentCaptor = ArgumentCaptor.forClass(ManagementTier::class.java)
        verify(managementTierRepository).save(managementTierArgumentCaptor.capture())
        val tier = managementTierArgumentCaptor.value

        assertThat(tier.id.personId, equalTo(person.id))
        assertThat(tier.id.tierId, equalTo(updatedTierScore.id))
        assertThat(tier.id.dateChanged, equalTo(tierCalculationDate))
        assertThat(tier.tierChangeReasonId, equalTo(changeReason.id))

        val contactArgumentCaptor = ArgumentCaptor.forClass(Contact::class.java)
        verify(contactRepository).save(contactArgumentCaptor.capture())
        val contact = contactArgumentCaptor.value

        assertThat(contact.date, equalTo(tierCalculationDate))
        assertThat(contact.person, equalTo(person))
        assertThat(contact.startTime, equalTo(tierCalculationDate))
        assertThat(
            contact.notes,
            equalTo(
                """
            Tier Change Date: 11/10/2022 12:00:00
            Tier: description of someOtherTierCode
            Tier Change Reason: description of ATS
                """.trimIndent()
            )
        )
        assertThat(contact.type.id, equalTo(ContactTypeGenerator.TIER_UPDATE.id))
        assertThat(contact.type.code, equalTo(ContactTypeGenerator.TIER_UPDATE.code))

        val personCaptor = ArgumentCaptor.forClass(Person::class.java)
        verify(personRepository).save(personCaptor.capture())
        val person = personCaptor.value
        assertThat(person.currentTier, equalTo(updatedTierScore.id))
    }
}
