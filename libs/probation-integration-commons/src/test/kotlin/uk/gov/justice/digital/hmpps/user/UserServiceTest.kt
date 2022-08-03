package uk.gov.justice.digital.hmpps.user

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
class UserServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @InjectMocks
    private lateinit var userService: UserService

    @Test
    fun `user service calls repository with same values`() {
        val username = "ServiceUsername"
        userService.findUser(username)
        verify(userRepository).findUserByUsername(username)
    }
}
