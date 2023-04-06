package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.api.model.ReferralStarted
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiRepository
import uk.gov.justice.digital.hmpps.service.ContractTypeNsiType
import uk.gov.justice.digital.hmpps.service.NsiService
import java.time.LocalDate
import java.util.UUID
import java.util.concurrent.CompletableFuture

@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NsiServiceTest {

    @Autowired
    lateinit var nsiService: NsiService

    @Autowired
    lateinit var nsiRepository: NsiRepository

    @Test
    fun `does not save duplicate nsis`() {
        val referralId = UUID.randomUUID()
        val startedAt = LocalDate.now().atStartOfDay().atZone(EuropeLondon)
        val contractType = ContractTypeNsiType.MAPPING.entries.first().key
        val sentenceId = SentenceGenerator.SENTENCE_WITHOUT_NSI.event.id
        val referralStarted = ReferralStarted(referralId, startedAt, contractType, sentenceId, "Some notes")

        val job1 = CompletableFuture.supplyAsync {
            nsiService.startNsi(PersonGenerator.SENTENCED_WITHOUT_NSI.crn, referralStarted)
        }
        val job2 = CompletableFuture.supplyAsync {
            nsiService.startNsi(PersonGenerator.SENTENCED_WITHOUT_NSI.crn, referralStarted)
        }

        CompletableFuture.allOf(job1, job2).join()
        job1.get()
        job2.get()

        val nsi = nsiRepository.findByPersonCrnAndExternalReference(
            PersonGenerator.SENTENCED_WITHOUT_NSI.crn,
            referralStarted.urn
        )
        assertThat(nsi?.externalReference, equalTo(referralStarted.urn))
        assertThat(nsi?.createdDatetime, equalTo(nsi?.lastUpdatedDatetime))
    }
}
