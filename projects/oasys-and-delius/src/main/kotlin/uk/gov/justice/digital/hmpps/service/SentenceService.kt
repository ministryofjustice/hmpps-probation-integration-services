package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.*
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integration.delius.sentence.Custody
import uk.gov.justice.digital.hmpps.integration.delius.sentence.CustodyRepository

@Service
class SentenceService(val custodyRepository: CustodyRepository) {
    fun findLatestReleaseRecall(crn: String): ReleaseRecall {
        val sentences = custodyRepository.findAllByDisposalEventPersonCrn(crn)
        return when (sentences.size) {
            0 -> throw NotFoundException("No custodial sentences found for $crn")
            1 -> sentences.first().asReleaseRecall()
            else -> error("Multiple custodial sentences found for $crn")
        }
    }

    fun Custody.asReleaseRecall(): ReleaseRecall {
        val release = mostRecentRelease()
        return ReleaseRecall(release?.toModel(), release?.recall?.toModel())
    }

    fun uk.gov.justice.digital.hmpps.integration.delius.sentence.Release.toModel() = Release(
        date.toLocalDate(), notes, institution?.toModel(), type.codeDescription()
    )

    fun uk.gov.justice.digital.hmpps.integration.delius.provider.entity.Institution.toModel() = Institution(
        id, establishment, code, description, name, type?.codeDescription(), private, nomisCdeCode
    )

    fun uk.gov.justice.digital.hmpps.integration.delius.sentence.Recall.toModel() = Recall(
        date.toLocalDate(), CodeDescription(reason.code, reason.description), notes
    )
}