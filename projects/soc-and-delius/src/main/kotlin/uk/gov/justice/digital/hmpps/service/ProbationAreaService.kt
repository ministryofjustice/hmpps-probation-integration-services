package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
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
            .mapValues { (_, v) ->
                v.map {
                    ProbationAreaHistory(
                        it.allocationDate,
                        it.endDate,
                        CodeDescription(it.probationArea.code, it.probationArea.description)
                    )
                }.sortedBy { it.startDate }
            }
            .toMap()
}
