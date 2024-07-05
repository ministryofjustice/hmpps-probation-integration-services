package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.controller.IdentifierType
import uk.gov.justice.digital.hmpps.entity.CourtAppearanceEntity
import uk.gov.justice.digital.hmpps.entity.CourtAppearanceRepository
import uk.gov.justice.digital.hmpps.model.AllCourtAppearancesContainer
import uk.gov.justice.digital.hmpps.model.CourtAppearance
import uk.gov.justice.digital.hmpps.model.CourtAppearancesContainer
import uk.gov.justice.digital.hmpps.model.Type
import java.time.LocalDate

@Service
class CourtAppearanceService(private val courtAppearanceRepository: CourtAppearanceRepository) {
    fun getCourtAppearances(value: String, type: IdentifierType, requestDate: LocalDate?): CourtAppearancesContainer {
        val courtAppearanceModels = mutableListOf<CourtAppearance>()
        var fromDate = LocalDate.now()
        requestDate?.also { fromDate = it }
        val courtAppearances = when (type) {
            IdentifierType.CRN -> courtAppearanceRepository.findMostRecentCourtAppearancesByCrn(fromDate, value)
            IdentifierType.NOMS -> courtAppearanceRepository.findMostRecentCourtAppearancesByNomsNumber(fromDate, value)
        }
        courtAppearances.forEach { courtAppearanceModels.add(it.toModel()) }
        return CourtAppearancesContainer(courtAppearanceModels)
    }

    @Transactional
    fun getAllCourtAppearances(crns: List<String>) = courtAppearanceRepository.findCourtAppearancesForCrns(crns)
        .map { it.toModel() }
        .groupBy { it.crn }
        .let { AllCourtAppearancesContainer(it) }

    private fun CourtAppearanceEntity.toModel() = CourtAppearance(
        appearanceDate = appearanceDate,
        type = Type(appearanceType.code, appearanceType.description),
        courtCode = court.code,
        courtName = court.name,
        crn = courtAppearanceEventEntity.courtAppearancePerson.crn,
        courtAppearanceId = id,
        offenderId = courtAppearanceEventEntity.courtAppearancePerson.id
    )
}
