package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.integration.delius.EventRepository
import uk.gov.justice.digital.hmpps.integration.delius.entity.*
import uk.gov.justice.digital.hmpps.integration.delius.getEvent
import uk.gov.justice.digital.hmpps.ldap.findByUsername
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.model.Court
import uk.gov.justice.digital.hmpps.model.CourtAppearance
import uk.gov.justice.digital.hmpps.model.Offence
import uk.gov.justice.digital.hmpps.model.Provider
import uk.gov.justice.digital.hmpps.model.Team

@Service
class CaseDetailsService(
    private val comRepository: PersonManagerRepository,
    private val responsibleOfficerRepository: ResponsibleOfficerRepository,
    private val personAddressRepository: PersonAddressRepository,
    private val registrationRepository: RegistrationRepository,
    private val eventRepository: EventRepository,
    private val courtAppearanceRepository: CourtAppearanceRepository,
    private val keyDateRepository: KeyDateRepository,
    private val personRepository: PersonRepository,
    private val ogrsAssessmentRepository: OgrsAssessmentRepository,
    private val ldapTemplate: LdapTemplate,
    private val limitedAccess: LimitedAccessService
) {
    fun getAddresses(crn: String): AddressWrapper {
        val addresses = personAddressRepository.findAllByPersonCrn(crn)
        return AddressWrapper(ContactDetailAddresses(addresses.map { it.asCaseAddress() }))
    }

    fun getSupervisions(crn: String): SupervisionResponse = with(comRepository.getForCrn(crn)) {
        return SupervisionResponse(
            communityManager = toCommunityManagerResponse(),
            mappaDetail = registrationRepository.findMappa(person.id).toMappaResponse(),
            supervisions = eventRepository.findByPersonIdOrderByConvictionDateDesc(person.id).toSupervisionResponse(),
            dynamicRisks = registrationRepository.findDynamicRiskRegistrations(person.id)
                .toDynamicRiskRegistrationResponse(),
            personStatus = registrationRepository.findPersonStatusRegistrations(person.id)
                .toPersonStatusRegistrationResponse(),
        )
    }

    fun getLimitedAccessDetail(crn: String): LimitedAccessDetail = personRepository.getByCrn(crn).limitedAccessDetail()

    @Transactional(readOnly = true)
    fun getCaseDetails(crn: String, eventNumber: Int): CaseDetails {
        val responsibleOfficer = responsibleOfficerRepository.findByPersonCrn(crn)
        val com = responsibleOfficer?.communityManager ?: comRepository.getForCrn(crn)
        val person = responsibleOfficer?.person ?: com.person
        val provider = responsibleOfficer?.provider() ?: com.provider
        val event = eventRepository.getEvent(person.crn, eventNumber.toString())
        val appearance = courtAppearanceRepository.findByEventIdOrderByDateDesc(event.id)
        val erd = event.disposal?.custody?.let { keyDateRepository.getExpectedReleaseDate(it.id) }
        val ogrsScore = ogrsAssessmentRepository.findFirstByEventIdOrderByAssessmentDateDesc(event.id)?.score
        return CaseDetails(
            person.nomsId,
            person.name(),
            person.dateOfBirth,
            person.gender.description,
            appearance?.let { Appearance(it.date.toLocalDate(), Court(it.court.name)) },
            erd?.let { SentenceSummary(it.date) },
            ResponsibleProvider(provider.code, provider.description),
            ogrsScore,
            person.dynamicRsrScore,
            if (person.currentExclusion == true || person.currentRestriction == true) person.limitedAccessDetail() else null
        )
    }

    fun getCrnForNomsId(nomsId: String) = PersonIdentifier(personRepository.getCrn(nomsId), nomsId)

    fun checkIfPersonExists(crn: String) = PersonExists(crn, personRepository.existsByCrn(crn))

    private fun PersonManager.toCommunityManagerResponse(): Manager {
        staff.user?.apply {
            ldapTemplate.findByUsername<LdapUser>(username)?.let {
                email = it.email
                telephone = it.telephone
            }
        }
        return Manager(
            code = staff.code,
            name = staff.name(),
            username = staff.user?.username,
            email = staff.user?.email,
            telephoneNumber = staff.user?.telephone,
            team = Team(
                code = team.code,
                description = team.description,
                email = team.emailAddress,
                telephoneNumber = team.telephone,
                provider = Provider(provider.code, provider.description)
            )
        )
    }

    private fun RegistrationEntity?.toMappaResponse() = this?.let {
        MappaDetail(
            level = it.level?.code?.toMappaLevel(),
            levelDescription = it.level?.description,
            category = it.category?.code?.toMappaCategory(),
            categoryDescription = it.category?.description,
            startDate = it.date,
            reviewDate = it.reviewDate,
            notes = it.notes
        )
    }

    private fun List<RegistrationEntity>.toDynamicRiskRegistrationResponse() = this.map {
        DynamicRiskRegistration(
            code = it.type.code,
            description = it.type.description,
            startDate = it.date,
            reviewDate = it.reviewDate,
            notes = it.notes
        )
    }

    private fun List<RegistrationEntity>.toPersonStatusRegistrationResponse() = this.map {
        PersonStatusRegistration(
            code = it.type.code,
            description = it.type.description,
            startDate = it.date,
            reviewDate = it.reviewDate,
            notes = it.notes
        )
    }

    private fun List<Event>.toSupervisionResponse() = this.map { event ->
        Supervision(
            number = event.number.toInt(),
            active = event.active,
            date = event.convictionDate,
            sentence = event.disposal?.let { disposal ->
                Sentence(
                    description = disposal.type.description,
                    date = disposal.date,
                    length = disposal.length?.toInt(),
                    lengthUnits = disposal.lengthUnits?.let { LengthUnit.entries.firstOrNull { lu -> lu.name == it.description } },
                    custodial = disposal.type.isCustodial()
                )
            },
            mainOffence = event.mainOffence.let { Offence.of(it.date, it.count, it.offence) },
            additionalOffences = event.additionalOffences.map { Offence.of(it.date, it.count, it.offence) },
            courtAppearances = event.courtAppearances.map {
                CourtAppearance(
                    type = it.type.description,
                    date = it.date,
                    court = it.court.name,
                    plea = it.plea?.description
                )
            }
        )
    }

    private fun Person.limitedAccessDetail(): LimitedAccessDetail = limitedAccess.getLimitedAccessDetails(this)
}

private fun PersonAddress.asCaseAddress() = CaseAddress(
    noFixedAbode == true,
    CodedValue(status.code, status.description),
    buildingName,
    addressNumber,
    streetName,
    town,
    district,
    county,
    postcode,
    startDate,
    endDate,
    notes,
)