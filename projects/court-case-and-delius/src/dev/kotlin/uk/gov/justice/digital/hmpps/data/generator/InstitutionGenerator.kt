package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.Institution
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.InstitutionId

object InstitutionGenerator {

    val WSIHMP = generate("WSIHMP", "WSI")

    fun generate(code: String, prisonId: String): Institution {
        return Institution(
            id = InstitutionId(IdGenerator.getAndIncrement(), true),
            code = code,
            description = "Test institution ($code)",
            institutionName = "Test institution $code",
            establishmentType = ReferenceDataGenerator.PRISON,
            nomisCdeCode = prisonId,
            private = true
        )
    }
}
