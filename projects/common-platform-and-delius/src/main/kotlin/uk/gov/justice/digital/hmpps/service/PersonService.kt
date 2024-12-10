package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.integrations.client.OsClient
import uk.gov.justice.digital.hmpps.integrations.client.OsPlacesResponse
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonAddress
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonAddressRepository
import uk.gov.justice.digital.hmpps.messaging.Address
import uk.gov.justice.digital.hmpps.messaging.Defendant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period

@Service
class PersonService(
    auditedInteractionService: AuditedInteractionService,
    private val personRepository: PersonRepository,
    private val courtRepository: CourtRepository,
    private val equalityRepository: EqualityRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val teamRepository: TeamRepository,
    private val staffRepository: StaffRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val personAddressRepository: PersonAddressRepository,
    private val osClient: OsClient
) : AuditableService(auditedInteractionService) {

    @Transactional
    fun insertPerson(defendant: Defendant, courtCode: String): InsertPersonResult =
        audit(BusinessInteractionCode.INSERT_PERSON) { audit ->

            val dateOfBirth = defendant.personDefendant?.personDetails?.dateOfBirth
                ?: throw IllegalArgumentException("Date of birth not found in message")

            // Under 10 years old validation
            dateOfBirth.let {
                val age = Period.between(it, LocalDate.now()).years
                require(age > 10) {
                    "Date of birth would indicate person is under ten years old: $it"
                }
            }

            // Person record
            val savedPerson = personRepository.save(defendant.toPerson())

            val courtLinkedProvider = courtRepository.getByOuCode(courtCode).provider
            val initialAllocation = referenceDataRepository.initialAllocationReason()
            val unallocatedTeam = teamRepository.findByCode(courtLinkedProvider.code + "UAT")
            val unallocatedStaff = staffRepository.findByCode(unallocatedTeam.code + "U")

            // Person manager record
            val manager = PersonManager(
                person = savedPerson,
                staff = unallocatedStaff,
                team = unallocatedTeam,
                provider = courtLinkedProvider,
                softDeleted = false,
                active = true,
                allocationReason = initialAllocation,
                staffEmployeeID = unallocatedStaff.id,
                trustProviderTeamId = unallocatedTeam.id,
                allocationDate = LocalDateTime.of(1900, 1, 1, 0, 0)

            )
            val savedManager = personManagerRepository.save(manager)

            // Equality record
            val equality = Equality(
                id = null,
                personId = savedPerson.id!!,
                softDeleted = false,
            )

            val savedEquality = equalityRepository.save(equality)

            val addressInfo = defendant.personDefendant.personDetails.address
            val osPlacesResponse = addressInfo?.takeIf { it.containsInformation() && !it.postcode.isNullOrBlank() }
                ?.let { findAddressByFreeText(it) }

            val deliveryPointAddress = osPlacesResponse?.results?.firstOrNull()?.dpa

            val savedAddress = if (deliveryPointAddress != null) {
                insertAddress(
                    PersonAddress(
                        id = null,
                        start = LocalDate.now(),
                        status = referenceDataRepository.mainAddressStatus(),
                        person = savedPerson,
                        type = referenceDataRepository.awaitingAssessmentAddressType(),
                        postcode = deliveryPointAddress.postcode,
                        notes = "UPRN: ${deliveryPointAddress.uprn}",
                        buildingName = listOfNotNull(
                            deliveryPointAddress.subBuildingName,
                            deliveryPointAddress.buildingName
                        ).joinToString(" "),
                        addressNumber = deliveryPointAddress.buildingNumber?.toString(),
                        streetName = deliveryPointAddress.thoroughfareName,
                        town = deliveryPointAddress.postTown,
                        district = deliveryPointAddress.localCustodianCodeDescription
                    )
                )
            } else {
                addressInfo?.takeIf { it.containsInformation() }?.let {
                    insertAddress(
                        PersonAddress(
                            id = null,
                            start = LocalDate.now(),
                            status = referenceDataRepository.mainAddressStatus(),
                            person = savedPerson,
                            postcode = it.postcode,
                            type = referenceDataRepository.awaitingAssessmentAddressType(),
                            streetName = it.address1,
                            district = it.address2,
                            town = it.address3,
                            county = listOfNotNull(it.address4, it.address5).joinToString(", ")
                        )
                    )
                }
            }

            audit["offenderId"] = savedPerson.id
            InsertPersonResult(savedPerson, savedManager, savedEquality, savedAddress)
        }

    @Transactional
    fun insertAddress(address: PersonAddress): PersonAddress = audit(BusinessInteractionCode.INSERT_ADDRESS) { audit ->
        val savedAddress = personAddressRepository.save(address)
        audit["addressId"] = address.id!!
        savedAddress
    }

    fun generateCrn(): String {
        return personRepository.getNextCrn()
    }

    fun String.toDeliusGender() = ReferenceData.GenderCode.entries.find { it.commonPlatformValue == this }?.deliusValue
        ?: throw IllegalStateException("Gender not found: $this")

    fun Defendant.toPerson(): Person {
        val personDetails = personDefendant?.personDetails ?: throw IllegalArgumentException("No person found")
        val genderCode = personDetails.gender?.toDeliusGender() ?: throw IllegalArgumentException("Gender not found")

        return Person(
            id = null,
            crn = generateCrn(),
            croNumber = this.croNumber,
            pncNumber = this.pncId,
            forename = personDetails.firstName!!,
            secondName = personDetails.middleName,
            telephoneNumber = personDetails.contact?.home,
            mobileNumber = personDetails.contact?.mobile,
            surname = personDetails.lastName!!,
            dateOfBirth = personDetails.dateOfBirth!!,
            gender = referenceDataRepository.findByCodeAndDatasetCode(genderCode, DatasetCode.GENDER)!!,
            softDeleted = false,
            surnameSoundex = personRepository.getSoundex(personDetails.lastName),
            middleNameSoundex = personDetails.middleName?.let { personRepository.getSoundex(it) },
            firstNameSoundex = personRepository.getSoundex(personDetails.firstName),
        )
    }

    fun Address?.containsInformation(): Boolean {
        return this != null && listOf(
            this.address1, this.address2, this.address3,
            this.address4, this.address5, this.postcode
        ).any { !it.isNullOrBlank() }
    }

    fun findAddressByFreeText(address: Address): OsPlacesResponse {
        val freeText = address.toFreeText()
        return osClient.searchByFreeText(query = freeText, maxResults = 1, minMatch = 0.6)
    }

    fun Address.toFreeText(): String {
        return listOfNotNull(
            this.address1?.trim(),
            this.address2?.trim(),
            this.address3?.trim(),
            this.address4?.trim(),
            this.address5?.trim(),
            this.postcode?.trim()
        ).joinToString(", ")
    }
}