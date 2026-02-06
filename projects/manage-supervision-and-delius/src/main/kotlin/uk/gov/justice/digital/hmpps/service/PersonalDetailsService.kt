package uk.gov.justice.digital.hmpps.service

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import uk.gov.justice.digital.hmpps.alfresco.AlfrescoClient
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.PersonSummary
import uk.gov.justice.digital.hmpps.api.model.overview.PersonalCircumstance
import uk.gov.justice.digital.hmpps.api.model.personalDetails.*
import uk.gov.justice.digital.hmpps.api.model.personalDetails.Disability
import uk.gov.justice.digital.hmpps.api.model.personalDetails.Document
import uk.gov.justice.digital.hmpps.api.model.personalDetails.Provision
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.exception.InvalidRequestException
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.ContactAddress
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.*
import uk.gov.justice.digital.hmpps.messaging.Notifier
import java.time.LocalDate
import kotlin.jvm.optionals.getOrNull

@Service
class PersonalDetailsService(
    auditedInteractionService: AuditedInteractionService,
    private val personRepository: PersonRepository,
    private val addressRepository: PersonAddressRepository,
    private val documentRepository: DocumentRepository,
    private val provisionRepository: ProvisionRepository,
    private val disabilityRepository: DisabilityRepository,
    private val personalCircumstanceRepository: PersonCircumstanceRepository,
    private val aliasRepository: AliasRepository,
    private val personalContactRepository: PersonalContactRepository,
    private val alfrescoClient: AlfrescoClient,
    private val referenceDataRepository: ReferenceDataRepository,
    private val notifier: Notifier,
    private val transactionTemplate: TransactionTemplate,
    private val contactService: ContactService
) : AuditableService(auditedInteractionService) {

    fun getContactInformation(crn: String): PersonContactInformation = personRepository.getPerson(crn).let {
        PersonContactInformation(it.crn, it.telephoneNumber, it.mobileNumber, it.emailAddress)
    }

    fun updatePersonContactDetails(crn: String, request: PersonContactEditRequest): PersonalDetails {

        val person = personRepository.getPerson(crn)

        person.telephoneNumber = request.phoneNumber
        person.mobileNumber = request.mobileNumber
        person.emailAddress = request.emailAddress

        val updated = updatePersonContact(person)

        notifier.caseUpdated(updated)
        return getPersonalDetails(crn)
    }

    fun updatePersonalAddressDetails(crn: String, request: PersonAddressEditRequest): PersonalDetails {

        val startDate = request.startDate ?: throw InvalidRequestException("Start date must be provided")
        val addressType = request.addressTypeCode?.let {
            referenceDataRepository.getAddressTypeByCode(it)
        }

        if (request.endDate != null && request.endDate > LocalDate.now()) {
            throw InvalidRequestException("End date must not be later than today")
        }

        if (startDate > LocalDate.now()) {
            throw InvalidRequestException("Start date must not be later than today")
        }

        if (request.endDate != null && startDate > request.endDate) {
            throw InvalidRequestException("Start date must not be later than end date")
        }

        val person = personRepository.getPerson(crn)
        val mainAddress = addressRepository.findByPersonId(person.id)
            .filter { it.endDate == null }
            .firstOrNull { it.status.code == "M" }

        val updatedAddress = toUpdatedAddress(person.id, mainAddress, addressType, request, startDate)
        val isAddressUpdate = (updatedAddress.id != null)
        val updated = updatePersonAddress(updatedAddress)

        if (isAddressUpdate) {
            notifier.addressUpdated(updated, crn)
        } else {
            notifier.addressCreated(updated, crn)
        }

        return getPersonalDetails(crn)
    }

    private fun updatePersonContact(person: Person): Person =
        transactionTemplate.execute {
            updatePerson(person)
        }!!

    private fun updatePersonAddress(personAddress: PersonAddress): PersonAddress =
        transactionTemplate.execute {
            val updatedAddress = if (personAddress.id == null) {
                createMainAddress(personAddress)
            } else {
                updateMainAddress(personAddress)
            }

            updatedAddress
        }!!

    private fun createMainAddress(personAddress: PersonAddress) =
        audit(BusinessInteractionCode.INSERT_ADDRESS) { audit ->
            audit["offenderId"] = personAddress.personId
            val new = addressRepository.save(personAddress)
            new.id?.let { audit["offender_address_id"] = it }
            return@audit new
        }

    private fun updateMainAddress(personAddress: PersonAddress) =
        audit(BusinessInteractionCode.UPDATE_ADDRESS) { audit ->
            audit["offenderId"] = personAddress.personId
            personAddress.id?.let { audit["offender_address_id"] = it }
            val updated = addressRepository.save(personAddress)
            return@audit updated
        }

    private fun updatePerson(person: Person) = audit(BusinessInteractionCode.UPDATE_PERSON) { audit ->
        audit["offenderId"] = person.id
        return@audit personRepository.save(person)
    }

    private fun toUpdatedAddress(
        personId: Long,
        mainAddress: PersonAddress?,
        addressType: ReferenceData?,
        request: PersonAddressEditRequest,
        startDate: LocalDate
    ): PersonAddress {
        val postcode = if (request.noFixedAddress == true) "NF1 1NF" else request.postcode
        val status =
            if (request.endDate != null) referenceDataRepository.getPreviousAddressType() else referenceDataRepository.getMainAddressType()
        return if (mainAddress != null) {
            mainAddress.buildingName = request.buildingName
            mainAddress.buildingNumber = request.buildingNumber
            mainAddress.streetName = request.streetName
            mainAddress.district = request.district
            mainAddress.town = request.town
            mainAddress.county = request.county
            mainAddress.postcode = postcode
            mainAddress.type = addressType
            mainAddress.typeVerified = request.verified
            mainAddress.noFixedAbode = request.noFixedAddress ?: false
            mainAddress.startDate = startDate
            mainAddress.endDate = request.endDate
            mainAddress.notes = listOfNotNull(mainAddress.notes, request.notes).joinToString(System.lineSeparator())
            mainAddress.status = status
            mainAddress
        } else {
            PersonAddress(
                personId = personId,
                status = status,
                type = addressType,
                buildingName = request.buildingName,
                buildingNumber = request.buildingNumber,
                streetName = request.streetName,
                district = request.district,
                town = request.town,
                county = request.county,
                postcode = postcode,
                startDate = startDate,
                endDate = request.endDate,
                typeVerified = request.verified,
                noFixedAbode = request.noFixedAddress ?: false,
                notes = request.notes,
                id = null
            )
        }
    }

    fun getPersonalDetails(crn: String): PersonalDetails {
        val person = personRepository.getPerson(crn)
        val provisions = provisionRepository.findByPersonId(person.id)
        val personalCircumstances = personalCircumstanceRepository.findCurrentCircumstances(person.id)
        val disabilities = disabilityRepository.findByPersonId(person.id)
        val allAddresses = addressRepository.findByPersonId(person.id)
        val currentAddresses = allAddresses.filter { it.endDate == null }
        val previousAddresses =
            allAddresses.filter { it.endDate != null && it.status.code == AddressStatus.PREVIOUS.code }
                .map(PersonAddress::toAddress).mapNotNull { it }
        val aliases = aliasRepository.findByPersonId(person.id)
        val personalContacts = personalContactRepository.findByPersonId(person.id)
        val mainAddress = currentAddresses.firstOrNull { it.status.code == AddressStatus.MAIN.code }?.toAddress()
        val otherAddresses =
            currentAddresses.filter { it.status.code != AddressStatus.MAIN.code }.map(PersonAddress::toAddress)
                .mapNotNull { it }
        val documents = documentRepository.findByPersonId(person.id)
        val addressTypes =
            referenceDataRepository.findByDatasetCode(DatasetCode.ADDRESS_TYPE.code)
                .map { AddressType(it.code, it.description) }

        return PersonalDetails(
            crn = person.crn,
            name = person.name(),
            mainAddress = mainAddress,
            otherAddressCount = otherAddresses.size,
            previousAddressCount = previousAddresses.size,
            contacts = personalContacts.map(PersonalContactEntity::toContact),
            preferredGender = person.gender.description,
            dateOfBirth = person.dateOfBirth,
            dateOfDeath = person.dateOfDeath,
            preferredName = person.preferredName,
            telephoneNumber = person.telephoneNumber,
            mobileNumber = person.mobileNumber,
            circumstances = Circumstances(
                lastUpdated = personalCircumstances.maxOfOrNull { it.lastUpdated },
                circumstances = personalCircumstances.map {
                    PersonalCircumstance(
                        it.subType.description,
                        it.type.description
                    )
                }),
            disabilities = Disabilities(
                lastUpdated = disabilities.maxOfOrNull { it.lastUpdated },
                disabilities = disabilities.map { it.type.description }),
            provisions = Provisions(
                lastUpdated = provisions.maxOfOrNull { it.lastUpdated },
                provisions = provisions.map { it.type.description }),
            documents = documents.map(PersonDocument::toDocument),
            pnc = person.pnc,
            noms = person.noms,
            religionOrBelief = person.religion?.description,
            sex = person.gender.description,
            sexualOrientation = person.sexualOrientation?.description,
            email = person.emailAddress,
            preferredLanguage = person.language?.description,
            previousSurname = person.previousSurname,
            aliases = aliases.map { Name(forename = it.forename, middleName = it.secondName, it.surname) },
            genderIdentity = person.genderIdentity?.description,
            selfDescribedGender = person.genderIdentityDescription,
            requiresInterpreter = person.requiresInterpreter,
            lastUpdated = person.lastUpdatedDatetime.toLocalDate(),
            lastUpdatedBy = person.lastUpdatedUser?.let { Name(forename = it.forename, surname = it.surname) },
            addressTypes = addressTypes,
            staffContacts = contactService.getActivePersonManagers(person.id)
        )
    }

    fun getMainAddressSingleNote(crn: String, noteId: Int): PersonalDetailsSummary {
        val person = personRepository.getPerson(crn)
        val allAddresses = addressRepository.findByPersonId(person.id)
        val currentAddresses = allAddresses.filter { it.endDate == null }
        val mainAddress = currentAddresses.firstOrNull { it.status.code == AddressStatus.MAIN.code }
        val personalContacts = personalContactRepository.findByPersonId(person.id)
        val previousAddresses =
            allAddresses.filter { it.endDate != null && it.status.code == AddressStatus.PREVIOUS.code }
                .map(PersonAddress::toAddress).mapNotNull { it }
        val otherAddresses =
            currentAddresses.filter { it.status.code != AddressStatus.MAIN.code }.map(PersonAddress::toAddress)
                .mapNotNull { it }

        return PersonalDetailsSummary(
            crn = person.crn,
            name = person.name(),
            contacts = personalContacts.map(PersonalContactEntity::toContact),
            mainAddress = mainAddress?.toAddress(singleNote = true, noteId = noteId),
            otherAddressCount = otherAddresses.size,
            previousAddressCount = previousAddresses.size,
            telephoneNumber = person.telephoneNumber,
            mobileNumber = person.mobileNumber,
            email = person.emailAddress,
        )
    }

    fun downloadDocument(crn: String, id: String): ResponseEntity<StreamingResponseBody> {
        val filename = documentRepository.getDocument(crn, id)
        return alfrescoClient.streamDocument(id, filename)
    }

    fun getPersonContact(crn: String, contactId: Long): PersonalContact {
        return personalContactRepository.getContact(crn, contactId).toContact()
    }

    fun getPersonContactSingleNote(crn: String, contactId: Long): PersonalContact {
        return personalContactRepository.getContact(crn, contactId).toContact(true, 0)
    }

    fun getPersonSummary(crn: String): PersonSummary {
        return personRepository.getSummary(crn).toPersonSummary()
    }

    fun getPersonAddresses(crn: String): AddressOverview {
        val person = personRepository.getSummary(crn)
        val addresses = addressRepository.findByPersonId(person.id)
        val currentAddresses = addresses.filter { it.endDate == null }

        return AddressOverview(
            personSummary = person.toPersonSummary(),
            mainAddress = currentAddresses.firstOrNull { it.status.code == "M" }?.toAddress(),
            otherAddresses = currentAddresses.filter { it.status.code != "M" }.map(PersonAddress::toAddress)
                .mapNotNull { it },
            previousAddresses = addresses.filter { it.endDate != null && it.status.code == AddressStatus.PREVIOUS.code }
                .map(PersonAddress::toAddress).mapNotNull { it }
                .sortedWith(compareByDescending<Address> { it.from }.thenByDescending { it.to })
        )
    }

    fun getPersonAddressSingleNote(crn: String, id: Long, noteId: Int): AddressOverviewSummary {
        val person = personRepository.getSummary(crn)

        return AddressOverviewSummary(
            personSummary = person.toPersonSummary(),
            addressRepository.findById(id).getOrNull()?.toAddress(singleNote = true, noteId = noteId),
        )
    }

    fun getPersonCircumstances(crn: String): CircumstanceOverview {
        val person = personRepository.getSummary(crn)
        val circumstances = personalCircumstanceRepository.findAllCircumstances(person.id)
            .partition { it.endDate == null || it.endDate > LocalDate.now() }

        return CircumstanceOverview(
            personSummary = person.toPersonSummary(),
            circumstances = circumstances.first.map { it.toCircumstance() },
            previousCircumstances = circumstances.second.map { it.toCircumstance() },
        )
    }

    fun getPersonCircumstancesSingleNote(crn: String, circumstanceId: Long, noteId: Int): CircumstanceOverviewSummary {
        val person = personRepository.getSummary(crn)
        val circumstance = personalCircumstanceRepository.findById(circumstanceId).getOrNull()

        return CircumstanceOverviewSummary(
            personSummary = person.toPersonSummary(),
            circumstance = circumstance?.toCircumstance(singleNote = true, noteId = noteId)
        )
    }

    fun getPersonProvisions(crn: String): ProvisionOverview {
        val person = personRepository.getSummary(crn)
        val provisions = provisionRepository.findByPersonId(person.id)

        return ProvisionOverview(
            personSummary = person.toPersonSummary(),
            provisions = provisions.map { it.toProvision() }
        )
    }

    fun getPersonProvisionsSingleNote(crn: String, provisionId: Long, noteId: Int): ProvisionOverviewSummary {
        val person = personRepository.getSummary(crn)
        val provision = provisionRepository.findById(provisionId).getOrNull()

        return ProvisionOverviewSummary(
            personSummary = person.toPersonSummary(),
            provision = provision?.toProvision(singleNote = true, noteId = noteId)
        )
    }

    fun getPersonDisabilities(crn: String): DisabilityOverview {
        val person = personRepository.getSummary(crn)
        val disabilities = disabilityRepository.findByPersonId(person.id)

        return DisabilityOverview(
            personSummary = person.toPersonSummary(),
            disabilities = disabilities.mapIndexed { index, disability -> disability.toDisability(index) },
        )
    }

    fun getPersonDisabilitySingleNote(crn: String, disabilityId: Int, noteId: Int): DisabilityOverview {
        val person = personRepository.getSummary(crn)
        val disabilities = disabilityRepository.findByPersonId(person.id)

        val disabilityEntity = disabilities.elementAtOrNull(disabilityId)

        return DisabilityOverview(
            personSummary = person.toPersonSummary(),
            disability = disabilityEntity?.let {
                Disability(
                    disabilityId,
                    disabilityEntity.type.description,
                    disabilityNote = formatNote(disabilityEntity.notes, false).elementAtOrNull(noteId),
                    startDate = disabilityEntity.startDate,
                    lastUpdated = disabilityEntity.lastUpdated,
                    lastUpdatedBy = Name(
                        forename = disabilityEntity.lastUpdatedUser.forename,
                        surname = disabilityEntity.lastUpdatedUser.surname
                    )
                )
            }
        )
    }
}

fun uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.PersonalCircumstance.toCircumstance(
    singleNote: Boolean = false,
    noteId: Int? = null
) =
    Circumstance(
        id = id,
        type = type.description,
        subType = subType.description,
        circumstanceNotes = if (!singleNote) formatNote(notes, true) else null,
        circumstanceNote = if (singleNote) formatNote(notes, false).elementAtOrNull(noteId!!) else null,
        verified = evidenced,
        startDate = startDate,
        endDate = endDate,
        lastUpdated = lastUpdated,
        lastUpdatedBy = Name(forename = lastUpdatedUser.forename, surname = lastUpdatedUser.surname)
    )

fun uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Provision.toProvision(
    singleNote: Boolean = false,
    noteId: Int? = null
) = Provision(
    id = id,
    description = type.description,
    provisionNotes = if (!singleNote) formatNote(notes, true) else null,
    provisionNote = if (singleNote) formatNote(notes, false).elementAtOrNull(noteId!!) else null,
    startDate = startDate,
    lastUpdated = lastUpdated,
    lastUpdatedBy = Name(forename = lastUpdatedUser.forename, surname = lastUpdatedUser.surname)
)

fun uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Disability.toDisability(disabilityIndex: Int) =
    Disability(
        disabilityId = disabilityIndex,
        description = type.description,
        disabilityNotes = formatNote(notes, true),
        startDate = startDate,
        lastUpdated = lastUpdated,
        lastUpdatedBy = Name(forename = lastUpdatedUser.forename, surname = lastUpdatedUser.surname)
    )

fun PersonalContactEntity.toContact(singleNote: Boolean = false, noteId: Int? = null) = PersonalContact(
    personSummary = person.toSummary(),
    name = Name(forename, middleNames, surname),
    relationship = relationship,
    address = address?.toAddress(),
    contactNotes = if (!singleNote) formatNote(notes, true) else null,
    contactNote = if (singleNote) formatNote(notes, false).elementAtOrNull(noteId!!) else null,
    relationshipType = relationshipType.description,
    contactId = id,
    lastUpdated = lastUpdated,
    email = email,
    phone = mobileNumber,
    lastUpdatedBy = Name(forename = lastUpdatedUser.forename, surname = lastUpdatedUser.surname),
    startDate = startDate
)

fun Person.toSummary() =
    PersonSummary(
        name = Name(forename, secondName, surname),
        pnc = pnc,
        dateOfBirth = dateOfBirth,
        crn = crn,
        noms = noms,
        offenderId = id
    )

fun Person.name() = Name(forename, listOfNotNull(secondName, thirdName).joinToString(" "), surname)
fun PersonAddress.toAddress(singleNote: Boolean = false, noteId: Int? = null) = Address.from(
    id = id,
    buildingName = buildingName,
    buildingNumber = buildingNumber,
    streetName = streetName,
    district = district,
    town = town,
    county = county,
    postcode = postcode,
    from = startDate,
    to = endDate,
    verified = typeVerified,
    lastUpdated = lastUpdated,
    status = status.description,
    type = type?.description,
    typeCode = type?.code,
    telephoneNumber = telephoneNumber,
    lastUpdatedBy = lastUpdatedUser?.let {
        Name(
            forename = lastUpdatedUser.forename,
            surname = lastUpdatedUser.surname
        )
    },
    addressNotes = if (!singleNote) formatNote(notes, true) else null,
    addressNote = if (singleNote) formatNote(notes, false).elementAtOrNull(noteId!!) else null,
    noFixedAddress = noFixedAbode

)

fun ContactAddress.toAddress() = uk.gov.justice.digital.hmpps.api.model.personalDetails.ContactAddress.from(
    buildingName = buildingName,
    buildingNumber = addressNumber,
    streetName = streetName,
    district = district,
    town = town,
    county = county,
    postcode = postcode,
    lastUpdated = lastUpdated,
    lastUpdatedBy = Name(forename = lastUpdatedUser.forename, surname = lastUpdatedUser.surname)
)

fun PersonDocument.toDocument() =
    Document(id = alfrescoId, name = name, createdAt = createdAt, lastUpdated = lastUpdated)

fun PersonSummaryEntity.toPersonSummary() =
    PersonSummary(Name(forename, secondName, surname), crn, id, pnc, noms, dateOfBirth.toLocalDate())