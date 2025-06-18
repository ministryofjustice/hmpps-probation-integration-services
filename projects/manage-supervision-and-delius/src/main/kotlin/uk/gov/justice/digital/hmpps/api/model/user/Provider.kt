package uk.gov.justice.digital.hmpps.api.model.user

import uk.gov.justice.digital.hmpps.api.model.sentence.User

data class Provider(
    val code: String,
    val name: String,
)

data class UserProviderResponse(
    val providers: List<Provider>,
    val teams: List<Team>,
    val users: List<User>
)