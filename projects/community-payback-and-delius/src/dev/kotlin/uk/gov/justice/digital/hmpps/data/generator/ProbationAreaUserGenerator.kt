package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.ProbationAreaUser
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ProbationAreaUserId
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.entity.User

object ProbationAreaUserGenerator {
    val DEFAULT_PROBATION_AREA_USER =
        generateProbationAreaUser(UserGenerator.DEFAULT_USER, ProviderGenerator.DEFAULT_PROVIDER)
    val SECOND_DEFAULT_PROBATION_AREA_USER =
        generateProbationAreaUser(UserGenerator.DEFAULT_USER, ProviderGenerator.SECOND_PROVIDER)
    val DEFAULT_USER_UNSELECTABLE_PROBATION_AREA =
        generateProbationAreaUser(UserGenerator.DEFAULT_USER, ProviderGenerator.UNSELECTABLE_PROVIDER)

    fun generateProbationAreaUser(user: User, provider: Provider) =
        ProbationAreaUser(
            ProbationAreaUserId(user, provider)
        )
}