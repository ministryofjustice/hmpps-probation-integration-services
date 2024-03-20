package uk.gov.justice.digital.hmpps.service

import uk.gov.justice.digital.hmpps.api.model.*
import uk.gov.justice.digital.hmpps.integration.delius.entity.Person
import uk.gov.justice.digital.hmpps.integration.delius.entity.ReferenceData

fun Person.detail() = PersonDetail(
    identifiers(),
    name(),
    dob,
    title?.asCodeDescription(),
    gender?.asCodeDescription(),
    nationality?.asCodeDescription(),
    ethnicity?.asCodeDescription(),
    ethnicityDescription,
    contactDetails()
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

