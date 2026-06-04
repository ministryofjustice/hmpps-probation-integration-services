package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.entity.ExclusionDetail
import uk.gov.justice.digital.hmpps.entity.RestrictionDetail
import uk.gov.justice.digital.hmpps.entity.LimitedAccessUser
import uk.gov.justice.digital.hmpps.entity.PersonAccess
import uk.gov.justice.digital.hmpps.entity.UserAccessRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime

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

        assertThat(res.access, hasSize(4))
        assertThat(res, equalTo(userAccess()))
    }

    @Test
    fun `user limited access is correctly returned when user doesn't exist`() {
        val pas = givenLimitedAccessResults()
        whenever(uar.checkLimitedAccessFor(listOf("E123456", "R123456", "B123456", "N123456")))
            .thenReturn(pas)

        val res = userAccessService.userAccessFor("jane-smith", listOf("E123456", "R123456", "B123456", "N123456"))

        assertThat(res.access, hasSize(4))
        assertThat(res, equalTo(userAccess()))
    }

    @Test
    fun `limited access is correctly returned`() {
        val pas = givenLimitedAccessResults()
        whenever(uar.checkLimitedAccessFor(listOf("E123456", "R123456", "B123456", "N123456")))
            .thenReturn(pas)

        val res = userAccessService.checkLimitedAccessFor(listOf("E123456", "R123456", "B123456", "N123456"))

        assertThat(res.access, hasSize(4))
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

    @Test
    fun `allCaseAccessForCrn returns exclusions and restrictions`() {
        val exclusions = listOf(stubExclusionDetail("excluded-user"))
        val restrictions = listOf(stubLaoDetail("restricted-user"))
        whenever(uar.findLimitedAccessPersonByCrn("B123456")).thenReturn(null)
        whenever(uar.getExclusionsForCrn("B123456")).thenReturn(exclusions)
        whenever(uar.getRestrictionsForCrn("B123456")).thenReturn(restrictions)

        val res = userAccessService.allCaseAccessForCrn("B123456")

        assertThat(res.crn, equalTo("B123456"))
        assertThat(res.excludedFrom!!.size, equalTo(1))
        assertThat(res.excludedFrom!![0].username, equalTo("excluded-user"))
        assertThat(res.restrictedTo!!.size, equalTo(1))
        assertThat(res.restrictedTo!![0].username, equalTo("restricted-user"))
    }

    @Test
    fun `allCaseAccessForCrn returns null lists when no exclusions or restrictions`() {
        whenever(uar.findLimitedAccessPersonByCrn("N123456")).thenReturn(null)
        whenever(uar.getExclusionsForCrn("N123456")).thenReturn(emptyList())
        whenever(uar.getRestrictionsForCrn("N123456")).thenReturn(emptyList())

        val res = userAccessService.allCaseAccessForCrn("N123456")

        assertThat(res.crn, equalTo("N123456"))
        assertThat(res.excludedFrom, nullValue())
        assertThat(res.restrictedTo, nullValue())
        assertThat(res.exclusionMessage, nullValue())
        assertThat(res.restrictionMessage, nullValue())
    }

    @Test
    fun `allCaseAccessForCrn returns only exclusions when no restrictions`() {
        whenever(uar.findLimitedAccessPersonByCrn("E123456")).thenReturn(null)
        whenever(uar.getExclusionsForCrn("E123456")).thenReturn(listOf(stubExclusionDetail("excluded-user")))
        whenever(uar.getRestrictionsForCrn("E123456")).thenReturn(emptyList())

        val res = userAccessService.allCaseAccessForCrn("E123456")

        assertThat(res.excludedFrom!!.size, equalTo(1))
        assertThat(res.restrictedTo, nullValue())
    }

    @Test
    fun `allCaseAccessForCrn returns only restrictions when no exclusions`() {
        whenever(uar.findLimitedAccessPersonByCrn("R123456")).thenReturn(null)
        whenever(uar.getExclusionsForCrn("R123456")).thenReturn(emptyList())
        whenever(uar.getRestrictionsForCrn("R123456")).thenReturn(listOf(stubLaoDetail("restricted-user")))

        val res = userAccessService.allCaseAccessForCrn("R123456")

        assertThat(res.excludedFrom, nullValue())
        assertThat(res.restrictedTo!!.size, equalTo(1))
    }

    @Test
    fun `allCaseAccessForCrn includes messages from person record`() {
        val person = uk.gov.justice.digital.hmpps.entity.LimitedAccessPerson(
            crn = "E123456",
            exclusionMessage = "You are excluded",
            restrictionMessage = "Access is restricted",
            id = 1L
        )
        whenever(uar.findLimitedAccessPersonByCrn("E123456")).thenReturn(person)
        whenever(uar.getExclusionsForCrn("E123456")).thenReturn(emptyList())
        whenever(uar.getRestrictionsForCrn("E123456")).thenReturn(emptyList())

        val res = userAccessService.allCaseAccessForCrn("E123456")

        assertThat(res.exclusionMessage, equalTo("You are excluded"))
        assertThat(res.restrictionMessage, equalTo("Access is restricted"))
    }

    private fun stubExclusionDetail(username: String) = object : ExclusionDetail {
        override val username = username
        override val start: LocalDate = LocalDate.now().minusDays(1)
        override val end: LocalDateTime? = null
    }

    private fun stubLaoDetail(username: String) = object : RestrictionDetail {
        override val username = username
        override val since: ZonedDateTime = ZonedDateTime.now()
        override val until: ZonedDateTime = ZonedDateTime.now().plusDays(30)
    }

    private fun givenLimitedAccessResults() =
        listOf(
            object : PersonAccess {
                override val crn = "E123456"
                override val excluded = true
                override val restricted = false
                override val exclusionMessage = "This person has an exclusion"
                override val restrictionMessage = null
            },
            object : PersonAccess {
                override val crn = "R123456"
                override val excluded = false
                override val restricted = true
                override val exclusionMessage = null
                override val restrictionMessage = "This person has a restriction"
            },
            object : PersonAccess {
                override val crn = "B123456"
                override val excluded = true
                override val restricted = true
                override val exclusionMessage = "This person has an exclusion"
                override val restrictionMessage = "This person has a restriction"
            },
            object : PersonAccess {
                override val crn = "N123456"
                override val excluded = false
                override val restricted = false
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
