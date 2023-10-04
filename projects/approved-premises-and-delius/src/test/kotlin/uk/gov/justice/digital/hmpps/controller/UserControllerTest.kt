package uk.gov.justice.digital.hmpps.controller

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.service.CaseAccess
import uk.gov.justice.digital.hmpps.service.UserAccess
import uk.gov.justice.digital.hmpps.service.UserAccessService

@ExtendWith(MockitoExtension::class)
class UserControllerTest {
    @Mock
    internal lateinit var userAccessService: UserAccessService

    @InjectMocks
    internal lateinit var userController: UserController

    @Test
    fun `when username provided, calls service with username`() {
        val username = "john-smith"
        val crns = listOf("T123456", "T234567", "T345678")
        whenever(userAccessService.userAccessFor(username, crns)).thenReturn(
            UserAccess(
                crns.map {
                    CaseAccess(
                        it,
                        userExcluded = false,
                        userRestricted = false
                    )
                }
            )
        )

        val res = userController.userAccessCheck(username, crns)
        verify(userAccessService).userAccessFor(username, crns)
        assertThat(res.access.size, equalTo(3))
    }

    @Test
    fun `when no username provided, calls service without username`() {
        val crns = listOf("N123456", "N234567", "N345678")
        whenever(userAccessService.checkLimitedAccessFor(crns)).thenReturn(
            UserAccess(
                crns.map {
                    CaseAccess(
                        it,
                        userExcluded = false,
                        userRestricted = false
                    )
                }
            )
        )

        val res = userController.userAccessCheck(null, crns)
        verify(userAccessService).checkLimitedAccessFor(crns)
        assertThat(res.access.size, equalTo(3))
    }
}
