package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.appointment.*
import uk.gov.justice.digital.hmpps.api.model.sentence.MinimalOrder
import uk.gov.justice.digital.hmpps.api.model.sentence.MinimalSentence
import uk.gov.justice.digital.hmpps.api.model.sentence.ProviderOfficeLocation
import uk.gov.justice.digital.hmpps.api.model.user.Team
import uk.gov.justice.digital.hmpps.api.model.user.TeamResponse
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.AppointmentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.compliance.Nsi
import uk.gov.justice.digital.hmpps.integrations.delius.compliance.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.LicenceConditionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.LocationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.team.TeamRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactType as ContactTypeEntity
import uk.gov.justice.digital.hmpps.integrations.delius.user.team.entity.Team as TeamEntity

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
    private val locationRepository: LocationRepository,
    private val appointmentRepository: AppointmentRepository,
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
                    CreateAppointment.Type.entries.map { it.code }
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
            requirements = requirementRepository.getRequirements(id, eventNumber).filter { it.active }.asMinimals {
                requirementService.getRar(it.disposal!!.id, it.mainCategory!!.code)
            }
        )
    }

    fun Nsi.toMinimalNsi() = MinimalNsi(id, type.description + (subType?.let { " (${it.description})" } ?: ""))

    fun findOverdueOutcomes(crn: String): OverdueOutcomeAppointments =
        appointmentRepository.findOverdueOutcomes(crn).map {
            OverdueOutcome(
                it.id,
                it.externalReference,
                OverdueOutcome.Type(it.type.code, it.type.description),
                it.date,
                it.startTime?.toLocalTime(),
                it.endTime?.toLocalTime()
            )
        }.let(::OverdueOutcomeAppointments)
}

fun ContactTypeEntity.toAppointmentType() = AppointmentType(code, description, offenderContact, locationRequired == "Y")

fun TeamEntity.toTeam() = Team(code = code, description = description)