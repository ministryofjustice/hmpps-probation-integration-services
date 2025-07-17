package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.entity.sentence.Disposal
import uk.gov.justice.digital.hmpps.entity.sentence.custody.Custody

object CustodyGenerator {
    fun generate(disposal: Disposal) = Custody(
        id = id(),
        releases = listOf(),
        postSentenceSupervisionRequirements = listOf(),
        postSentenceSupervisionEndDate = listOf(),
        probationResetDate = listOf(),
        licenceEndDate = listOf(),
        disposal = disposal,
        softDeleted = false,
    )
}
