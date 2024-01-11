package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.User

object UserGenerator {
    val USER = User(IdGenerator.getAndIncrement(), "HmppsAuthAndDelius")
    val TEST_USER = User(IdGenerator.getAndIncrement(), "test.user")
    val INACTIVE_USER = User(IdGenerator.getAndIncrement(), "test.user.inactive")
}
