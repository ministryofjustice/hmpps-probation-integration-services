package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.springframework.stereotype.Service
import software.amazon.awssdk.utils.ImmutableMap
import uk.gov.justice.digital.hmpps.api.model.*
import uk.gov.justice.digital.hmpps.integrations.delius.entity.DocumentEntity
import uk.gov.justice.digital.hmpps.integrations.delius.entity.DocumentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.getPreviousConviction
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.getPerson
import java.time.LocalDate
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Disability as DisabilityEntity

@Service
class OffenderService(
    private val documentRepository: DocumentRepository,
    private val personRepository: PersonRepository,
) {

    fun getOffenderDetailSummary(crn: String): OffenderDetailSummary {
        val person = personRepository.getPerson(crn)
        val previousConviction = documentRepository.getPreviousConviction(person.id)
        return person.toOffenderSummary(previousConviction)
    }

    fun getOffenderDetail(crn: String): OffenderDetail {
        val person = personRepository.getPerson(crn)
        val previousConviction = documentRepository.getPreviousConviction(person.id)
        return person.toOffenderDetail(previousConviction)
    }
}

fun DisabilityEntity.isActive(): Boolean {
    if (startDate.isAfter(LocalDate.now())) {
        return false
    }
    return finishDate == null || finishDate.isAfter(LocalDate.now())
}

fun Person.toPhoneNumbers(): List<PhoneNumber>? {
    val phoneNumbers = listOfNotNull(
        if (telephoneNumber != null) PhoneNumber(telephoneNumber, PhoneTypes.TELEPHONE.name) else null,
        if (mobileNumber != null) PhoneNumber(mobileNumber, PhoneTypes.MOBILE.name) else null
    )
    return phoneNumbers.ifEmpty { null }
}

fun Person.toEmailAddresses(): List<String>? {
    return if (emailAddress != null) listOf(emailAddress) else null
}

fun Person.toOffenderSummary(previousConviction: DocumentEntity?) = OffenderDetailSummary(
    preferredName = preferredName,
    activeProbationManagedSentence = currentDisposal,
    contactDetails = ContactDetailsSummary(
        allowSMS = allowSms,
        emailAddresses = toEmailAddresses(),
        phoneNumbers = toPhoneNumbers()
    ),
    currentDisposal = if (currentDisposal) "1" else "0",
    currentExclusion = currentExclusion,
    currentRestriction = currentRestriction,
    dateOfBirth = dateOfBirth,
    firstName = forename,
    middleNames = listOfNotNull(secondName, thirdName).takeIf { it.isNotEmpty() },
    surname = surname,
    gender = gender.description,
    offenderId = id,
    offenderProfile = toProfile(previousConviction),
    otherIds = toOtherIds(),
    partitionArea = partitionArea.area,
    previousSurname = previousSurname,
    softDeleted = softDeleted,
    title = title?.description
)

fun Person.toContactDetails() = ContactDetails(
    allowSMS = allowSms,
    emailAddresses = toEmailAddresses(),
    phoneNumbers = toPhoneNumbers(),
    addresses = addresses.map { address ->
        Address(
            from = address.startDate,
            to = address.endDate,
            noFixedAbode = address.noFixedAbode,
            notes = address.notes?.takeIf { it.isNotBlank() },
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
            latestAssessmentDate = address.addressAssessments.map { it.assessmentDate }.maxByOrNull { it },
            createdDatetime = address.createdDatetime.toLocalDateTime(),
            lastUpdatedDatetime = address.lastUpdatedDatetime.toLocalDateTime()
        )
    }.takeIf { addresses.isNotEmpty() }
)

fun Person.toOtherIds() = OtherIds(
    crn = crn,
    croNumber = croNumber,
    immigrationNumber = immigrationNumber,
    mostRecentPrisonerNumber = mostRecentPrisonerNumber,
    niNumber = niNumber,
    nomsNumber = nomsNumber,
    pncNumber = pnc
)

fun Person.toOffenderManagers() = offenderManagers.sortedByDescending { it.date }.map { manager ->
    OffenderManager(
        trustOfficer = Human(
            forenames = listOfNotNull(manager.officer.forename, manager.officer.forename2).joinToString(" "),
            surname = manager.officer.surname
        ),
        softDeleted = manager.softDeleted,
        partitionArea = manager.partitionArea.area,
        staff = manager.staff?.let { staff ->
            StaffHuman(
                code = staff.code,
                forenames = listOfNotNull(staff.forename, staff.forename2).joinToString(" "),
                surname = staff.surname,
                unallocated = staff.isUnallocated()
            )
        },
        providerEmployee = manager.providerEmployee.let { emp ->
            emp?.surname?.let { surname ->
                Human(
                    forenames = listOfNotNull(emp.forename, emp.forename2).joinToString(" "),
                    surname = surname
                )
            }
        },
        team = manager.team?.let { team ->
            Team(
                code = team.code,
                description = team.description,
                telephone = team.telephone,
                //emailAddress = team.emailAddress,
                localDeliveryUnit = KeyValue(team.district.code, team.district.description),
                district = KeyValue(team.district.code, team.district.description),
                borough = KeyValue(team.district.borough.code, team.district.borough.description)
            )
        },
        probationArea = ProbationArea(
            code = manager.provider.code,
            description = manager.provider.description,
            nps = !manager.provider.privateSector
        ),
        active = manager.active,
        fromDate = manager.date.toLocalDate(),
        toDate = manager.endDate,
        allocationReason = manager.allocationReason.keyValueOf()
    )
}

fun Person.toOffenderDetail(previousConviction: DocumentEntity?) = OffenderDetail(
    preferredName = preferredName,
    activeProbationManagedSentence = currentDisposal,
    contactDetails = toContactDetails(),
    offenderAliases = offenderAliases.map {
        OffenderAlias(
            id = it.aliasID.toString(),
            dateOfBirth = it.dateOfBirth,
            firstName = it.firstName,
            middleNames = listOfNotNull(it.secondName, it.thirdName).takeIf { it.isNotEmpty() },
            surname = it.surname,
            gender = it.gender.description
        )
    }.takeIf { offenderAliases.isNotEmpty() },
    currentDisposal = if (currentDisposal) "1" else "0",
    currentExclusion = currentExclusion,
    exclusionMessage = exclusionMessage,
    restrictionMessage = restrictionMessage,
    currentRestriction = currentRestriction,
    dateOfBirth = dateOfBirth,
    firstName = forename,
    middleNames = listOfNotNull(secondName, thirdName).takeIf { it.isNotEmpty() },
    surname = surname,
    gender = gender.description,
    offenderId = id,
    offenderProfile = toProfile(previousConviction),
    otherIds = toOtherIds(),
    offenderManagers = toOffenderManagers().takeIf { it.isNotEmpty() },
    partitionArea = partitionArea.area,
    previousSurname = previousSurname,
    softDeleted = softDeleted,
    title = title?.description,
    currentTier = currentTier?.description
)

fun Person.toAliases() = offenderAliases.map {
    OffenderAlias(
        id = it.aliasID.toString(),
        dateOfBirth = it.dateOfBirth,
        firstName = it.firstName,
        middleNames = listOfNotNull(it.secondName, it.thirdName).takeIf { it.isNotEmpty() },
        surname = it.surname,
        gender = it.gender.description
    )
}.takeIf { it.isNotEmpty() }

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

fun DocumentEntity?.toPreviousConviction() = PreviousConviction(
    convictionDate = this?.dateProduced?.toLocalDate(),
    detail = this?.name?.let { ImmutableMap.of("documentName", it) })

fun Person.toProfile(previousConviction: DocumentEntity?) = OffenderProfile(
    genderIdentity = genderIdentity?.description,
    selfDescribedGender = genderIdentityDescription,
    disabilities = disabilities.filter { d -> !d.softDeleted }.sortedBy { it.startDate }.reversed().map {
        it.toDisability()
    }.takeIf { disabilities.isNotEmpty() },
    ethnicity = ethnicity?.description,
    immigrationStatus = immigrationStatus?.description,
    nationality = nationality?.description,
    offenderDetails = offenderDetails,
    offenderLanguages = OffenderLanguages(
        languageConcerns = languageConcerns,
        primaryLanguage = language?.description,
        requiresInterpreter = requiresInterpreter
    ),
    previousConviction = previousConviction.toPreviousConviction(),

    provisions = provisions.sortedByDescending { it.startDate }.map {
        Provision(
            category = it.category?.let { cat -> KeyValue(cat.code, cat.description) },
            finishDate = it.finishDate,
            notes = it.notes,
            provisionId = it.id,
            provisionType = KeyValue(it.type.code, it.type.description),
            startDate = it.startDate
        )
    }.takeIf { provisions.isNotEmpty() },
    religion = religion?.description,
    remandStatus = currentRemandStatus,
    riskColour = currentHighestRiskColour,
    sexualOrientation = sexualOrientation?.description,
    secondaryNationality = secondNationality?.description

)
