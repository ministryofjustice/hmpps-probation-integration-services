package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import uk.gov.justice.digital.hmpps.entity.ExclusionDetail
import uk.gov.justice.digital.hmpps.entity.LimitedAccessDetail
import uk.gov.justice.digital.hmpps.entity.LimitedAccessPerson
import uk.gov.justice.digital.hmpps.entity.LimitedAccessRow
import uk.gov.justice.digital.hmpps.entity.LimitedAccessUser
import uk.gov.justice.digital.hmpps.entity.PersonAccess
import uk.gov.justice.digital.hmpps.entity.RestrictionDetail
import uk.gov.justice.digital.hmpps.entity.UserAccessRepository
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.OffsetDateTime
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
        whenever(uar.findLimitedAccessPersonByCrn("B123456")).thenReturn(null)
        whenever(uar.getExclusionsForCrn("B123456")).thenReturn(
            listOf(
                object : ExclusionDetail {
                    override val username = "excluded-user"
                    override val since: ZonedDateTime = ZonedDateTime.now().minusDays(1)
                    override val until: ZonedDateTime? = null
                }
            )
        )
        whenever(uar.getRestrictionsForCrn("B123456")).thenReturn(
            listOf(
                object : RestrictionDetail {
                    override val username = "restricted-user"
                    override val since: ZonedDateTime = ZonedDateTime.now().minusDays(1)
                    override val until: ZonedDateTime? = null
                }
            )
        )

        val res = userAccessService.allCaseAccessForCrn("B123456")
        val excludedFrom = checkNotNull(res.excludedFrom)
        val restrictedTo = checkNotNull(res.restrictedTo)

        assertThat(res.crn, equalTo("B123456"))
        assertThat(excludedFrom.size, equalTo(1))
        assertThat(excludedFrom[0].username, equalTo("excluded-user"))
        assertThat(restrictedTo.size, equalTo(1))
        assertThat(restrictedTo[0].username, equalTo("restricted-user"))
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
        whenever(uar.getExclusionsForCrn("E123456")).thenReturn(
            listOf(
                object : ExclusionDetail {
                    override val username = "excluded-user"
                    override val since: ZonedDateTime = ZonedDateTime.now().minusDays(1)
                    override val until: ZonedDateTime? = null
                }
            )
        )
        whenever(uar.getRestrictionsForCrn("E123456")).thenReturn(emptyList())

        val res = userAccessService.allCaseAccessForCrn("E123456")

        assertThat(res.excludedFrom!!.size, equalTo(1))
        assertThat(res.restrictedTo, nullValue())
    }

    @Test
    fun `allCaseAccessForCrn returns only restrictions when no exclusions`() {
        whenever(uar.findLimitedAccessPersonByCrn("R123456")).thenReturn(null)
        whenever(uar.getExclusionsForCrn("R123456")).thenReturn(emptyList())
        whenever(uar.getRestrictionsForCrn("R123456")).thenReturn(
            listOf(
                object : RestrictionDetail {
                    override val username = "restricted-user"
                    override val since: ZonedDateTime = ZonedDateTime.now().minusDays(1)
                    override val until: ZonedDateTime? = null
                }
            )
        )

        val res = userAccessService.allCaseAccessForCrn("R123456")

        assertThat(res.excludedFrom, nullValue())
        assertThat(res.restrictedTo!!.size, equalTo(1))
    }

    @Test
    fun `allCaseAccessForCrn includes messages from person record`() {
        val person = LimitedAccessPerson(
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

    @Test
    fun `allCases delegates to repository and maps result`() {
        val pageable = PageRequest.of(2, 25)
        val row = object : LimitedAccessRow {
            override val crn = "B123456"
            override val username = "john-smith"
            override val type = "Restriction"
            override val exclusionMessage: String? = null
            override val restrictionMessage: String? = null
            override val startDate = "2026-09-21T10:15:30Z"
            override val endDate: Any? = null
        }
        val expected: PageImpl<LimitedAccessRow> = PageImpl(listOf(row), pageable, 1)
        whenever(uar.getAll(pageable)).thenReturn(expected)

        val res = userAccessService.allCases(pageable)

        assertThat(
            res,
            equalTo(
                PageImpl(
                    listOf(
                        LimitedAccessDetail(
                            crn = "B123456",
                            username = "john-smith",
                            type = "Restriction",
                            exclusionMessage = null,
                            restrictionMessage = null,
                            startDate = ZonedDateTime.parse("2026-09-21T10:15:30Z"),
                            endDate = null,
                        )
                    ),
                    pageable,
                    1
                )
            )
        )
    }

    @Test
    fun `allCases returns empty page from repository`() {
        val pageable = PageRequest.of(0, 10)
        val expected: PageImpl<LimitedAccessRow> = PageImpl(emptyList(), pageable, 0)
        whenever(uar.getAll(pageable)).thenReturn(expected)

        val res = userAccessService.allCases(pageable)

        assertThat(res.totalElements, equalTo(0L))
        assertThat(res.content, hasSize(0))
    }

    @Test
    fun `allCases maps zoned offset timestamp and local datetimes`() {
        val pageable = PageRequest.of(0, 10)
        whenever(uar.getAll(pageable)).thenReturn(
            PageImpl(
                listOf(
                    limitedAccessRow(
                        startDate = ZonedDateTime.parse("2026-09-21T10:15:30Z"),
                        endDate = OffsetDateTime.parse("2026-09-21T11:15:30+01:00"),
                    )
                ),
                pageable,
                1
            )
        )

        val res = userAccessService.allCases(pageable).content.single()

        assertThat(res.startDate, equalTo(ZonedDateTime.parse("2026-09-21T10:15:30Z")))
        assertThat(res.endDate, equalTo(ZonedDateTime.parse("2026-09-21T11:15:30+01:00")))
    }

    @Test
    fun `allCases maps string datetime values`() {
        val pageable = PageRequest.of(0, 10)
        whenever(uar.getAll(pageable)).thenReturn(
            PageImpl(
                listOf(
                    limitedAccessRow(
                        startDate = "2026-09-21T10:15:30Z",
                        endDate = "2026-09-21T11:15:30+01:00[Europe/London]",
                    )
                ),
                pageable,
                1
            )
        )

        val res = userAccessService.allCases(pageable).content.single()

        assertThat(res.startDate, equalTo(ZonedDateTime.parse("2026-09-21T10:15:30Z")))
        assertThat(res.endDate, equalTo(ZonedDateTime.parse("2026-09-21T11:15:30+01:00[Europe/London]")))
    }

    @Test
    fun `allCases maps oracle timestamp with timezone values`() {
        val pageable = PageRequest.of(0, 10)
        whenever(uar.getAll(pageable)).thenReturn(
            PageImpl(
                listOf(
                    limitedAccessRow(
                        startDate = oracle.sql.TIMESTAMPTZ(ZonedDateTime.parse("2026-09-21T10:15:30Z")),
                        endDate = oracle.sql.TIMESTAMPTZ(ZonedDateTime.parse("2026-09-21T11:15:30+01:00[Europe/London]")),
                    )
                ),
                pageable,
                1
            )
        )

        val res = userAccessService.allCases(pageable).content.single()

        assertThat(res.startDate, equalTo(ZonedDateTime.parse("2026-09-21T10:15:30Z")))
        assertThat(res.endDate, equalTo(ZonedDateTime.parse("2026-09-21T11:15:30+01:00[Europe/London]")))
    }

    @Test
    fun `allCases throws for unsupported datetime types`() {
        val pageable = PageRequest.of(0, 10)
        whenever(uar.getAll(pageable)).thenReturn(
            PageImpl(
                listOf(
                    limitedAccessRow(
                        startDate = 1,
                    )
                ),
                pageable,
                1
            )
        )

        val error = assertThrows<UnsupportedOperationException> {
            userAccessService.allCases(pageable)
        }

        assertThat(error.message, equalTo("Cannot convert kotlin.Int to ZonedDateTime"))
    }

    @Test
    fun `allCases wraps oracle conversion failures`() {
        val pageable = PageRequest.of(0, 10)
        whenever(uar.getAll(pageable)).thenReturn(
            PageImpl(
                listOf(
                    limitedAccessRow(
                        startDate = oracle.sql.TIMESTAMPTZ(
                            ZonedDateTime.parse("2026-09-21T10:15:30Z"),
                            shouldThrow = true
                        ),
                    )
                ),
                pageable,
                1
            )
        )

        val error = assertThrows<UnsupportedOperationException> {
            userAccessService.allCases(pageable)
        }

        assertThat(error.message, equalTo("Cannot convert oracle.sql.TIMESTAMPTZ to ZonedDateTime"))
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

    private fun limitedAccessRow(
        crn: String = "B123456",
        username: String = "john-smith",
        type: String = "Restriction",
        exclusionMessage: String? = null,
        restrictionMessage: String? = null,
        startDate: Any,
        endDate: Any? = null,
    ) = object : LimitedAccessRow {
        override val crn = crn
        override val username = username
        override val type = type
        override val exclusionMessage = exclusionMessage
        override val restrictionMessage = restrictionMessage
        override val startDate = startDate
        override val endDate = endDate
    }
}
