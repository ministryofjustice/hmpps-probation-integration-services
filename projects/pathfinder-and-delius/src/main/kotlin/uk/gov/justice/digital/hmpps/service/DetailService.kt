package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.DetailRepository
import uk.gov.justice.digital.hmpps.entity.PersonDetail
import uk.gov.justice.digital.hmpps.model.BatchRequest
import uk.gov.justice.digital.hmpps.model.Detail
import uk.gov.justice.digital.hmpps.model.KeyDate
import uk.gov.justice.digital.hmpps.model.Name

@Service
class DetailService(
    private val detailRepository: DetailRepository,
) {
    fun getBatchDetails(batchRequest: BatchRequest) =
        detailRepository.getByCrns(batchRequest.crns).groupBy { it.crn }
            .map { pd ->
                val detail = pd.value.first()
                Detail(
                    detail.name(),
                    detail.dateOfBirth,
                    detail.crn,
                    detail.nomisId,
                    detail.pncNumber,
                    detail.ldu,
                    detail.probationArea,
                    detail.offenderManagerName(),
                    detail.mainOffence,
                    detail.religion,
                    pd.value.keyDates(),
                    detail.releaseDate,
                    detail.releaseLocation,
                )
            }

    fun List<PersonDetail>.keyDates() =
        map {
            KeyDate(it.keyDateCode, it.keyDateDesc, it.keydate)
        }

    fun PersonDetail.name() = Name(forename, listOfNotNull(middleNameOne, middleNameTwo).joinToString(" "), surname)

    fun PersonDetail.offenderManagerName() = Name(omForename, omMiddleName, omSurname)
}
