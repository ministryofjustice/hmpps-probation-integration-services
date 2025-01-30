package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.User

object UserGenerator {
    val AUDIT_USER = User(IdGenerator.getAndIncrement(), null, "ManageSupervisionAndDelius", "Manage", "Supervisions")
}
