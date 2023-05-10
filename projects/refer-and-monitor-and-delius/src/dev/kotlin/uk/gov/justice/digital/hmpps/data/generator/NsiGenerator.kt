package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.Dataset
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.Nsi
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.NsiManager
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.NsiOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.NsiStatus
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.NsiType
import uk.gov.justice.digital.hmpps.messaging.ReferralEndType
import uk.gov.justice.digital.hmpps.service.ContractTypeNsiType
import java.time.LocalDate
import java.time.ZonedDateTime

object NsiGenerator {
    val NSI_OUTCOME_DS = Dataset(Dataset.Code.NSI_OUTCOME.value, IdGenerator.getAndIncrement())
    val OUTCOMES = ReferralEndType.values().map { generateOutcome(it.outcome) }.associateBy { it.code }
    val TYPES = ContractTypeNsiType.MAPPING.values.map { generateType(it) }.associateBy { it.code }
    val INPROG_STATUS = generateStatus(
        NsiStatus.Code.IN_PROGRESS.value,
        ContactGenerator.TYPES[ContactType.Code.IN_PROGRESS.value]!!.id
    )
    val COMP_STATUS = generateStatus(
        NsiStatus.Code.END.value,
        ContactGenerator.TYPES[ContactType.Code.COMPLETED.value]!!.id
    )

    var END_PREMATURELY = generate(
        TYPES.values.first(),
        externalReference = "urn:hmpps:interventions-referral:68df9f6c-3fcb-4ec6-8fcf-96551cd9b080",
        eventId = SentenceGenerator.EVENT_WITH_NSI.id,
        rarCount = 3
    )

    var NO_APPOINTMENTS = generate(
        TYPES.values.first(),
        person = PersonGenerator.NO_APPOINTMENTS,
        externalReference = "urn:hmpps:interventions-referral:09c62549-bcd3-49a9-8120-7811b76925e5"
    )

    var FUZZY_SEARCH = generate(
        TYPES.values.toList()[3],
        PersonGenerator.FUZZY_SEARCH,
        referralDate = LocalDate.parse("2023-02-14"),
        eventId = 97
    )

    fun generate(
        type: NsiType,
        person: Person = PersonGenerator.DEFAULT,
        status: NsiStatus = INPROG_STATUS,
        statusDate: ZonedDateTime = ZonedDateTime.now().minusDays(7),
        referralDate: LocalDate = LocalDate.now().minusDays(21),
        externalReference: String? = null,
        eventId: Long? = null,
        requirementId: Long? = null,
        rarCount: Long? = null,
        providerId: Long = ProviderGenerator.INTENDED_PROVIDER.id,
        notes: String? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Nsi(
        person,
        type,
        status,
        statusDate = statusDate,
        referralDate = referralDate,
        externalReference = externalReference,
        eventId = eventId,
        intendedProviderId = providerId,
        requirementId = requirementId,
        rarCount = rarCount,
        notes = notes,
        id = id
    )

    fun generateType(code: String, id: Long = IdGenerator.getAndIncrement()) = NsiType(code, id)

    fun generateStatus(code: String, contactTypeId: Long, id: Long = IdGenerator.getAndIncrement()) =
        NsiStatus(code, contactTypeId, id)

    fun generateOutcome(
        code: String,
        description: String = "Description of $code",
        id: Long = IdGenerator.getAndIncrement()
    ) = NsiOutcome(code, description, id, NSI_OUTCOME_DS.id)

    fun generateManager(nsi: Nsi, startDateTime: ZonedDateTime = ZonedDateTime.now().minusDays(7)) = NsiManager(
        nsi,
        ProviderGenerator.INTENDED_PROVIDER.id,
        ProviderGenerator.INTENDED_TEAM.id,
        ProviderGenerator.INTENDED_STAFF.id,
        startDateTime
    )
}
