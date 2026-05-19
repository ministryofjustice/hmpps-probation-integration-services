package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.person.Disability as DisabilityEntity
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonFull
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonOffenderRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.getByCrn
import uk.gov.justice.digital.hmpps.model.Disability
import uk.gov.justice.digital.hmpps.model.KeyValue
import uk.gov.justice.digital.hmpps.model.OffenderAddress
import uk.gov.justice.digital.hmpps.model.OffenderAlias
import uk.gov.justice.digital.hmpps.model.OffenderContactDetails
import uk.gov.justice.digital.hmpps.model.OffenderDetail
import uk.gov.justice.digital.hmpps.model.OffenderLanguages
import uk.gov.justice.digital.hmpps.model.OffenderProfile
import uk.gov.justice.digital.hmpps.model.OtherIds
import uk.gov.justice.digital.hmpps.model.PhoneNumber
import uk.gov.justice.digital.hmpps.model.PhoneTypes
import uk.gov.justice.digital.hmpps.model.Provision
import uk.gov.justice.digital.hmpps.model.keyValueOf
import java.time.LocalDate

@Service
class OffenderService(
    private val personOffenderRepository: PersonOffenderRepository
) {
    fun getOffenderDetail(crn: String): OffenderDetail {
        val personOffender = personOffenderRepository.getByCrn(crn)
        return personOffender.toOffenderDetail()
    }
}

fun PersonFull.toOffenderDetail() = OffenderDetail(
    title = title?.description,
    preferredName = preferredName,
    firstName = forename,
    middleNames = listOfNotNull(secondName, thirdName),
    surname = surname,
    previousSurname = previousSurname,
    dateOfBirth = dateOfBirth,
    contactDetails = toContactDetails(),
    offenderAliases = offenderAliases.map {
        OffenderAlias(
            id = it.aliasID.toString(),
            dateOfBirth = it.dateOfBirth,
            firstName = it.firstName,
            middleNames = listOfNotNull(it.secondName, it.thirdName),
            surname = it.surname,
            gender = it.gender.description
        )
    },
    offenderProfile = toProfile(),
    otherIds = toOtherIds(),
    currentExclusion = currentExclusion,
    currentRestriction = currentRestriction,
    currentTier = currentTier?.description
)

fun PersonFull.toProfile() = OffenderProfile(
    gender = gender.description,
    genderIdentity = genderIdentity?.description,
    selfDescribedGender = genderIdentityDescription,
    disabilities = disabilities.filter { d -> !d.softDeleted }.sortedBy { it.startDate }.reversed().map {
        it.toDisability()
    },
    ethnicity = ethnicity?.description,
    immigrationStatus = immigrationStatus?.description,
    religion = religion?.description,
    nationality = nationality?.description,
    secondaryNationality = secondNationality?.description,
    sexualOrientation = sexualOrientation?.description,
    offenderLanguages = OffenderLanguages(
        languageConcerns = languageConcerns,
        primaryLanguage = language?.description,
        requiresInterpreter = requiresInterpreter
    ),
    provisions = provisions.sortedByDescending { it.startDate }.map {
        Provision(
            category = it.category?.let { cat -> KeyValue(cat.code, cat.description) },
            finishDate = it.finishDate,
            notes = it.notes,
            provisionId = it.id,
            provisionType = KeyValue(it.type.code, it.type.description),
            startDate = it.startDate,
            lastUpdatedDate = it.lastUpdated
        )
    },
)

fun PersonFull.toOtherIds() = OtherIds(
    crn = crn,
    croNumber = croNumber,
    immigrationNumber = immigrationNumber,
    mostRecentPrisonerNumber = mostRecentPrisonerNumber,
    niNumber = niNumber,
    nomsNumber = nomsNumber,
    pncNumber = pnc
)

fun PersonFull.toEmailAddresses(): List<String> = listOfNotNull(emailAddress)

fun PersonFull.toPhoneNumbers(): List<PhoneNumber> = listOfNotNull(
    if (telephoneNumber != null) PhoneNumber(telephoneNumber, PhoneTypes.TELEPHONE.name) else null,
    if (mobileNumber != null) PhoneNumber(mobileNumber, PhoneTypes.MOBILE.name) else null
)

fun PersonFull.toContactDetails() = OffenderContactDetails(
    allowSMS = allowSms,
    emailAddresses = toEmailAddresses(),
    phoneNumbers = toPhoneNumbers(),
    addresses = addresses.map { address ->
        OffenderAddress(
            from = address.startDate,
            to = address.endDate,
            noFixedAbode = address.noFixedAbode,
            addressNumber = address.addressNumber,
            buildingName = address.buildingName,
            streetName = address.streetName,
            district = address.district,
            town = address.town,
            county = address.county,
            postcode = address.postcode,
            telephoneNumber = address.telephoneNumber,
            status = address.status.keyValueOf(),
            type = address.type?.keyValueOf(),
            typeVerified = address.typeVerified ?: false,
            createdDatetime = address.createdDatetime.toLocalDateTime(),
            lastUpdatedDatetime = address.lastUpdatedDatetime.toLocalDateTime()
        )
    }
)

fun DisabilityEntity.toDisability() = Disability(
    lastUpdatedDateTime = lastUpdated.toLocalDateTime(),
    disabilityCondition = condition?.let { dis -> KeyValue(dis.code, dis.description) },
    disabilityId = id,
    disabilityType = KeyValue(type.code, type.description),
    endDate = finishDate,
    isActive = isActive(),
    notes = notes,
    startDate = startDate
)

fun DisabilityEntity.isActive(): Boolean {
    if (startDate.isAfter(LocalDate.now())) {
        return false
    }
    return finishDate == null || finishDate.isAfter(LocalDate.now())
}