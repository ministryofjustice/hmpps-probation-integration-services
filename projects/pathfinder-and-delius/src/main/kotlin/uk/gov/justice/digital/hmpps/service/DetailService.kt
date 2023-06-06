package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.controller.IdentifierType
import uk.gov.justice.digital.hmpps.entity.ConvictionEventRepository
import uk.gov.justice.digital.hmpps.entity.CustodyRepository
import uk.gov.justice.digital.hmpps.entity.DetailReleaseRepository
import uk.gov.justice.digital.hmpps.entity.DetailRepository
import uk.gov.justice.digital.hmpps.entity.findByCrn
import uk.gov.justice.digital.hmpps.entity.findByNomsNumber
import uk.gov.justice.digital.hmpps.entity.getLatestConviction
import uk.gov.justice.digital.hmpps.model.BatchRequest
import uk.gov.justice.digital.hmpps.model.Detail
import uk.gov.justice.digital.hmpps.model.KeyDate
import uk.gov.justice.digital.hmpps.model.Name
import uk.gov.justice.digital.hmpps.model.name
import java.time.LocalDate

@Service
class DetailService(
    private val detailRepository: DetailRepository,
    private val convictionEventRepository: ConvictionEventRepository,
    private val custodyRepository: CustodyRepository,
    private val detailReleaseRepository: DetailReleaseRepository
) {
    fun getDetails(value: String, type: IdentifierType): Detail {
        val p = when (type) {
            IdentifierType.CRN -> detailRepository.findByCrn(value)
            IdentifierType.NOMS -> detailRepository.findByNomsNumber(value)
        }
        val c = convictionEventRepository.getLatestConviction(p.id)
        var mainOffence = ""
        val keyDates = mutableListOf<KeyDate>()
        var releaseLocation: String? = null
        var releaseDate: LocalDate? = null
        if (c!=null) {
            mainOffence = c.mainOffence!!.offence.description
            if (c.disposal != null) {
                val custody = custodyRepository.getCustodyByDisposalId(c.disposal.id)
                if (custody != null) {
                    val keyDateEntities = custody.keyDates
                    keyDateEntities.forEach { keyDates.add(KeyDate(it.type.code, it.type.description, it.date)) }
                    val release = detailReleaseRepository.findFirstByCustodyIdOrderByDateDesc(custody.id)
                    releaseLocation = release?.institution?.name
                    releaseDate = release?.date
                }
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
            keyDates,
            releaseDate,
            releaseLocation
        )
    }

    fun getBatchDetails(batchRequest: BatchRequest): List<Detail> {
        val detailPersons = detailRepository.getByCrnIsIn(batchRequest.crns)
        return detailPersons.map { p ->
            Detail(
                p.name(),
                p.dateOfBirth,
                p.crn,
                p.nomsNumber,
                p.pncNumber,
                p.personManager[0].team.district.description,
                p.personManager[0].team.probationArea.description,
                Name(
                    p.personManager[0].staff.forename,
                    p.personManager[0].staff.middleName,
                    p.personManager[0].staff.surname
                ),
                "mainOffence",
                p.religion?.description
            )
        }
    }
}
