package uk.gov.justice.digital.hmpps.service

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import uk.gov.justice.digital.hmpps.api.model.*
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integration.delius.sentence.entity.Custody
import uk.gov.justice.digital.hmpps.integration.delius.sentence.entity.CustodyRepository

@Service
class SentenceService(val custodyRepository: CustodyRepository) {
    fun findLatestReleaseRecall(crn: String): ReleaseRecall {
        val sentences = custodyRepository.findAllByDisposalEventPersonCrn(crn)
        return when (sentences.size) {
            0 -> throw NotFoundException("No custodial sentences found for $crn")
            1 -> sentences.first().asReleaseRecall()
            else -> throw ResponseStatusException(
                HttpStatus.EXPECTATION_FAILED,
                "Multiple custodial sentences found for $crn"
            )
        }
    }

    private fun Custody.asReleaseRecall(): ReleaseRecall {
        val release = mostRecentRelease()
        return ReleaseRecall(release?.toModel(), release?.recall?.toModel())
    }

    private fun uk.gov.justice.digital.hmpps.integration.delius.sentence.entity.Release.toModel() = Release(
        date.toLocalDate(), notes, institution?.toModel(), type.codeDescription()
    )

    private fun uk.gov.justice.digital.hmpps.integration.delius.provider.entity.Institution.toModel() = Institution(
        id, establishment, code, description, name, type?.codeDescription(), private, nomisCdeCode
    )

    private fun uk.gov.justice.digital.hmpps.integration.delius.sentence.entity.Recall.toModel() = Recall(
        date.toLocalDate(), CodeDescription(reason.code, reason.description), notes
    )
}