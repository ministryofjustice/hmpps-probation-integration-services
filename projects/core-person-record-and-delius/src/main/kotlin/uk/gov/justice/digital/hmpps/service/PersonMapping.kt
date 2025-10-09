package uk.gov.justice.digital.hmpps.service

import uk.gov.justice.digital.hmpps.api.model.*
import uk.gov.justice.digital.hmpps.api.model.Alias
import uk.gov.justice.digital.hmpps.api.model.LimitedAccessUser
import uk.gov.justice.digital.hmpps.integration.delius.entity.*

fun Person.detail(
    aliases: List<Alias>,
    addresses: List<Address>,
    exclusions: LimitedAccess? = null,
    restrictions: LimitedAccess? = null,
    sentences: List<Sentence>,
) = PersonDetail(
    identifiers = identifiers(),
    name = name(),
    dateOfBirth = dob,
    title = title?.asCodeDescription(),
    gender = gender?.asCodeDescription(),
    nationality = nationality?.asCodeDescription(),
    secondNationality = secondNationality?.asCodeDescription(),
    ethnicity = ethnicity?.asCodeDescription(),
    ethnicityDescription = ethnicityDescription,
    contactDetails = contactDetails(),
    sexualOrientation = sexualOrientation?.asCodeDescription(),
    aliases = aliases,
    addresses = addresses,
    excludedFrom = exclusions,
    restrictedTo = restrictions,
    sentences = sentences,
)

fun Person.identifiers() =
    Identifiers(id, crn, nomsId?.trim(), prisonerNumber, pnc?.trim(), cro?.trim(), niNumber?.trim())

fun Person.name() =
    Name(
        firstName,
        listOfNotNull(secondName, thirdName).ifEmpty { null }?.joinToString(" "),
        surname,
        previousSurname,
        preferredName
    )

fun Person.contactDetails() = ContactDetails.of(telephoneNumber, mobileNumber, emailAddress)

fun ReferenceData.asCodeDescription() = CodeDescription(code, description)

fun uk.gov.justice.digital.hmpps.integration.delius.entity.Alias.asModel() = Alias(
    Name(
        firstName,
        listOfNotNull(secondName, thirdName).ifEmpty { null }?.joinToString(" "),
        surname
    ),
    dateOfBirth
)

fun Disposal.asModel() = Sentence(
    startDate,
    active
)

fun PersonAddress.asAddress() = postcode?.let {
    Address(
        fullAddress = listOf(
            buildingName,
            listOf(addressNumber, streetName).trimAndJoin(" "),
            district,
            townCity,
            county,
            postcode
        ).trimAndJoin(),
        postcode = postcode,
        noFixedAbode = noFixedAbode,
        status = status.asCodeDescription(),
        startDate = startDate,
        endDate = endDate,
    )
}

private fun List<String?>.trimAndJoin(separator: String = ", ") =
    filterNotNull().filter { it.isNotBlank() }.joinToString(separator) { it.trim() }

fun List<Exclusion>.exclusionsAsLimitedAccess(message: String?) = if (isNotEmpty()) {
    LimitedAccess(
        message = message,
        users = map { LimitedAccessUser(it.user.username) }
    )
} else null

fun List<Restriction>.restrictionsAsLimitedAccess(message: String?) = if (isNotEmpty()) {
    LimitedAccess(
        message = message,
        users = map { LimitedAccessUser(it.user.username) }
    )
} else null
