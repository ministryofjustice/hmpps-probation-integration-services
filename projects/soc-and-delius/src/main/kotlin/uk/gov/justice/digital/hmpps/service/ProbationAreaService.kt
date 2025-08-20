package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.ManagerHistory
import uk.gov.justice.digital.hmpps.entity.ManagerHistoryRepository
import uk.gov.justice.digital.hmpps.entity.ProbationAreaRepository
import uk.gov.justice.digital.hmpps.model.*

@Service
class ProbationAreaService(
    private val probationAreaRepository: ProbationAreaRepository,
    private val managerHistoryRepository: ManagerHistoryRepository,
) {
    fun getProbationAreas(includeNonSelectable: Boolean): ProbationAreaContainer {
        val pad = when (includeNonSelectable) {
            true -> probationAreaRepository.probationAreaDistrictsNonSelectable()
            false -> probationAreaRepository.probationAreaDistricts()
        }

        return ProbationAreaContainer(
            pad.groupBy { Pair(it.pCode, it.pDesc) }
                .map { pa ->
                    ProbationArea(
                        pa.key.first,
                        pa.key.second,
                        pa.value.map { LocalDeliveryUnit(it.dCode, it.dDesc) }
                    )
                }
        )
    }

    fun getProbationAreaHistory(crns: List<String>): Map<String, List<ProbationAreaHistory>> =
        managerHistoryRepository.findByPersonCrnInOrderByPersonCrn(crns)
            .groupBy { it.person.crn }
            .mapValues { (_, v) -> v.mergeOverlapping().sortedBy { it.startDate } }
            .toMap()

    private fun List<ManagerHistory>.mergeOverlapping() = groupBy { it.team.district.borough.probationArea.code }.values
        .flatMap { rows ->
            rows.sortedBy { it.allocationDate }
                .fold(emptyList<ProbationAreaHistory>()) { merged, next ->
                    merged.lastOrNull()?.let { last ->
                        when {
                            last.endDate == null -> merged
                            next.allocationDate <= last.endDate -> merged.dropLast(1) + last.copy(endDate = next.endDate?.takeIf { it > last.endDate })
                            else -> null
                        }
                    } ?: (merged + next.toProbationAreaHistory())
                }
        }

    private fun ManagerHistory.toProbationAreaHistory() = ProbationAreaHistory(allocationDate, endDate, lau())
    private fun ManagerHistory.lau() = with(team.district) { HistoryLau(code, description, pdu()) }
    private fun ManagerHistory.pdu() = with(team.district.borough) { HistoryPdu(code, description, probationArea()) }
    private fun ManagerHistory.probationArea() =
        with(team.district.borough.probationArea) { CodeDescription(code, description) }
}
