package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.epf.entity.PersonManager
import uk.gov.justice.digital.hmpps.epf.entity.PrisonManager
import uk.gov.justice.digital.hmpps.epf.entity.Provider
import uk.gov.justice.digital.hmpps.epf.entity.ResponsibleOfficer
import java.time.ZonedDateTime

object ManagerGenerator {

    val DEFAULT_PERSON_MANAGER = personManagerGenerator()
    val DEFAULT_RESPONSIBLE_OFFICER =
        responsibleOfficerGenerator(communityManager = DEFAULT_PERSON_MANAGER, prisonManager = null, endDate = null)

    fun responsibleOfficerGenerator(
        id: Long = IdGenerator.getAndIncrement(),
        communityManager: PersonManager?,
        prisonManager: PrisonManager?,
        endDate: ZonedDateTime?
    ) = ResponsibleOfficer(id, PersonGenerator.DEFAULT.id, communityManager, prisonManager, endDate)

    fun personManagerGenerator(
        provider: Provider = ProviderGenerator.DEFAULT,
        id: Long = IdGenerator.getAndIncrement()
    ) = PersonManager(provider, id = id)
}

object ProviderGenerator {
    val DEFAULT = Provider(
        "N02",
        "NPS North East",
        IdGenerator.getAndIncrement()
    )
}
