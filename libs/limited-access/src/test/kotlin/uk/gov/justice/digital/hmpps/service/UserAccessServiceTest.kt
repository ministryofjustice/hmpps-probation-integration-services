package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.entity.LimitedAccessUser
import uk.gov.justice.digital.hmpps.entity.PersonAccess
import uk.gov.justice.digital.hmpps.entity.UserAccessRepository

@ExtendWith(MockitoExtension::class)
internal class UserAccessServiceTest {
    @Mock
    internal lateinit var uar: UserAccessRepository

    @InjectMocks
    internal lateinit var userAccessService: UserAccessService

    @Test
    fun `user limited access is correctly returned`() {
        val pas = givenLimitedAccessResults()
        whenever(uar.findByUsername("john-smith")).thenReturn(LimitedAccessUser("john-smith", 1))
        whenever(uar.getAccessFor("john-smith", listOf("E123456", "R123456", "B123456", "N123456")))
            .thenReturn(pas)

        val res = userAccessService.userAccessFor("john-smith", listOf("E123456", "R123456", "B123456", "N123456"))

        assertThat(res.access.size, equalTo(4))
        assertThat(res, equalTo(userAccess()))
    }

    @Test
    fun `user limited access is correctly returned when user doesn't exist`() {
        val pas = givenLimitedAccessResults()
        whenever(uar.checkLimitedAccessFor(listOf("E123456", "R123456", "B123456", "N123456")))
            .thenReturn(pas)

        val res = userAccessService.userAccessFor("jane-smith", listOf("E123456", "R123456", "B123456", "N123456"))

        assertThat(res.access.size, equalTo(4))
        assertThat(res, equalTo(userAccess()))
    }

    @Test
    fun `limited access is correctly returned`() {
        val pas = givenLimitedAccessResults()
        whenever(uar.checkLimitedAccessFor(listOf("E123456", "R123456", "B123456", "N123456")))
            .thenReturn(pas)

        val res = userAccessService.checkLimitedAccessFor(listOf("E123456", "R123456", "B123456", "N123456"))

        assertThat(res.access.size, equalTo(4))
        assertThat(res, equalTo(userAccess()))
    }

    @Test
    fun `single case access is correctly returned`() {
        val pas = givenLimitedAccessResults()
        whenever(uar.findByUsername("john-smith")).thenReturn(LimitedAccessUser("john-smith", 1))
        whenever(uar.getAccessFor("john-smith", listOf("E123456"))).thenReturn(pas)

        val res = userAccessService.caseAccessFor("john-smith", "E123456")

        assertThat(res, equalTo(userAccess().access[0]))
    }

    private fun givenLimitedAccessResults() =
        listOf(
            object : PersonAccess {
                override val crn = "E123456"
                override val exclusionMessage = "This person has an exclusion"
                override val restrictionMessage = null
            },
            object : PersonAccess {
                override val crn = "R123456"
                override val exclusionMessage = null
                override val restrictionMessage = "This person has a restriction"
            },
            object : PersonAccess {
                override val crn = "B123456"
                override val exclusionMessage = "This person has an exclusion"
                override val restrictionMessage = "This person has a restriction"
            },
            object : PersonAccess {
                override val crn = "N123456"
                override val exclusionMessage = null
                override val restrictionMessage = null
            }
        )

    private fun userAccess(): UserAccess =
        UserAccess(
            listOf(
                CaseAccess("E123456", userExcluded = true, userRestricted = false, "This person has an exclusion"),
                CaseAccess(
                    "R123456",
                    userExcluded = false,
                    userRestricted = true,
                    restrictionMessage = "This person has a restriction"
                ),
                CaseAccess(
                    "B123456",
                    userExcluded = true,
                    userRestricted = true,
                    "This person has an exclusion",
                    "This person has a restriction"
                ),
                CaseAccess("N123456", userExcluded = false, userRestricted = false)
            )
        )
}
