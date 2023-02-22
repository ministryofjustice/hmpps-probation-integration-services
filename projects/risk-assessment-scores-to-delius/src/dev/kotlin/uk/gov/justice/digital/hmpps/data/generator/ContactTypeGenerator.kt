package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.entity.OGRS_ASSESSMENT_CT

object ContactTypeGenerator {
    val DEFAULT = generate()

    fun generate(id: Long = IdGenerator.getAndIncrement()) = ContactType(id, OGRS_ASSESSMENT_CT )
}
