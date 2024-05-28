package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.CURRENT_SENTENCE
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.UpwDetails

object UnpaidWorkGenerator {

    val UNPAID_WORK_DETAILS_1 = UpwDetails(IdGenerator.getAndIncrement(), CURRENT_SENTENCE, 0)
}