import uk.gov.justice.digital.hmpps.user.User

object UserGenerator {
    val APPLICATION_USER = User(IdGenerator.getAndIncrement(), "MakeRecallDecisionsAndDelius")
    val TEST_USER1 = User(IdGenerator.getAndIncrement(), "Test1")
    val TEST_USER2 = User(IdGenerator.getAndIncrement(), "Test2")
}
