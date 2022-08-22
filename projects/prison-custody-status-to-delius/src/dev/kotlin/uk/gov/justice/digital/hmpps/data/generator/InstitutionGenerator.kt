package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.institution.Institution
import uk.gov.justice.digital.hmpps.integrations.delius.institution.InstitutionId
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.InstitutionCode
import uk.gov.justice.digital.hmpps.listener.prisonId

object InstitutionGenerator {
    val RELEASED_FROM = generate(MessageGenerator.PRISONER_RELEASED.additionalInformation.prisonId())
    val STANDARD_INSTITUTIONS = InstitutionCode.values().associateWith { generate(it.code) }

    fun generate(code: String) = Institution(
        id = InstitutionId(IdGenerator.getAndIncrement(), true),
        code = code,
        nomisCdeCode = code,
        description = "Test institution",
        selectable = true
    )
}
