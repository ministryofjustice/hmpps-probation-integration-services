package uk.gov.justice.digital.hmpps.service

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.risk.*
import uk.gov.justice.digital.hmpps.integrations.delius.compliance.Nsi
import uk.gov.justice.digital.hmpps.integrations.delius.compliance.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.compliance.NsiType
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.getSummary
import uk.gov.justice.digital.hmpps.integrations.delius.risk.DeRegistration
import uk.gov.justice.digital.hmpps.integrations.delius.risk.RiskFlagRepository
import uk.gov.justice.digital.hmpps.integrations.delius.risk.getRiskFlag

@Service
class RiskService(
    private val personRepository: PersonRepository,
    private val riskFlagRepository: RiskFlagRepository,
    private val nsiRepository: NsiRepository
) {

    @Transactional
    fun getPersonRiskFlag(
        crn: String,
        riskFlagId: Long,
        noteId: Int? = null,
        riskRemovalNoteId: Int? = null
    ): PersonRiskFlag {
        val summary = personRepository.getSummary(crn)
        val riskFlag = riskFlagRepository.getRiskFlag(summary.id, riskFlagId)
        return PersonRiskFlag(
            personSummary = summary.toPersonSummary(),
            riskFlag = riskFlag.toRiskFlag(noteId, riskRemovalNoteId)
        )
    }

    @Transactional
    fun getPersonRiskFlags(crn: String): PersonRiskFlags {
        val summary = personRepository.getSummary(crn)
        val riskFlags = riskFlagRepository.findByPersonId(summary.id)
        val opd = nsiRepository.findByPersonIdAndTypeCode(summary.id, NsiType.Code.OPD_COMMUNITY_PATHWAY.value)
            .firstOrNull(Nsi::active)
        val mappa = riskFlagRepository.findActiveMappaRegistrationByOffenderId(summary.id, PageRequest.of(0, 1))
            .firstOrNull()

        return PersonRiskFlags(
            personSummary = summary.toPersonSummary(),
            mappa = mappa?.toMappa(),
            opd = opd?.let { Opd(eligible = true, date = opd.lastUpdated) },
            riskFlags = riskFlags
                .filter { !it.deRegistered }
                .map { it.toRiskFlag() }
                .sortedWith(compareByDescending<RiskFlag> { it.level?.severity }.thenBy { it.description }
                    .thenBy { it.createdDate }),
            removedRiskFlags = riskFlags.filter { it.deRegistered }.map { it.toRiskFlag() }
        )
    }
}

enum class MappaLevel(val communityValue: Int, val deliusValue: String) {
    NOMINAL(0, "M0"),
    ONE(1, "M1"),
    TWO(2, "M2"),
    THREE(3, "M3");

    companion object {
        fun toCommunityLevel(deliusLevel: String): Int {
            return entries.firstOrNull { level: MappaLevel -> level.deliusValue == deliusLevel }?.communityValue
                ?: NOMINAL.communityValue
        }
    }
}

fun uk.gov.justice.digital.hmpps.integrations.delius.risk.RiskFlag.toMappa() = MappaDetail(
    level = level?.code?.let { code -> MappaLevel.toCommunityLevel(code) },
    levelDescription = level?.description,
    category = category?.code?.let { code -> MappaCategory.toCommunityCategory(code) },
    categoryDescription = category?.description,
    startDate = date,
    reviewDate = nextReviewDate,
    lastUpdated = lastUpdated.toLocalDate()
)

fun uk.gov.justice.digital.hmpps.integrations.delius.risk.RiskFlag.toRiskFlag(
    noteId: Int? = null,
    riskRemovalNoteId: Int? = null
) = RiskFlag(
    id = id,
    description = type.description,
    level = RiskLevel.fromString(type.colour),
    levelCode = level?.code,
    levelDescription = level?.description,
    riskNotes = if (noteId == null) formatNote(notes, true) else null,
    riskNote = if (noteId != null) formatNote(notes, false).elementAtOrNull(noteId) else null,
    createdDate = createdDate,
    createdBy = Name(forename = createdBy.forename, surname = createdBy.surname),
    nextReviewDate = nextReviewDate,
    mostRecentReviewDate = reviews.filter { it.completed == true }.maxByOrNull { it.date }?.date,
    removed = deRegistered,
    removalHistory = deRegistrations.sortedByDescending { it.deRegistrationDate }
        .map { it.toRiskFlagRemoval(riskRemovalNoteId) }
)

fun DeRegistration.toRiskFlagRemoval(riskRemovalNoteId: Int? = null) = RiskFlagRemoval(
    riskRemovalNotes = if (riskRemovalNoteId == null) formatNote(notes, true) else null,
    riskRemovalNote = if (riskRemovalNoteId != null) formatNote(
        notes,
        false
    ).elementAtOrNull(riskRemovalNoteId) else null,
    removedBy = Name(forename = staff.forename, surname = staff.surname),
    removalDate = deRegistrationDate
)

internal enum class MappaCategory(val communityValue: Int, val deliusValue: String) {
    NOMINAL(0, "X9"),
    ONE(1, "M1"),
    TWO(2, "M2"),
    THREE(3, "M3"),
    FOUR(4, "M4");

    companion object {
        fun toCommunityCategory(deliusCategory: String): Int {
            return entries.firstOrNull { category: MappaCategory -> category.deliusValue == deliusCategory }?.communityValue
                ?: NOMINAL.communityValue
        }
    }
}
