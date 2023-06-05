package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.controller.IdentifierType
import uk.gov.justice.digital.hmpps.entity.CourtAppearanceRepository
import uk.gov.justice.digital.hmpps.model.CourtAppearance
import uk.gov.justice.digital.hmpps.model.CourtAppearancesContainer
import uk.gov.justice.digital.hmpps.model.Type
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Service
class CourtAppearanceService(private val courtAppearanceRepository: CourtAppearanceRepository) {
    fun getCourtAppearances(value: String, type: IdentifierType): CourtAppearancesContainer {
        val courtAppearanceModels = mutableListOf<CourtAppearance>()

        val courtAppearances = when (type) {
            IdentifierType.CRN -> courtAppearanceRepository.findMostRecentCourtAppearancesByCrn(ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS), value)
            IdentifierType.NOMS -> courtAppearanceRepository.findMostRecentCourtAppearancesByNomsNumber(ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS), value)
        }
        courtAppearances.map {
            courtAppearanceModels.add(
                CourtAppearance(
                    it.appearanceDate.toLocalDate(),
                    Type(it.appearanceType.code, it.appearanceType.description),
                    it.court.code,
                    it.court.name,
                    it.courtAppearanceEventEntity.courtAppearancePerson.crn
                )
            )
        }
        return CourtAppearancesContainer(courtAppearanceModels)
    }
}
