package uk.gov.justice.digital.hmpps.service

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.simple.SimpleJdbcCall
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonAddress
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonAddressRepository
import uk.gov.justice.digital.hmpps.messaging.Address
import uk.gov.justice.digital.hmpps.messaging.Defendant
import uk.gov.justice.digital.hmpps.messaging.Notifier
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class PersonService(
    jdbcTemplate: JdbcTemplate,
    auditedInteractionService: AuditedInteractionService,
    private val notifier: Notifier,
    private val personRepository: PersonRepository,
    private val courtRepository: CourtRepository,
    private val equalityRepository: EqualityRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val teamRepository: TeamRepository,
    private val staffRepository: StaffRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val personAddressRepository: PersonAddressRepository
) : AuditableService(auditedInteractionService) {

    private val generateCrn = SimpleJdbcCall(jdbcTemplate)
        .withCatalogName("offender_support_api")
        .withFunctionName("getNextCRN")

    @Transactional
    fun insertPerson(defendant: Defendant, courtCode: String): Person =
        audit(BusinessInteractionCode.INSERT_PERSON) { audit ->
            // Person record
            val savedPerson = personRepository.save(defendant.toPerson())

            val courtLinkedProvider = courtRepository.getByOuCode(courtCode).provider
            val initialAllocation = referenceDataRepository.initialAllocationReason()
            val unallocatedTeam = teamRepository.findByCode(courtLinkedProvider.code + "UAT")
            val unallocatedStaff = staffRepository.findByCode(unallocatedTeam.code + "U")

            notifier.caseCreated(savedPerson)

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
            personManagerRepository.save(manager)

            // Equality record
            val equality = Equality(
                id = null,
                personId = savedPerson.id!!,
                softDeleted = false,
            )

            equalityRepository.save(equality)

            val address = defendant.personDefendant?.personDetails?.address
            if (address.containsInformation()) {
                insertAddress(
                    PersonAddress(
                        id = null,
                        start = LocalDate.now(),
                        status = referenceDataRepository.mainAddressStatus(),
                        personId = savedPerson.id,
                        notes = address?.buildNotes(),
                        postcode = address?.postcode,
                        type = referenceDataRepository.awaitingAssessmentAddressType()
                    )
                )
            }
            audit["offenderId"] = savedPerson.id
            savedPerson
        }

    @Transactional
    fun insertAddress(address: PersonAddress): PersonAddress = audit(BusinessInteractionCode.INSERT_ADDRESS) { audit ->
        val savedAddress = personAddressRepository.save(address)
        audit["addressId"] = address.id!!
        savedAddress
    }

    fun generateCrn(): String {
        return generateCrn.executeFunction(String::class.java)
    }

    fun String.toDeliusGender() = ReferenceData.GenderCode.entries.find { it.commonPlatformValue == this }?.deliusValue
        ?: throw IllegalStateException("Gender not found: $this")

    fun Defendant.toPerson(): Person {
        val personDetails = personDefendant?.personDetails ?: throw IllegalArgumentException("No person found")
        val genderCode = personDetails.gender.toDeliusGender()

        return Person(
            id = null,
            crn = generateCrn(),
            croNumber = this.croNumber,
            pncNumber = this.pncId,
            forename = personDetails.firstName,
            secondName = personDetails.middleName,
            telephoneNumber = personDetails.contact?.home,
            mobileNumber = personDetails.contact?.mobile,
            surname = personDetails.lastName,
            dateOfBirth = personDetails.dateOfBirth,
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

    fun Address.buildNotes(): String {
        return listOf(
            "Address record automatically created by common-platform-delius-service with the following information:",
            "Address1: ${this.address1 ?: "N/A"}",
            "Address2: ${this.address2 ?: "N/A"}",
            "Address3: ${this.address3 ?: "N/A"}",
            "Address4: ${this.address4 ?: "N/A"}",
            "Postcode: ${this.postcode ?: "N/A"}"
        ).joinToString("\n")
    }
}