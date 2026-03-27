package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.entity.ReferenceDataSet
import uk.gov.justice.digital.hmpps.model.CodeAndDescription
import uk.gov.justice.digital.hmpps.model.EmailResponse

@Service
class EmailService(private val referenceDataRepository: ReferenceDataRepository) {
    fun getAuthorisedEmails(): EmailResponse = referenceDataRepository
            .findAllByDataSetName(ReferenceDataSet.Code.AUTHORISED_EMAILS.value)
            .map { CodeAndDescription(it.code, it.description) }
            .let { EmailResponse(it) }
}