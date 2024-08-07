package uk.gov.justice.digital.hmpps.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.api.model.KeyValue
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.service.toSentenceStatus

class ConvictionServiceTest {

    @Test
    fun `toSentenceStatus returns null when no values are present `() {

        val sentenceStatus = SentenceGenerator.CURRENT_SENTENCE.toSentenceStatus()
        assertEquals(null, sentenceStatus.actualReleaseDate)
        assertEquals(KeyValue("NOT_IN_CUSTODY", "Not in custody"), sentenceStatus.custodialType)
        assertEquals(null, sentenceStatus.mainOffence)
        assertEquals(null, sentenceStatus.licenceExpiryDate)
    }
}

