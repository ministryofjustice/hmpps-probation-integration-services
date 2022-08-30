package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.Institution
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.InstitutionId
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.InstitutionCode
import uk.gov.justice.digital.hmpps.listener.prisonId

object InstitutionGenerator {
    val DEFAULT = generate(MessageGenerator.PRISONER_RELEASED.additionalInformation.prisonId())
    val STANDARD_INSTITUTIONS = InstitutionCode.values().associateWith { generate(it.code) }

    fun generate(prisonId: String): Institution {
        val institution = Institution(
            id = InstitutionId(IdGenerator.getAndIncrement(), true),
            code = prisonId.padEnd(6, 'X'),
            nomisCdeCode = prisonId,
            description = "Test institution ($prisonId)",
            probationArea = if (prisonId.length == 3) ProbationAreaGenerator.generate(prisonId) else null
        )
        institution.probationArea?.institution = institution
        return institution
    }
}
