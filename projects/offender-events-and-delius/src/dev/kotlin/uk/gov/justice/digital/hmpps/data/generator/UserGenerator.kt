package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.user.User

object UserGenerator {
    val APPLICATION_USER = User(IdGenerator.getAndIncrement(), "OffenderEventsAndDelius")
}
