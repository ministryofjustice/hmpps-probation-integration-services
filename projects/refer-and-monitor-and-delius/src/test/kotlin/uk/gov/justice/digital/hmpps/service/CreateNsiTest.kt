package uk.gov.justice.digital.hmpps.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.api.model.ReferralStarted
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.DisposalRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.ProviderRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiStatusRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.RequirementRepository
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
        val person = PersonGenerator.SENTENCED_WITHOUT_NSI
        val sentence = SentenceGenerator.SENTENCE_WITHOUT_NSI
        val referralId = UUID.randomUUID()
        whenever(personRepository.findByCrn(person.crn)).thenReturn(person)
        whenever(disposalRepository.findByEventPersonIdAndEventId(person.id, sentence.event.id)).thenReturn(sentence)
        whenever(
            requirementRepository.findForPersonAndEvent(
                person.id,
                sentence.id,
                RequirementMainCategory.Code.REHAB_ACTIVITY_TYPE.value,
            ),
        ).thenReturn(listOf())

        assertThrows<IllegalArgumentException> {
            createNsi.new(
                person.crn,
                ReferralStarted(referralId, ZonedDateTime.now(), "unknown", sentence.event.id, "Notes"),
            ) {}
        }
    }
}
