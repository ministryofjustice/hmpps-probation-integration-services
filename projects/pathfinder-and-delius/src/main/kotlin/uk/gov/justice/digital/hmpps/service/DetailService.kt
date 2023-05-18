package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.controller.IdentifierType
import uk.gov.justice.digital.hmpps.entity.ConvictionEventRepository
import uk.gov.justice.digital.hmpps.entity.CustodyRepository
import uk.gov.justice.digital.hmpps.entity.DetailRepository
import uk.gov.justice.digital.hmpps.model.Detail
import uk.gov.justice.digital.hmpps.model.KeyDate
import uk.gov.justice.digital.hmpps.model.Name
import uk.gov.justice.digital.hmpps.model.name

@Service
class DetailService(
    private val detailRepository: DetailRepository,
    private val convictionEventRepository: ConvictionEventRepository,
    private val custodyRepository: CustodyRepository
) {
    fun getDetails(value: String, type: IdentifierType): Detail {
        val p = when (type) {
            IdentifierType.CRN -> detailRepository.getByCrn(value)
            IdentifierType.NOMS -> detailRepository.getByNomsNumber(value)
        }
        val c = convictionEventRepository.getAllByConvictionEventPersonId(p.id)
        var mainOffence: String? = ""
        val keyDates = mutableListOf<KeyDate>()
        if (c.isNotEmpty()) {
            val convictionEvent = c.sortedBy { it.convictionDate }[0]
            mainOffence = convictionEvent.mainOffence?.offence?.description
            if (convictionEvent.disposal != null) {
                val keyDateEntities = custodyRepository.getCustodyByDisposalId(convictionEvent.disposal.id).keyDates
                keyDateEntities.forEach { keyDates.add(KeyDate(it.type.code, it.type.description, it.date)) }
            }
        }

        val personManager = p.personManager[0]
        return Detail(
            p.name(),
            p.dateOfBirth,
            p.crn,
            p.nomsNumber,
            p.pncNumber,
            personManager.team.district.description,
            personManager.team.probationArea.description,
            Name(personManager.staff.forename, personManager.staff.middleName, personManager.staff.surname),
            mainOffence,
            p.religion?.description,
            keyDates
        )
    }
}
