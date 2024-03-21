package uk.gov.justice.digital.hmpps.service

import uk.gov.justice.digital.hmpps.api.model.*
import uk.gov.justice.digital.hmpps.integration.delius.entity.Person
import uk.gov.justice.digital.hmpps.integration.delius.entity.PersonAddress
import uk.gov.justice.digital.hmpps.integration.delius.entity.ReferenceData

fun Person.detail(aliases: List<Alias>, addresses: List<Address>) = PersonDetail(
    identifiers(),
    name(),
    dob,
    title?.asCodeDescription(),
    gender?.asCodeDescription(),
    nationality?.asCodeDescription(),
    ethnicity?.asCodeDescription(),
    ethnicityDescription,
    contactDetails(),
    aliases,
    addresses
)

fun Person.identifiers() =
    Identifiers(id, crn, nomsId?.trim(), prisonerNumber, pnc?.trim(), cro?.trim(), niNumber?.trim())

fun Person.name() =
    Name(
        firstname,
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

