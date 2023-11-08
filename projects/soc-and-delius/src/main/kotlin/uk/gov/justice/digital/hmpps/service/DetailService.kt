package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.controller.IdentifierType
import uk.gov.justice.digital.hmpps.entity.ConvictionEventRepository
import uk.gov.justice.digital.hmpps.entity.CustodyRepository
import uk.gov.justice.digital.hmpps.entity.DetailReleaseRepository
import uk.gov.justice.digital.hmpps.entity.DetailRepository
import uk.gov.justice.digital.hmpps.entity.NsiRepository
import uk.gov.justice.digital.hmpps.entity.findByCrn
import uk.gov.justice.digital.hmpps.entity.findByNomsNumber
import uk.gov.justice.digital.hmpps.entity.getLatestConviction
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
    private val detailReleaseRepository: DetailReleaseRepository,
    private val nsiRepository: NsiRepository
) {
    fun getDetails(value: String, type: IdentifierType): Detail {
        val p = when (type) {
            IdentifierType.CRN -> detailRepository.findByCrn(value)
            IdentifierType.NOMS -> detailRepository.findByNomsNumber(value)
        }
        val c = convictionEventRepository.getLatestConviction(p.id)
        var mainOffence = ""
        var releaseLocation: String? = null
        var releaseDate: LocalDate? = null
        var recallDate: LocalDate? = null
        var releaseReason: String? = null
        var recallReason: String? = null
        var keyDates = listOf<KeyDate>()
        if (c != null) {
            mainOffence = c.mainOffence!!.offence.description
            if (c.disposal != null) {
                val custody = custodyRepository.getCustodyByDisposalId(c.disposal.id)
                if (custody != null) {
                    val release = detailReleaseRepository.findFirstByCustodyIdOrderByDateDesc(custody.id)
                    releaseLocation = release?.institution?.name
                    releaseReason = release?.releaseType?.description
                    releaseDate = release?.date
                    recallDate = release?.recall?.date
                    recallReason = release?.recall?.reason?.description
                    keyDates = custody.keyDates.map { KeyDate(it.type.code, it.type.description, it.date) }
                }
            }
        }

        val nsiDates = nsiRepository.findBreachAndRecallDates(p.id)

        val personManager = p.personManager.first()
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
            releaseReason,
            releaseLocation,
            recallDate,
            recallReason,
            nsiDates.firstOrNull { it.name == "recall" }?.referralDate,
            nsiDates.firstOrNull { it.name == "breach" }?.referralDate
        )
    }
}
