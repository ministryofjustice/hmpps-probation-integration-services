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
) = PersonDetail(
    identifiers = identifiers(),
    name = name(),
    dateOfBirth = dob,
    title = title?.asCodeDescription(),
    gender = gender?.asCodeDescription(),
    nationality = nationality?.asCodeDescription(),
    ethnicity = ethnicity?.asCodeDescription(),
    ethnicityDescription = ethnicityDescription,
    contactDetails = contactDetails(),
    aliases = aliases,
    addresses = addresses,
    excludedFrom = exclusions,
    restrictedTo = restrictions,
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

fun PersonAddress.asAddress() = postcode?.let { Address(it) }

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
