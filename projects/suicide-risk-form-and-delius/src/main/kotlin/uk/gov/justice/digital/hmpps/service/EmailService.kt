package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.Dataset
import uk.gov.justice.digital.hmpps.integrations.delius.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.model.CodedDescription
import uk.gov.justice.digital.hmpps.model.EmailResponse

@Service
class EmailService(private val referenceDataRepository: ReferenceDataRepository) {
    fun getAuthorisedEmails(): EmailResponse = referenceDataRepository
        .findAllByDataset_CodeOrderByCode(Dataset.AUTHORISED_SRF_EMAILS)
        .filter { it.selectable }
        .map { CodedDescription(it.code, it.description) }
        .let { EmailResponse(it) }
}