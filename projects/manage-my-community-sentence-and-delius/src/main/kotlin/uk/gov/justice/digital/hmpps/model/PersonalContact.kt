package uk.gov.justice.digital.hmpps.model

import uk.gov.justice.digital.hmpps.entity.PersonalContact as PersonalContactEntity

data class PersonalContact(
    val name: Name,
    val relationship: String,
    val mobileNumber: String?,
    val emailAddress: String?,
) {
    companion object {
        fun PersonalContactEntity.toModel() = PersonalContact(
            name = name(),
            relationship = relationship,
            mobileNumber = mobileNumber,
            emailAddress = emailAddress,
        )
    }
}