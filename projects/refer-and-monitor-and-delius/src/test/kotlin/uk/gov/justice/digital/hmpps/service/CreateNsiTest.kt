package uk.gov.justice.digital.hmpps.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.dao.IncorrectResultSizeDataAccessException
import uk.gov.justice.digital.hmpps.api.model.ReferralStarted
import uk.gov.justice.digital.hmpps.data.generator.NsiGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator
import uk.gov.justice.digital.hmpps.exception.AlreadyCreatedException
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.ProviderRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiStatusRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.RequirementRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.DisposalRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.Nsi
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.NsiStatus
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.RequirementMainCategory
import java.time.ZonedDateTime
import java.util.UUID

@ExtendWith(MockitoExtension::class)
internal class CreateNsiTest {
    @Mock
    lateinit var personRepository: PersonRepository

    @Mock
    lateinit var disposalRepository: DisposalRepository

    @Mock
    lateinit var requirementRepository: RequirementRepository

    @Mock
    lateinit var providerRepository: ProviderRepository

    @Mock
    lateinit var nsiTypeRepository: NsiTypeRepository

    @Mock
    lateinit var nsiStatusRepository: NsiStatusRepository

    @Mock
    lateinit var nsiRepository: NsiRepository

    @Mock
    lateinit var nsiManagerService: NsiManagerService

    @InjectMocks
    lateinit var createNsi: CreateNsi

    @Test
    fun `contract type not recognised`() {
        val person = PersonGenerator.SETENCED_WITHOUT_NSI
        val sentence = SentenceGenerator.SENTENCE_WITHOUT_NSI
        val referralId = UUID.randomUUID()
        whenever(personRepository.findByCrn(person.crn)).thenReturn(person)
        whenever(disposalRepository.findByPersonIdAndEventId(person.id, sentence.eventId)).thenReturn(sentence)
        whenever(
            requirementRepository.findForPersonAndEvent(
                person.id,
                sentence.id,
                RequirementMainCategory.Code.REHAB_ACTIVITY_TYPE.value
            )
        ).thenReturn(listOf())

        assertThrows<IllegalArgumentException> {
            createNsi.new(
                person.crn,
                ReferralStarted(referralId, ZonedDateTime.now(), "unknown", sentence.eventId, "Notes")
            ) {}
        }
    }

    @Test
    fun `Already Created Exception when attempting to create a duplicate`() {
        val person = PersonGenerator.SETENCED_WITHOUT_NSI
        val sentence = SentenceGenerator.SENTENCE_WITHOUT_NSI
        val referralId = UUID.randomUUID()
        val contractType = ContractTypeNsiType.MAPPING.keys.first()
        val nsiType = ContractTypeNsiType.MAPPING[contractType]!!
        val rs = ReferralStarted(
            referralId,
            ZonedDateTime.now(),
            contractType,
            sentence.eventId,
            "Notes"
        )

        whenever(personRepository.findByCrn(person.crn)).thenReturn(person)
        whenever(disposalRepository.findByPersonIdAndEventId(person.id, sentence.eventId)).thenReturn(sentence)
        whenever(
            requirementRepository.findForPersonAndEvent(
                person.id,
                sentence.id,
                RequirementMainCategory.Code.REHAB_ACTIVITY_TYPE.value
            )
        ).thenReturn(listOf())
        whenever(nsiTypeRepository.findByCode(nsiType)).thenReturn(NsiGenerator.TYPES[nsiType])
        whenever(nsiStatusRepository.findByCode(NsiStatus.Code.IN_PROGRESS.value)).thenReturn(NsiGenerator.INPROG_STATUS)
        whenever(nsiRepository.findByPersonCrnAndExternalReference(person.crn, rs.urn))
            .thenThrow(IncorrectResultSizeDataAccessException(1, 2))
        whenever(providerRepository.findByCode(ProviderGenerator.INTENDED_PROVIDER.code)).thenReturn(ProviderGenerator.INTENDED_PROVIDER)
        whenever(nsiRepository.save(any<Nsi>())).thenAnswer { it.arguments[0] }
        whenever(nsiManagerService.createNewManager(any())).thenAnswer { NsiGenerator.generateManager(it.arguments[0] as Nsi) }

        assertThrows<AlreadyCreatedException> { createNsi.new(person.crn, rs) {} }
    }
}
