package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.service.PersonManager

object PersonManagerGenerator {
    val DEFAULT = generate()

    fun generate(softDeleted: Boolean = false, id: Long = IdGenerator.getAndIncrement()) =
        PersonManager(
            PersonGenerator.DEFAULT,
            ProviderGenerator.DEFAULT_STAFF,
            ProviderGenerator.DEFAULT_TEAM,
            softDeleted,
            true,
            id
        )
}
