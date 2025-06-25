package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.appointment.*
import uk.gov.justice.digital.hmpps.api.model.sentence.MinimalOrder
import uk.gov.justice.digital.hmpps.api.model.sentence.MinimalRequirement
import uk.gov.justice.digital.hmpps.api.model.sentence.MinimalSentence
import uk.gov.justice.digital.hmpps.api.model.sentence.ProviderOfficeLocation
import uk.gov.justice.digital.hmpps.api.model.user.Team
import uk.gov.justice.digital.hmpps.api.model.user.TeamResponse
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.compliance.Nsi
import uk.gov.justice.digital.hmpps.integrations.delius.compliance.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.LicenceConditionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.LocationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.TeamRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactType as ContactTypeEntity
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.Team as TeamEntity

@Service
class AppointmentService(
    private val personRepository: PersonRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val sentenceService: SentenceService,
    private val requirementService: RequirementService,
    private val licenceConditionRepository: LicenceConditionRepository,
    private val requirementRepository: RequirementRepository,
    private val teamRepository: TeamRepository,
    private val nsiRepository: NsiRepository,
    private val locationRepository: LocationRepository
) {

    fun getProbationRecordsByContactType(crn: String, code: String): ContactTypeAssociation {
        val person = personRepository.getPerson(crn)

        if (!CreateAppointment.Type.entries.any { it.code == code }) {
            throw NotFoundException("CreateAppointment", "code", code)
        }

        val contactType = contactTypeRepository.getContactType(code)
        val activeEvents = sentenceService.getActiveSentences(person.id)
        val (eventLevelNsis, personLevelNsis) = nsiRepository.findByPersonIdAndActiveIsTrue(person.id)
            .partition { it.eventId != null }

        return ContactTypeAssociation(
            personSummary = person.toSummary(),
            contactTypeCode = code,
            associatedWithPerson = contactType.offenderContact,
            personNsis = personLevelNsis.map {
                it.toMinimalNsi()
            },
            sentences = activeEvents.map { it.toMinimalSentence(eventLevelNsis) }
        )
    }

    fun getAppointmentTypes(): AppointmentTypeResponse =
        AppointmentTypeResponse(
            contactTypeRepository
                .findByCodeIn(
                    CreateAppointment.Type.entries.map { it.code },
                    CreateAppointment.Type.entries.associate { it.code to it.ordinal }
                        .toList().joinToString { "'${it.first}', ${it.second}" }
                ).map { it.toAppointmentType() }
        )

    fun getTeamsByProvider(code: String) =
        TeamResponse(
            teamRepository
                .findByProviderCode(code).map { it.toTeam() }
        )

    fun getOfficeByProviderAndTeam(provideCode: String, teamCode: String): ProviderOfficeLocation =
        ProviderOfficeLocation(
            locationRepository.findByProviderAndTeam(provideCode, teamCode)
                .map { it.toLocationDetails() }
        )

    fun Event.toMinimalSentence(eventLevelNsis: List<Nsi>): MinimalSentence {
        val filteredNsiList = eventLevelNsis.filter { nsi -> nsi.eventId == id }
        return MinimalSentence(
            id,
            eventNumber,
            disposal?.toMinimalOrder() ?: MinimalOrder("Pre-Sentence"),
            filteredNsiList.map { it.toMinimalNsi() },
            licenceConditions = disposal?.let {
                licenceConditionRepository.findAllByDisposalId(disposal.id).map {
                    it.toMinimalLicenceCondition()
                }
            } ?: emptyList(),
            requirements = requirementRepository.getRequirements(id, eventNumber).filter { it.active }
                .map { it.toMinimalRequirement() },
        )
    }

    fun Requirement.toMinimalRequirement(): MinimalRequirement {
        val rar = requirementService.getRar(disposal!!.id, mainCategory!!.code)
        return MinimalRequirement(
            id,
            populateRequirementDescription(mainCategory.description, subCategory?.description, length, rar)
        )
    }

    fun Nsi.toMinimalNsi() = MinimalNsi(id, type.description + (subType?.let { " (${it.description})" } ?: ""))
}

fun ContactTypeEntity.toAppointmentType() = AppointmentType(code, description, offenderContact, locationRequired == "Y")

fun TeamEntity.toTeam() = Team(code = code, description = description)