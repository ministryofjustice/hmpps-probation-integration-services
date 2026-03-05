package uk.gov.justice.digital.hmpps.model

data class Name(
    val forename: String,
    val middleName: String? = null,
    val surname: String,
) {
    constructor(firstName: String, secondName: String?, thirdName: String?, surname: String)
        : this(firstName, listOfNotNull(secondName, thirdName).joinToString(" "), surname)
}