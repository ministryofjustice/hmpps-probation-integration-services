package uk.gov.justice.digital.hmpps.service

import uk.gov.justice.digital.hmpps.api.model.*
import uk.gov.justice.digital.hmpps.api.model.Alias
import uk.gov.justice.digital.hmpps.api.model.LimitedAccessUser
import uk.gov.justice.digital.hmpps.api.model.ReligionHistory
import uk.gov.justice.digital.hmpps.integration.delius.entity.*

fun Person.detail(
    aliases: List<Alias>,
    addresses: List<Address>,
    exclusions: LimitedAccess? = null,
    restrictions: LimitedAccess? = null,
    sentences: List<Sentence>,
    additionalIdentifiers: List<Identifier>,
    religionHistory: List<ReligionHistory>,
) = PersonDetail(
    identifiers = identifiers(additionalIdentifiers),
    name = name(),
    dateOfBirth = dateOfBirth,
    dateOfDeath = dateOfDeath,
    title = title?.asCodeDescription(),
    gender = gender?.asCodeDescription(),
    genderIdentity = genderIdentity?.asCodeDescription(),
    genderIdentityDescription = genderIdentityDescription,
    nationality = nationality?.asCodeDescription(),
    secondNationality = secondNationality?.asCodeDescription(),
    ethnicity = ethnicity?.asCodeDescription(),
    ethnicityDescription = ethnicityDescription,
    religion = religion?.asCodeDescription(),
    religionDescription = religionDescription,
    sexualOrientation = sexualOrientation?.asCodeDescription(),
    contactDetails = contactDetails(),
    aliases = aliases,
    addresses = addresses,
    excludedFrom = exclusions,
    restrictedTo = restrictions,
    sentences = sentences,
    religionHistory = religionHistory,
)

fun Person.identifiers(additionalIdentifiers: List<Identifier>) =
    Identifiers(
        deliusId = id,
        crn = crn,
        nomsId = nomsId?.trim(),
        prisonerNumber = prisonerNumber,
        pnc = pnc?.trim(),
        cro = cro?.trim(),
        ni = niNumber?.trim(),
        additionalIdentifiers = additionalIdentifiers,
    )

fun Person.name() =
    Name(
        firstName,
        listOfNotNull(secondName, thirdName).ifEmpty { null }?.joinToString(" "),
        surname,
        previousSurname,
        preferredName
    )

fun uk.gov.justice.digital.hmpps.integration.delius.entity.ReligionHistory.asModel() = ReligionHistory(
    code = referenceData?.code,
    description = referenceData?.description ?: religionDescription,
    startDate = startDate,
    endDate = endDate,
    lastUpdatedBy = lastUpdatedBy.distinguishedName,
    lastUpdatedAt = lastUpdatedAt,
)

fun Person.contactDetails() = ContactDetails.of(telephoneNumber, mobileNumber, emailAddress)

fun ReferenceData.asCodeDescription() = CodeDescription(code, description)

fun AdditionalIdentifier.asModel() = Identifier(
    type = type.asCodeDescription(),
    value = value,
)

fun uk.gov.justice.digital.hmpps.integration.delius.entity.Alias.asModel() = Alias(
    Name(
        firstName,
        listOfNotNull(secondName, thirdName).ifEmpty { null }?.joinToString(" "),
        surname
    ),
    dateOfBirth,
    gender?.asCodeDescription(),
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
        buildingName = buildingName,
        addressNumber = addressNumber,
        streetName = streetName,
        district = district,
        townCity = townCity,
        county = county,
        postcode = postcode,
        uprn = uprn,
        telephoneNumber = telephoneNumber,
        noFixedAbode = noFixedAbode,
        status = status.asCodeDescription(),
        notes = notes,
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
