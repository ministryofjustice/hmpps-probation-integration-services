package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.controller.IdentifierType
import uk.gov.justice.digital.hmpps.entity.*
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.model.KeyDate
import uk.gov.justice.digital.hmpps.model.Team
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
        val currentlyInPrison = custodyRepository.isInCustody(p.id)
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
            Manager(
                name = Name(personManager.staff.forename, personManager.staff.middleName, personManager.staff.surname),
                team = Team(
                    code = personManager.team.code,
                    localDeliveryUnit = Ldu(
                        code = personManager.team.district.code,
                        name = personManager.team.district.description
                    )
                ),
                provider = Provider(personManager.probationArea.code, personManager.probationArea.description)
            ),
            p.currentDisposal,
            currentlyInPrison,
            mainOffence,
            p.toProfile(),
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

fun DetailPerson.toProfile() = if (nationality?.description != null || religion?.description != null) {
    Profile(nationality?.description, religion?.description)
} else {
    null
}

