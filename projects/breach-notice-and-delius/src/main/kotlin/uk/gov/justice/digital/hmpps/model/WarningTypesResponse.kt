package uk.gov.justice.digital.hmpps.model

data class WarningTypesResponse(
    val warningTypes: List<CodedDescription>,
    val sentenceTypes: List<SentenceType>,
    val defaultSentenceTypeCode: String,
)