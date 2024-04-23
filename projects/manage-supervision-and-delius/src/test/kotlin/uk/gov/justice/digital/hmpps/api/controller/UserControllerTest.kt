package uk.gov.justice.digital.hmpps.api.controller

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.USER
import uk.gov.justice.digital.hmpps.service.UserService
import uk.gov.justice.digital.hmpps.service.toUser

@ExtendWith(MockitoExtension::class)
internal class UserControllerTest {

    @Mock
    lateinit var userService: UserService

    @InjectMocks
    lateinit var controller: UserController

    @Test
    fun `calls get get activity function `() {
        val username = "username"
        val expectedResponse = USER.toUser()
        whenever(userService.getUserDetails(username)).thenReturn(expectedResponse)
        val res = controller.getCaseload(username)
        assertThat(res, equalTo(expectedResponse))
    }
}