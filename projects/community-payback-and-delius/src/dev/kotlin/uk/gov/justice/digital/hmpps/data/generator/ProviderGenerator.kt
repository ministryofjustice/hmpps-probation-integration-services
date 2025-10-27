package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.ProbationAreaUser
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ProbationAreaUserId
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.entity.User

object ProviderGenerator {
    val DEFAULT_PROVIDER = generateProvider(code = "N01", description = "N01 Provider")
    val SECOND_PROVIDER = generateProvider(code = "N02", description = "N02 Provider")
    val UNSELECTABLE_PROVIDER =
        generateProvider(code = "N50", description = "N50 Inactive Provider", selectable = false)

    val DEFAULT_PROBATION_AREA_USER = generateProbationAreaUser(UserGenerator.DEFAULT_USER, DEFAULT_PROVIDER)
    val SECOND_DEFAULT_PROBATION_AREA_USER = generateProbationAreaUser(UserGenerator.DEFAULT_USER, SECOND_PROVIDER)
    val DEFAULT_USER_UNSELECTABLE_PROBATION_AREA =
        generateProbationAreaUser(UserGenerator.DEFAULT_USER, UNSELECTABLE_PROVIDER)

    fun generateProvider(
        id: Long = IdGenerator.getAndIncrement(),
        code: String,
        description: String,
        selectable: Boolean = true
    ) = Provider(id, code, description, selectable)

    fun generateProbationAreaUser(user: User, provider: Provider) =
        ProbationAreaUser(
            ProbationAreaUserId(user, provider)
        )
}