package uk.gov.justice.digital.hmpps.integrations.tier

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
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
import uk.gov.justice.digital.hmpps.integrations.delius.management.ManagementTierRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.staff.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.team.TeamRepository
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.test.CustomMatchers.isCloseTo
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
internal class TierServiceTest {
    @Mock lateinit var tierClient: TierClient
    @Mock lateinit var personRepository: PersonRepository
    @Mock lateinit var referenceDataRepository: ReferenceDataRepository
    @Mock lateinit var managementTierRepository: ManagementTierRepository
    @Mock lateinit var contactRepository: ContactRepository
    @Mock lateinit var staffRepository: StaffRepository
    @Mock lateinit var teamRepository: TeamRepository
    @Mock lateinit var contactTypeRepository: ContactTypeRepository
    @Mock lateinit var telemetryService: TelemetryService
    @InjectMocks lateinit var tierService: TierService

    private val tierScore = ReferenceDataGenerator.generate("someTierCode", ReferenceDataSetGenerator.TIER)
    private val changeReason = ReferenceDataGenerator.generate("ATS", ReferenceDataSetGenerator.TIER_CHANGE_REASON)
    private val person = PersonGenerator.generate("someCrn")

    @Test
    fun `should throw exception when reference data not found`() {
        whenever(tierClient.getTierCalculation(any(), any())).thenReturn(
            TierCalculation(tierScore.code, "someCalculationId", ZonedDateTime.now())
        )

        val exception = assertThrows<NotFoundException> {
            tierService.handleTierCalculation(person.crn, "someCalculationId")
        }

        assertEquals("TIER with code of UsomeTierCode not found", exception.message)
    }

    @Test
    fun `should throw exception when person not found`() {
        whenever(tierClient.getTierCalculation(any(), any())).thenReturn(
            TierCalculation(tierScore.code, "someCalculationId", ZonedDateTime.now())
        )

        whenever(referenceDataRepository.findByCodeAndSetName("U${tierScore.code}", ReferenceDataSetGenerator.TIER.name))
            .thenReturn(tierScore)

        val exception = assertThrows<NotFoundException> {
            tierService.handleTierCalculation(person.crn, "someCalculationId")
        }

        assertEquals("Person with crn of someCrn not found", exception.message)
    }

    @Test
    fun `should throw exception when change reason not found`() {
        whenever(tierClient.getTierCalculation(any(), any())).thenReturn(
            TierCalculation(tierScore.code, "someCalculationId", ZonedDateTime.now())
        )

        whenever(referenceDataRepository.findByCodeAndSetName("U${tierScore.code}", ReferenceDataSetGenerator.TIER.name))
            .thenReturn(tierScore)

        whenever(personRepository.findByCrnAndSoftDeletedIsFalse(person.crn)).thenReturn(person)

        val exception = assertThrows<NotFoundException> {
            tierService.handleTierCalculation(person.crn, "someCalculationId")
        }

        assertEquals("TIER CHANGE REASON with code of ATS not found", exception.message)
    }

    @Test
    fun `should throw exception when PersonManager not found`() {
        val tierCalculationDate = ZonedDateTime.now()

        whenever(tierClient.getTierCalculation(any(), any())).thenReturn(
            TierCalculation(tierScore.code, "someCalculationId", tierCalculationDate)
        )

        whenever(referenceDataRepository.findByCodeAndSetName("U${tierScore.code}", ReferenceDataSetGenerator.TIER.name))
            .thenReturn(tierScore)

        person.managers.clear()
        whenever(personRepository.findByCrnAndSoftDeletedIsFalse(person.crn)).thenReturn(person)

        whenever(referenceDataRepository.findByCodeAndSetName("ATS", ReferenceDataSetGenerator.TIER_CHANGE_REASON.name))
            .thenReturn(changeReason)

        val exception = assertThrows<NotFoundException> { tierService.handleTierCalculation(person.crn, "someCalculationId") }

        assertEquals("PersonManager with crn of someCrn not found", exception.message)
    }

    @Test
    fun `should save tier update to repository`() {
        val tierCalculationDate = ZonedDateTime.of(2022, 10, 11, 12, 0, 0, 0, EuropeLondon)

        whenever(tierClient.getTierCalculation(any(), any())).thenReturn(
            TierCalculation(tierScore.code, "someCalculationId", tierCalculationDate)
        )

        whenever(referenceDataRepository.findByCodeAndSetName("U${tierScore.code}", ReferenceDataSetGenerator.TIER.name))
            .thenReturn(tierScore)

        whenever(personRepository.findByCrnAndSoftDeletedIsFalse(person.crn)).thenReturn(person)

        whenever(referenceDataRepository.findByCodeAndSetName("ATS", ReferenceDataSetGenerator.TIER_CHANGE_REASON.name))
            .thenReturn(changeReason)

        whenever(staffRepository.findByCode(StaffGenerator.DEFAULT.code)).thenReturn(StaffGenerator.DEFAULT)

        whenever(teamRepository.findByCode(TeamGenerator.DEFAULT.code)).thenReturn(TeamGenerator.DEFAULT)

        whenever(contactTypeRepository.findByCode(ContactTypeGenerator.TIER_UPDATE.code)).thenReturn(ContactTypeGenerator.TIER_UPDATE)

        tierService.handleTierCalculation(person.crn, "someCalculationId")

        val managementTierArgumentCaptor = ArgumentCaptor.forClass(ManagementTier::class.java)
        verify(managementTierRepository).save(managementTierArgumentCaptor.capture())
        val tier = managementTierArgumentCaptor.value

        assertThat(tier.id.personId, equalTo(person.id))
        assertThat(tier.id.tierId, equalTo(tierScore.id))
        assertThat(tier.id.dateChanged, equalTo(tierCalculationDate))
        assertThat(tier.createdDatetime, isCloseTo(ZonedDateTime.now()))
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
            Tier: description of someTierCode
            Tier Change Reason: description of ATS
                """.trimIndent()
            )
        )
        assertThat(contact.type.id, equalTo(ContactTypeGenerator.TIER_UPDATE.id))
        assertThat(contact.type.code, equalTo(ContactTypeGenerator.TIER_UPDATE.code))

        val personCaptor = ArgumentCaptor.forClass(Person::class.java)
        verify(personRepository).save(personCaptor.capture())
        val person = personCaptor.value
        assertThat(person.currentTier, equalTo(tierScore.id))

        verify(telemetryService).trackEvent(
            "TierUpdateSuccess",
            mapOf(
                "crn" to person.crn,
                "tier" to "someTierCode",
                "calculationDate" to tierCalculationDate.toString()
            )
        )
    }
}
