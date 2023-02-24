package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.Dataset
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.Nsi
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.NsiManager
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.NsiOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.NsiStatus
import uk.gov.justice.digital.hmpps.messaging.ReferralEndType
import uk.gov.justice.digital.hmpps.set
import java.time.ZonedDateTime

object NsiGenerator {
    val NSI_OUTCOME_DS = Dataset(Dataset.Code.NSI_OUTCOME.value, IdGenerator.getAndIncrement())
    val OUTCOMES = ReferralEndType.values().map { generateOutcome(it.outcome) }.associateBy { it.code }
    val INPROG_STATUS = generateStatus(
        NsiStatus.Code.IN_PROGRESS.value,
        ContactGenerator.TYPES[ContactType.Code.IN_PROGRESS.value]!!.id
    )
    val COMP_STATUS = generateStatus(
        NsiStatus.Code.END.value,
        ContactGenerator.TYPES[ContactType.Code.COMPLETED.value]!!.id
    )

    val END_PREMATURELY =
        generate(externalReference = "urn:hmpps:interventions-referral:68df9f6c-3fcb-4ec6-8fcf-96551cd9b080")

    fun generate(
        person: Person = PersonGenerator.DEFAULT,
        status: NsiStatus = INPROG_STATUS,
        statusDate: ZonedDateTime = ZonedDateTime.now().minusDays(7),
        referralDate: ZonedDateTime = ZonedDateTime.now().minusDays(21),
        externalReference: String? = null
    ) = Nsi(
        person,
        status,
        statusDate = statusDate,
        referralDate = referralDate,
        externalReference = externalReference
    )

    fun generateStatus(code: String, contactTypeId: Long, id: Long = IdGenerator.getAndIncrement()) =
        NsiStatus(code, contactTypeId, id)

    fun generateOutcome(
        code: String,
        description: String = "Description of $code",
        id: Long = IdGenerator.getAndIncrement()
    ) = NsiOutcome(code, description, id, NSI_OUTCOME_DS.id)

    fun generateManager(nsi: Nsi): NsiManager {
        val manager = NsiManager(nsi, 101, 102, 103)
        nsi.set("managers", listOf(manager))
        return manager
    }
}
