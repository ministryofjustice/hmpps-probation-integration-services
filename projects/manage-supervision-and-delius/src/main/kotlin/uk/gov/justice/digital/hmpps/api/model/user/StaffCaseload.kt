package uk.gov.justice.digital.hmpps.api.model.user

import uk.gov.justice.digital.hmpps.api.model.Name

data class StaffCaseload(
    val totalPages: Int,
    val totalElements: Int,
    val sortedBy: String? = null,
    val provider: String?,
    val staff: Name,
    val caseload: List<StaffCase>,
    val metaData: MetaData? = null
)

data class MetaData(
    val sentenceTypes: List<KeyPair>,
    val contactTypes: List<KeyPair>
)

data class KeyPair(
    val code: String,
    val description: String
)
