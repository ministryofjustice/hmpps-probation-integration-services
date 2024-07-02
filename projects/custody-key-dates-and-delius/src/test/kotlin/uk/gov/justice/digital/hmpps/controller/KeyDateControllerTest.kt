package uk.gov.justice.digital.hmpps.controller

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.messaging.Notifier

@ExtendWith(MockitoExtension::class)
internal class KeyDateControllerTest {

    @Mock
    lateinit var notifier: Notifier

    @InjectMocks
    lateinit var keyDateController: KeyDateController

    @Test
    fun `a call is made to the notifier`() {
        keyDateController.updateKeyDates(listOf("A0001AA", "A0002AA"), false)
        verify(notifier).requestBulkUpdate(listOf("A0001AA", "A0002AA"), false)
    }
}
