package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.ProbationAreaRepository
import uk.gov.justice.digital.hmpps.model.LocalDeliveryUnit
import uk.gov.justice.digital.hmpps.model.ProbationArea
import uk.gov.justice.digital.hmpps.model.ProbationAreaContainer

@Service
class ProbationAreaService(private val probationAreaRepository: ProbationAreaRepository) {
    fun getProbationAreas(includeNonSelectable: Boolean): ProbationAreaContainer {
        val pad =
            when (includeNonSelectable) {
                true -> probationAreaRepository.probationAreaDistrictsNonSelectable()
                false -> probationAreaRepository.probationAreaDistricts()
            }

        return ProbationAreaContainer(
            pad.groupBy { Pair(it.pCode, it.pDesc) }
                .map { pa ->
                    ProbationArea(
                        pa.key.first,
                        pa.key.second,
                        pa.value.map { LocalDeliveryUnit(it.dCode, it.dDesc) },
                    )
                },
        )
    }
}
