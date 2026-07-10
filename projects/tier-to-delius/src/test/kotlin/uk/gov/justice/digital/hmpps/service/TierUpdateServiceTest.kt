package uk.gov.justice.digital.hmpps.service

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
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
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataSetGenerator.TIER
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
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
import uk.gov.justice.digital.hmpps.integrations.tier.TierCalculationV2
import uk.gov.justice.digital.hmpps.integrations.tier.TierCalculationV3
import java.time.ZonedDateTime
import java.time.ZonedDateTime.now

@ExtendWith(MockitoExtension::class)
internal class TierUpdateServiceTest {
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
    lateinit var optimisationTables: OptimisationTables

    @InjectMocks
    lateinit var tierUpdateService: TierUpdateService

    private val tierScore = ReferenceDataGenerator.generate("someTierCode", TIER)
    private val changeReason = ReferenceDataGenerator.generate("ATS", ReferenceDataSetGenerator.TIER_CHANGE_REASON)
    private val tierCalculation = TierCalculationV2(tierScore.code, "someCalculationId", now())
    private val person = PersonGenerator.generate("someCrn")

    @Test
    fun `should log to telemetry and not throw exception when person not found`() {
        assertThatThrownBy { tierUpdateService.updateTier(person.crn, tierCalculation) }
            .isInstanceOf(IgnorableMessageException::class.java)
            .hasMessage("PersonNotFound")
    }

    @Test
    fun `should throw exception when PersonManager not found`() {
        person.managers.clear()
        whenever(personRepository.findByCrnAndSoftDeletedIsFalse(person.crn)).thenReturn(person)
        whenever(referenceDataRepository.getV2Tier(tierScore.code)).thenReturn(tierScore)
        whenever(referenceDataRepository.getByCodeAndSetName("ATS", ReferenceDataSetGenerator.TIER_CHANGE_REASON.name))
            .thenReturn(changeReason)

        assertThatThrownBy { tierUpdateService.updateTier(person.crn, tierCalculation) }
            .isInstanceOf(NotFoundException::class.java)
            .hasMessage("PersonManager with crn of someCrn not found")
    }

    @Test
    fun `should ignore identical updates`() {
        person.currentTier = tierScore.id
        whenever(personRepository.findByCrnAndSoftDeletedIsFalse(person.crn)).thenReturn(person)
        whenever(referenceDataRepository.getV2Tier(tierScore.code)).thenReturn(tierScore)
        whenever(referenceDataRepository.getByCodeAndSetName("ATS", ReferenceDataSetGenerator.TIER_CHANGE_REASON.name))
            .thenReturn(changeReason)

        assertThatThrownBy { tierUpdateService.updateTier(person.crn, tierCalculation) }
            .isInstanceOf(IgnorableMessageException::class.java)
            .hasMessage("UnchangedTierIgnored")
        verify(managementTierRepository, never()).save(any())
        verify(contactRepository, never()).save(any())
        verify(personRepository, never()).save(any())
    }

    @Test
    fun `should only update the person tier for most recent change`() {
        val currentTierDate = now()
        val updatedTierScore = ReferenceDataGenerator.generate("someOtherTierCode", TIER)
        whenever(personRepository.findByCrnAndSoftDeletedIsFalse(person.crn)).thenReturn(person)
        whenever(referenceDataRepository.getV2Tier("someOtherTierCode")).thenReturn(tierScore)
        whenever(referenceDataRepository.getByCodeAndSetName("ATS", ReferenceDataSetGenerator.TIER_CHANGE_REASON.name))
            .thenReturn(changeReason)
        whenever(managementTierRepository.findByIdPersonIdAndEndDateIsNull(person.id))
            .thenReturn(
                ManagementTier(
                    id = ManagementTierId(person.id, tierScore.id, currentTierDate),
                    tierChangeReasonId = changeReason.id,
                    null,
                )
            )

        assertThatThrownBy {
            tierUpdateService.updateTier(
                person.crn,
                tierCalculation.copy(
                    tierScore = updatedTierScore.code,
                    calculationDate = currentTierDate.minusDays(1)
                )
            )
        }
            .isInstanceOf(IgnorableMessageException::class.java)
            .hasMessage("OutOfOrderMessageIgnored")
        verify(managementTierRepository, never()).save(any())
        verify(contactRepository, never()).save(any())
        verify(personRepository, never()).save(any())
    }

    @Test
    fun `should save tier update to repository`() {
        val tierCalculationDate = ZonedDateTime.of(2022, 10, 11, 12, 0, 0, 0, EuropeLondon)
        val currentTierDate = tierCalculationDate.minusDays(1)
        val updatedTierScore = ReferenceDataGenerator.generate("someOtherTierCode", TIER)
        whenever(personRepository.findByCrnAndSoftDeletedIsFalse(person.crn)).thenReturn(person)
        whenever(referenceDataRepository.getV2Tier(updatedTierScore.code)).thenReturn(updatedTierScore)
        whenever(referenceDataRepository.getByCodeAndSetName("ATS", ReferenceDataSetGenerator.TIER_CHANGE_REASON.name))
            .thenReturn(changeReason)
        whenever(managementTierRepository.findByIdPersonIdAndEndDateIsNull(person.id))
            .thenReturn(
                ManagementTier(
                    id = ManagementTierId(person.id, tierScore.id, currentTierDate),
                    tierChangeReasonId = changeReason.id,
                    null,
                )
            )
        whenever(staffRepository.findByCode(StaffGenerator.DEFAULT.code)).thenReturn(StaffGenerator.DEFAULT)
        whenever(teamRepository.findByCode(TeamGenerator.DEFAULT.code)).thenReturn(TeamGenerator.DEFAULT)
        whenever(contactTypeRepository.findByCode(ContactTypeGenerator.TIER_UPDATE.code)).thenReturn(
            ContactTypeGenerator.TIER_UPDATE
        )

        tierUpdateService.updateTier(
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

    @Test
    fun `should save v3 tier update to repository`() {
        val tierCalculationDate = ZonedDateTime.of(2022, 10, 11, 12, 0, 0, 0, EuropeLondon)
        val updatedTierScore = ReferenceDataGenerator.generate("SPB", TIER)
        whenever(personRepository.findByCrnAndSoftDeletedIsFalse(person.crn)).thenReturn(person)
        whenever(referenceDataRepository.getV3Tier("B", false)).thenReturn(updatedTierScore)
        whenever(referenceDataRepository.getByCodeAndSetName("ATS", ReferenceDataSetGenerator.TIER_CHANGE_REASON.name))
            .thenReturn(changeReason)
        whenever(staffRepository.findByCode(StaffGenerator.DEFAULT.code)).thenReturn(StaffGenerator.DEFAULT)
        whenever(teamRepository.findByCode(TeamGenerator.DEFAULT.code)).thenReturn(TeamGenerator.DEFAULT)
        whenever(contactTypeRepository.findByCode(ContactTypeGenerator.TIER_UPDATE.code)).thenReturn(
            ContactTypeGenerator.TIER_UPDATE
        )

        tierUpdateService.updateTier(
            person.crn,
            TierCalculationV3(
                tierScore = "B",
                provisional = false,
                calculationId = "someCalculationId",
                calculationDate = tierCalculationDate
            )
        )

        val managementTierArgumentCaptor = ArgumentCaptor.forClass(ManagementTier::class.java)
        verify(managementTierRepository).save(managementTierArgumentCaptor.capture())
        assertThat(managementTierArgumentCaptor.value.id.tierId, equalTo(updatedTierScore.id))

        val personCaptor = ArgumentCaptor.forClass(Person::class.java)
        verify(personRepository).save(personCaptor.capture())
        assertThat(personCaptor.value.currentTier, equalTo(updatedTierScore.id))
    }

    @Test
    fun `should update v3 tier hidden column`() {
        val updatedTierScore = ReferenceDataGenerator.generate("SPBI", TIER)
        whenever(referenceDataRepository.getV3Tier("B", true)).thenReturn(updatedTierScore)

        tierUpdateService.updateV3TierColumn(
            person.crn,
            TierCalculationV3(
                tierScore = "B",
                provisional = true,
                calculationId = "someCalculationId",
                calculationDate = now()
            )
        )

        verify(personRepository).updateV3TierColumn(person.crn, updatedTierScore.id)
    }
}