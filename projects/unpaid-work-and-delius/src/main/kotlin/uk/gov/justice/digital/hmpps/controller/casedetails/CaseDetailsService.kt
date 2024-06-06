package uk.gov.justice.digital.hmpps.controller.casedetails

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.controller.casedetails.entity.CaseRepository
import uk.gov.justice.digital.hmpps.controller.casedetails.entity.EventRepository
import uk.gov.justice.digital.hmpps.controller.casedetails.entity.getCase
import uk.gov.justice.digital.hmpps.controller.casedetails.entity.getEvent
import uk.gov.justice.digital.hmpps.controller.casedetails.model.*
import uk.gov.justice.digital.hmpps.integrations.common.model.*

@Service
class CaseDetailsService(
    val caseRepository: CaseRepository,
    val eventRepository: EventRepository
) {
    fun getCaseDetails(crn: String, eventId: Long): CaseDetails {
        val case = caseRepository.getCase(crn)
        val event = eventRepository.getEvent(eventId)
        return CaseDetails(
            crn = case.crn,
            name = Name(
                forename = case.forename,
                middleName = case.secondName,
                surname = case.surname
            ),
            dateOfBirth = case.dateOfBirth,
            gender = case.gender?.description,
            genderIdentity = case.genderIdentity?.description,
            croNumber = case.croNumber,
            pncNumber = case.pncNumber,
            aliases = case.aliases.map { Alias(it.name(), it.dateOfBirth) },
            emailAddress = case.emailAddress,
            phoneNumbers = listOfNotNull(
                case.mobileNumber?.let { PhoneNumber("MOBILE", it) },
                case.telephoneNumber?.let { PhoneNumber("TELEPHONE", it) }
            ),
            mainAddress = case.addresses.firstOrNull()?.let {
                Address(it.buildingName, it.addressNumber, it.streetName, it.district, it.town, it.county, it.postcode)
            },
            ethnicity = case.ethnicity?.description,
            disabilities = case.disabilities.map {
                Disability(
                    type = Type(it.type.code, it.type.description),
                    condition = it.condition?.let { c -> Type(c.code, c.description) },
                    notes = it.notes
                )
            },
            provisions = case.provisions.map {
                Provision(
                    type = Type(it.type.code, it.type.description),
                    category = it.category?.let { c -> Type(c.code, c.description) },
                    notes = it.notes
                )
            },
            language = case.primaryLanguage?.description?.let {
                Language(
                    requiresInterpreter = case.requiresInterpreter ?: false,
                    primaryLanguage = it
                )
            },
            personalCircumstances = case.personalCircumstances.map {
                PersonalCircumstance(
                    type = Type(it.type.code, it.type.description),
                    subType = it.subType?.let { t -> Type(t.code, t.description) },
                    notes = it.notes,
                    evidenced = it.evidenced ?: false
                )
            },
            personalContacts = case.personalContacts.map {
                PersonalContact(
                    relationship = it.relationship,
                    relationshipType = Type(it.relationshipType.code, it.relationshipType.description),
                    name = it.name(),
                    mobileNumber = it.mobileNumber,
                    telephoneNumber = it.address?.telephoneNumber,
                    address = it.address?.let { address ->
                        Address(
                            buildingName = address.buildingName,
                            addressNumber = address.addressNumber,
                            streetName = address.streetName,
                            district = address.district,
                            town = address.town,
                            county = address.county,
                            postcode = address.postcode
                        )
                    }
                )
            },
            mappaRegistration = case.registrations.filter { it.type.code == "MAPPA" }.maxByOrNull { it.startDate }
                ?.let {
                    MappaRegistration(
                        startDate = it.startDate,
                        level = Type(it.level.code, it.level.description),
                        category = Type(it.category.code, it.category.description)
                    )
                },
            registerFlags = case.registrations.map {
                RegisterFlag(
                    code = it.type.code,
                    description = it.type.description,
                    riskColour = it.type.riskColour
                )
            },
            sentence = if (event.mainOffence != null && event.disposal != null) {
                Sentence(
                    startDate = event.disposal.disposalDate,
                    mainOffence = MainOffence(
                        Type(
                            event.mainOffence.offence.mainCategoryCode,
                            event.mainOffence.offence.mainCategoryDescription
                        ),
                        Type(
                            event.mainOffence.offence.subCategoryCode,
                            event.mainOffence.offence.subCategoryDescription
                        )
                    )
                )
            } else {
                null
            }

        )
    }
}
