package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.ProbationAreaDistrict
import uk.gov.justice.digital.hmpps.entity.ProbationAreaRepository
import uk.gov.justice.digital.hmpps.model.LocalDeliveryUnit
import uk.gov.justice.digital.hmpps.model.ProbationArea
import uk.gov.justice.digital.hmpps.model.ProbationAreaContainer

@Service
class ProbationAreaService(private val probationAreaRepository: ProbationAreaRepository) {
    fun getProbationAreas(): ProbationAreaContainer {
        val pas = mutableListOf<ProbationArea>()
        val pad = probationAreaRepository.probationAreaDistricts()

        //create a set of probation areas
        val paCodes: Set<Pair<String, String>> = pad.map { Pair(it.pCode, it.pDesc) }.toSet()

        //get the districts for the paCode
        paCodes.forEach {
            pas.add(ProbationArea(it.first, it.second, function(it.first, pad)))
        }
        return ProbationAreaContainer(pas)
    }

    private fun function(pCode: String, pad: List<ProbationAreaDistrict>): List<LocalDeliveryUnit> {
        return pad.filter { it.pCode == pCode }.map { LocalDeliveryUnit(it.dCode, it.dDesc) }
    }
}