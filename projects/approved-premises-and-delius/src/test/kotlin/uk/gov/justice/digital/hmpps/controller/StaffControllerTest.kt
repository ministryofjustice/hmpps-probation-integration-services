package uk.gov.justice.digital.hmpps.controller

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.springframework.data.domain.Pageable
import uk.gov.justice.digital.hmpps.service.StaffService

@ExtendWith(MockitoExtension::class)
internal class StaffControllerTest {
    @Mock
    lateinit var staffService: StaffService

    @InjectMocks
    lateinit var staffController: StaffController

    @Test
    fun `calls the service`() {
        staffController.getStaff("TEST", false)
        verify(staffService).getStaffInApprovedPremises("TEST", false, Pageable.ofSize(100))
    }
}
