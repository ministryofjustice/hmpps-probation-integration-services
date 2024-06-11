package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.entity.Institution
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.entity.InstitutionId
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.InstitutionCode

object InstitutionGenerator {
    val DEFAULT = generate("WSIHMP", "WSI")
    val STANDARD_INSTITUTIONS = InstitutionCode.entries.associateWith { generate(it.code, null) }
    val MOVED_TO = generate("SWIHMP", "SWI")
    val MOVED_TO_WITH_POM = generate("BIRHMP", "BIR")

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
