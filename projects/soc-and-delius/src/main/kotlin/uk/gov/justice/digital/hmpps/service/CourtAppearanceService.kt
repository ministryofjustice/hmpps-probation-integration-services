package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.controller.IdentifierType
import uk.gov.justice.digital.hmpps.entity.CourtAppearanceRepository
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
        courtAppearances.map {
            courtAppearanceModels.add(
                CourtAppearance(
                    it.appearanceDate,
                    Type(it.appearanceType.code, it.appearanceType.description),
                    it.court.code,
                    it.court.name,
                    it.courtAppearanceEventEntity.courtAppearancePerson.crn,
                    it.id,
                    it.courtAppearanceEventEntity.courtAppearancePerson.id
                )
            )
        }
        return CourtAppearancesContainer(courtAppearanceModels)
    }
}
