package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.*
import uk.gov.justice.digital.hmpps.api.model.RecommendationModel.Institution
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.*
import java.time.LocalDate

@Service
class CaseSummaryService(
    private val personRepository: CaseSummaryPersonRepository,
    private val addressRepository: CaseSummaryAddressRepository,
    private val personManagerRepository: CaseSummaryPersonManagerRepository,
    private val registrationRepository: CaseSummaryRegistrationRepository,
    private val releaseRepository: CaseSummaryReleaseRepository,
    private val eventRepository: CaseSummaryEventRepository,
    private val contactRepository: CaseSummaryContactRepository
) {
    fun getPersonalDetailsOverview(person: Person) = PersonalDetailsOverview(
        name = person.name(),
        identifiers = person.identifiers(),
        dateOfBirth = person.dateOfBirth,
        gender = person.gender.description,
        ethnicity = person.ethnicity?.description,
        primaryLanguage = person.primaryLanguage?.description
    )

    fun getPersonalDetails(crn: String): PersonalDetails {
        val person = personRepository.getPerson(crn)
        val personalDetails = getPersonalDetailsOverview(person)
        val mainAddress = addressRepository.findMainAddress(person.id)
        val personManager = personManagerRepository.findByPersonId(person.id)
        return PersonalDetails(
            personalDetails = personalDetails,
            mainAddress = mainAddress?.toAddress(),
            communityManager = personManager?.toManager()
        )
    }

    fun getOverview(crn: String): Overview {
        val person = personRepository.getPerson(crn)
        val personalDetails = getPersonalDetailsOverview(person)
        val registerFlags = registrationRepository.findActiveTypeDescriptionsByPersonId(person.id)
        val events = eventRepository.findByPersonId(person.id)
        val lastRelease = events.lastRelease()
        return Overview(
            personalDetails = personalDetails,
            registerFlags = registerFlags,
            lastRelease = lastRelease?.dates(),
            activeConvictions = events.map { it.toConviction() }
        )
    }

    fun getMappaAndRoshHistory(crn: String): MappaAndRoshHistory {
        val person = personRepository.getPerson(crn)
        val personalDetails = getPersonalDetailsOverview(person)
        val mappa = registrationRepository.findMappa(person.id)?.toMappa()
        val roshHistory = registrationRepository.findRoshHistory(person.id).map { it.toRosh() }
        return MappaAndRoshHistory(
            personalDetails = personalDetails,
            mappa = mappa,
            roshHistory = roshHistory
        )
    }

    fun getLicenceConditions(crn: String): LicenceConditions {
        val person = personRepository.getPerson(crn)
        val personalDetails = getPersonalDetailsOverview(person)
        val events = eventRepository.findByPersonId(person.id)
        return LicenceConditions(
            personalDetails = personalDetails,
            activeConvictions = events.map { it.toConvictionWithLicenceConditions() }
        )
    }

    fun getContactHistory(
        crn: String,
        query: String? = null,
        from: LocalDate? = null,
        to: LocalDate = LocalDate.now(),
        types: List<String> = emptyList(),
        includeSystemGenerated: Boolean = true
    ): ContactHistory {
        val person = personRepository.getPerson(crn)
        val personalDetails = getPersonalDetailsOverview(person)
        val contacts = contactRepository.searchContacts(person.id, query, from, to, types, includeSystemGenerated)
        val typeSummary = contactRepository.summarizeContactTypes(person.id)
        return ContactHistory(
            personalDetails = personalDetails,
            contacts = contacts.map { it.toContact() },
            summary = ContactHistory.ContactSummary(
                types = typeSummary,
                hits = contacts.size
            )
        )
    }

    fun getRecommendationModel(crn: String): RecommendationModel {
        val person = personRepository.getPerson(crn)
        val personalDetails = getPersonalDetailsOverview(person)
        val mainAddress = addressRepository.findMainAddress(person.id)
        val mappa = registrationRepository.findMappa(person.id)?.toMappa()
        val events = eventRepository.findByPersonId(person.id)
        val lastRelease = events.lastRelease()
        return RecommendationModel(
            personalDetails = personalDetails,
            mainAddress = mainAddress?.toAddress(),
            lastRelease = lastRelease?.dates(),
            lastReleasedFromInstitution = lastRelease?.institution?.let { Institution(it.name, it.description) },
            mappa = mappa,
            activeConvictions = events.map { it.toConviction() },
            activeCustodialConvictions = events.custodial().map { it.toConvictionDetails() }
        )
    }

    private fun List<Event>.lastRelease() = mapNotNull { it.disposal?.custody?.id }
        .let { releaseRepository.findFirstByCustodyIdInOrderByDateDesc(it) }

    private fun List<Event>.custodial() = filter { it.disposal?.custody != null }
}
