package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.datetime.DeliusDateFormatter
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.oasys.AssessmentSummary
import uk.gov.justice.digital.hmpps.message.AdditionalInformation
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.PersonIdentifier
import uk.gov.justice.digital.hmpps.message.PersonReference
import java.time.LocalDate
import java.time.ZonedDateTime

@Service
class RiskService(
    private val registrationRepository: RegistrationRepository,
    private val registerTypeRepository: RegisterTypeRepository,
    private val contactService: ContactService
) {
    fun recordRisk(person: Person, summary: AssessmentSummary): List<HmppsDomainEvent> {
        val highestRisk = summary.riskFlags.mapNotNull(Risk::of).sortedByDescending(Risk::ordinal).firstOrNull()

        val registrations = registrationRepository.findByPersonIdAndTypeFlagCode(
            person.id,
            ReferenceData.Code.OASYS_RISK_FLAG.value
        ).toMutableList()

        val deRegEvents = registrations.filter { Risk.from(it.type) != highestRisk }.map {
            val contact: Contact = contactService.createContact(
                ContactDetail(ContactType.Code.DEREGISTRATION, notes = it.notes()),
                person
            )
            it.deregister(contact)
            it.deRegEvent(person.crn)
        }

        val regEvent: HmppsDomainEvent? = highestRisk?.let { risk ->
            val matchingReg = registrations.firstOrNull { risk == Risk.from(it.type) }
            if (matchingReg == null) createRegistration(person, registrations, risk) else null
        }

        person.highestRiskColour = registrations.firstOrNull { !it.deregistered }?.type?.colour

        return regEvent?.let { deRegEvents + it } ?: deRegEvents
    }

    private fun createRegistration(person: Person, registrations: MutableList<Registration>, risk: Risk): HmppsDomainEvent {
        val type = registerTypeRepository.getByCode(risk.code)
        val nextReviewDate = type.reviewPeriod?.let { LocalDate.now().plusMonths(it) }
        val notes = listOfNotNull(
            "Type: ${type.flag.description} - ${type.description}",
            nextReviewDate?.let { "Next Review Date: ${DeliusDateFormatter.format(it)}" }
        ).joinToString(System.lineSeparator())

        val contact: Contact = contactService.createContact(
            ContactDetail(
                ContactType.Code.REGISTRATION,
                notes = notes,
                contactType = type.registrationContactType
            ),
            person
        )
        val reviewContact: Contact = contactService.createContact(
            ContactDetail(
                ContactType.Code.REGISTRATION_REVIEW,
                notes = notes,
                contactType = type.reviewContactType
            ),
            person
        )
        val registration = registrationRepository.save(
            Registration(
                person.id,
                LocalDate.now(),
                contact,
                contact.teamId,
                contact.staffId,
                type,
                nextReviewDate
            ).withReview(reviewContact)
        )
        registrations += registration
        return registration.regEvent(person.crn)
    }
}

enum class Risk(val code: String) {
    L("RLRH"), M("RMRH"), H("RHRH"), V("RVHR");

    companion object {
        fun of(value: String): Risk? = entries.firstOrNull { it.name.equals(value, true) }
        fun from(type: RegisterType): Risk = entries.firstOrNull { it.code.equals(type.code, true) }
            ?: throw IllegalArgumentException("Unrecognised Register Type Code: ${type.code}")
    }
}

fun Registration.notes(): String = listOfNotNull(
    "Type: ${type.flag.description} - ${type.description}",
    nextReviewDate?.let { "Next Review Date: ${DeliusDateFormatter.format(it)}" }
).joinToString(System.lineSeparator())

fun Registration.deRegEvent(crn: String): HmppsDomainEvent = HmppsDomainEvent(
    eventType = ReferenceData.Code.REGISTRATION_DEREGISTERED.value,
    version = 1,
    occurredAt = ZonedDateTime.now(),
    personReference = forCrn(crn),
    nullableAdditionalInformation = AdditionalInformation(
        listOfNotNull(
            "registerTypeCode" to type.code,
            "registerTypeDescription" to type.description,
            "deregistrationId" to deregistration!!.id,
            "deregistrationDate" to deregistration!!.date,
            "createdDateAndTime" to deregistration!!.createdDatetime
        ).toMap().toMutableMap()
    )
)

fun Registration.regEvent(crn: String): HmppsDomainEvent = HmppsDomainEvent(
    eventType = ReferenceData.Code.REGISTRATION_ADDED.value,
    version = 1,
    personReference = forCrn(crn),
    nullableAdditionalInformation = AdditionalInformation(
        listOfNotNull(
            "registrationId" to id,
            "registerTypeCode" to type.code,
            "registerTypeDescription" to type.description,
            "registrationDate" to date,
            "createdDateAndTime" to createdDatetime
        ).toMap().toMutableMap()
    )
)

fun forCrn(crn: String) = PersonReference(listOf(PersonIdentifier("CRN", crn)))
