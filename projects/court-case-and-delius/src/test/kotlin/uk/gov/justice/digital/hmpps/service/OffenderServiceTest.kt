package uk.gov.justice.digital.hmpps.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.DocumentEntityGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.CURRENTLY_MANAGED
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Disability
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.service.*
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class OffenderServiceTest {

    @Mock
    internal lateinit var disability: Disability

    @Mock
    internal lateinit var personMock: Person

    @Test
    fun `disability is inactive `() {
        whenever(disability.startDate).thenReturn(LocalDate.now().plusDays(1))
        assertEquals(false, disability.isActive())
    }

    @Test
    fun `disability is active with startdate `() {
        whenever(disability.startDate).thenReturn(LocalDate.now().minusDays(2))
        assertEquals(true, disability.isActive())
    }

    @Test
    fun `disability is active no finish date `() {
        whenever(disability.startDate).thenReturn(LocalDate.now().minusDays(2))
        whenever(disability.finishDate).thenReturn(null)
        assertEquals(true, disability.isActive())
    }

    @Test
    fun `disability is active with finish date in future `() {
        whenever(disability.startDate).thenReturn(LocalDate.now().minusDays(2))
        whenever(disability.finishDate).thenReturn(LocalDate.now().plusDays(2))
        assertEquals(disability.isActive(), true)
    }

    @Test
    fun `person has no middle names `() {
        val doc = DocumentEntityGenerator.generateDocument(1L, 1L, "DOC", "DOC")
        val person = PersonGenerator.generate("TEST", secondName = null, thirdName = null)
        assertEquals(null, person.toOffenderSummary(doc).middleNames)
    }

    @Test
    fun `person has no phone numbers `() {
        val doc = DocumentEntityGenerator.generateDocument(1L, 1L, "DOC", "DOC")
        val person = PersonGenerator.generate("TEST", mobileNumber = null, telephoneNumber = null)

        assertEquals(null, person.toOffenderSummary(doc).contactDetails.phoneNumbers)
    }

    @Test
    fun `person has no email address `() {
        val doc = DocumentEntityGenerator.generateDocument(1L, 1L, "DOC", "DOC")
        val person = PersonGenerator.generate("TEST", emailAddress = null)

        assertEquals(null, person.toOffenderSummary(doc).contactDetails.emailAddresses)
    }

    @Test
    fun `person has no title `() {
        val doc = DocumentEntityGenerator.generateDocument(1L, 1L, "DOC", "DOC")
        val person = PersonGenerator.generate("TEST", title = null)

        assertEquals(null, person.toOffenderSummary(doc).title)
    }

    @Test
    fun `person address has no type `() {

        val address = PersonGenerator.generateAddress(1L, false, type = null)
        whenever(personMock.addresses).thenReturn(listOf(address))
        assertEquals(null, personMock.toContactDetails().addresses?.get(0)?.type)
    }

    @Test
    fun `person alias has no middle names `() {
        val alias = PersonGenerator.generatePersonAlias(CURRENTLY_MANAGED, secondName = null, thirdName = null)
        whenever(personMock.offenderAliases).thenReturn(listOf(alias))
        assertEquals(null, personMock.toAliases()?.get(0)?.middleNames)
    }

    @Test
    fun `person has no aliases `() {
        val doc = DocumentEntityGenerator.generateDocument(1L, 1L, "DOC", "DOC")
        whenever(personMock.offenderAliases).thenReturn(emptyList())
        assertEquals(null, personMock.toAliases())
    }

    @Test
    fun `person detail has no tier `() {
        val doc = DocumentEntityGenerator.generateDocument(1L, 1L, "DOC", "DOC")
        val person = PersonGenerator.generate("TEST", currentTier = null)
        assertEquals(null, person.toOffenderDetail(doc).currentTier)
    }

    @Test
    fun `person detail has no title `() {
        val doc = DocumentEntityGenerator.generateDocument(1L, 1L, "DOC", "DOC")
        val person = PersonGenerator.generate("TEST", title = null)
        assertEquals(null, person.toOffenderDetail(doc).title)
    }

    @Test
    fun `person detail has no middle names `() {
        val doc = DocumentEntityGenerator.generateDocument(1L, 1L, "DOC", "DOC")
        val person = PersonGenerator.generate("TEST", secondName = null, thirdName = null)
        assertEquals(null, person.toOffenderDetail(doc).middleNames)
    }
}

