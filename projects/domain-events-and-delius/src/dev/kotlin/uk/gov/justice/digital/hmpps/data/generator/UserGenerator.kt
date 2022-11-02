import uk.gov.justice.digital.hmpps.user.User

object UserGenerator {
    val APPLICATION_USER = User(IdGenerator.getAndIncrement(), "domain-events-and-delius")
}
