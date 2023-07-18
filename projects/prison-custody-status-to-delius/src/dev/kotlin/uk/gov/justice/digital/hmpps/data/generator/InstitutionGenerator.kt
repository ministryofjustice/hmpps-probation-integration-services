package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.Institution
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.InstitutionId
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.InstitutionCode
import uk.gov.justice.digital.hmpps.messaging.prisonId

object InstitutionGenerator {
    val DEFAULT = MessageGenerator.PRISONER_RELEASED.additionalInformation.prisonId()!!.let { generate(it + "HMP", it) }
    val STANDARD_INSTITUTIONS = InstitutionCode.entries.associateWith { generate(it.code, null) }

    fun generate(code: String, prisonId: String?): Institution {
        val institution = Institution(
            id = InstitutionId(IdGenerator.getAndIncrement(), true),
            code = code,
            nomisCdeCode = prisonId,
            description = "Test institution ($code)",
            probationArea = prisonId?.length?.let { ProbationAreaGenerator.generate(prisonId) }
        )
        institution.probationArea?.institution = institution
        return institution
    }
}
