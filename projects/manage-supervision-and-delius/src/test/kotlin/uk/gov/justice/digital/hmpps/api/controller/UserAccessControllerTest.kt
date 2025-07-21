package uk.gov.justice.digital.hmpps.api.controller

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.service.CaseAccess
import uk.gov.justice.digital.hmpps.service.UserAccessService
import uk.gov.justice.digital.hmpps.service.UserService

@ExtendWith(MockitoExtension::class)
internal class UserAccessControllerTest {
    @Mock
    lateinit var userAccessService: UserAccessService

    @Mock
    lateinit var userService: UserService

    @InjectMocks
    lateinit var userAccessController: UserAccessController

    @Test
    fun `check user access`() {
        val caseAccess = CaseAccess(
            crn = "crn",
            userRestricted = false,
            userExcluded = true,
            exclusionMessage = "testing",
        )

        whenever(userAccessService.caseAccessFor("username", "crn")).thenReturn(caseAccess)

        val response = userAccessController.checkAccess("username", "crn")

        assertThat(response, equalTo(caseAccess))
    }
}