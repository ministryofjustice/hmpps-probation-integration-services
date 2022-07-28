import uk.gov.justice.digital.hmpps.integrations.delius.audit.User

object UserGenerator {
    val APPLICATION_USER = User(IdGenerator.getAndIncrement(), "workforce-allocations-to-delius")
}
