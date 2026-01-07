package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager
import uk.gov.justice.digital.hmpps.integrations.delius.entity.OffenderManager

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        save(BusinessInteractionGenerator.CASE_NOTES_MERGE)

        save(CaseNoteTypeGenerator.DEFAULT)
        save(CaseNoteTypeGenerator.OTHER_INFORMATION)
        save(CaseNoteNomisTypeGenerator.NEG.type)
        save(CaseNoteNomisTypeGenerator.NEG)
        save(CaseNoteNomisTypeGenerator.RESETTLEMENT.type)
        save(CaseNoteNomisTypeGenerator.RESETTLEMENT)

        save(ProbationAreaGenerator.DEFAULT.institution!!)
        save(ProbationAreaGenerator.DEFAULT)
        save(TeamGenerator.DEFAULT)
        save(StaffGenerator.DEFAULT)

        save(OffenderGenerator.DEFAULT)
        save(OffenderGenerator.PREVIOUS)
        save(
            OffenderManager(
                OffenderGenerator.PREVIOUS,
                StaffGenerator.DEFAULT,
                TeamGenerator.DEFAULT,
                ProbationAreaGenerator.DEFAULT,
                true,
                false,
                IdGenerator.getAndIncrement()
            )
        )
        save(OffenderGenerator.NEW_IDENTIFIER)

        save(EventGenerator.CUSTODIAL_EVENT)
        save(EventGenerator.CUSTODIAL_EVENT.disposal!!.disposalType)
        save(EventGenerator.CUSTODIAL_EVENT.disposal!!)

        save(NsiGenerator.EVENT_CASE_NOTE_NSI.type)
        save(NomisNsiTypeGenerator.DEFAULT)
        save(NsiGenerator.EVENT_CASE_NOTE_NSI)

        save(CaseNoteGenerator.EXISTING)
    }
}
