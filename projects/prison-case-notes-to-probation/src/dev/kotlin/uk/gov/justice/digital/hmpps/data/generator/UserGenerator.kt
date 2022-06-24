package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.audit.User

object UserGenerator {
    val APPLICATION_USER = User(IdGenerator.getAndIncrement(), "prison-case-notes-to-probation")
}
