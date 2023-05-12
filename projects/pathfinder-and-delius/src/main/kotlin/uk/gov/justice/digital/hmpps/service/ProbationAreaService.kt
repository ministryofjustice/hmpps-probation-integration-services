package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.District
import uk.gov.justice.digital.hmpps.entity.ProbationAreaEntity
import uk.gov.justice.digital.hmpps.entity.ProbationAreaRepository
import uk.gov.justice.digital.hmpps.model.LocalDeliveryUnit
import uk.gov.justice.digital.hmpps.model.ProbationArea

@Service
class ProbationAreaService(private val probationAreaRepository: ProbationAreaRepository) {
    fun getProbationAreas(): List<ProbationArea> {
        val pas = probationAreaRepository.findAll()
        return pas.filter { !it.description.startsWith("ZZ") }
            .map { ProbationArea(it.code, it.description, getLocalDeliveryUnits(it)) }
    }

    private fun getLocalDeliveryUnits(pa: ProbationAreaEntity): List<LocalDeliveryUnit> {
        val ldus = mutableListOf<LocalDeliveryUnit>()
        pa.boroughs.forEach {
            it.districts.filter { district: District -> district.code != "-1" }.map { district ->
                ldus.add(
                    LocalDeliveryUnit(
                        district.code,
                        district.description
                    )
                )
            }
        }
        return ldus
    }
}