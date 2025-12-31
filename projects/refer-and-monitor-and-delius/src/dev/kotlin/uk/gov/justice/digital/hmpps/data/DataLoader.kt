package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.audit.BusinessInteraction
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator.generateExclusion
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator.generateRestriction
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.DeliveryUnit
import java.time.LocalDate
import java.time.ZonedDateTime

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        saveAll(
            listOf(
                BusinessInteraction(
                    IdGenerator.getAndIncrement(),
                    BusinessInteractionCode.MANAGE_NSI.code,
                    ZonedDateTime.now()
                ),
                BusinessInteraction(
                    IdGenerator.getAndIncrement(),
                    BusinessInteractionCode.ADD_CONTACT.code,
                    ZonedDateTime.now()
                ),
                BusinessInteraction(
                    IdGenerator.getAndIncrement(),
                    BusinessInteractionCode.UPDATE_CONTACT.code,
                    ZonedDateTime.now()
                )
            )
        )
        save(SentenceGenerator.DEFAULT_DISPOSAL_TYPE)
        saveAll(ContactGenerator.TYPES.values)
        saveAll(ContactGenerator.OUTCOMES.values)
        save(ContactGenerator.ENFORCEMENT_ACTION)
        saveAll(NsiGenerator.TYPES.values)
        saveAll(listOf(NsiGenerator.INPROG_STATUS, NsiGenerator.COMP_STATUS))

        saveAll(ReferenceDataGenerator.allDatasets() + NsiGenerator.NSI_OUTCOME_DS)
        saveAll(ReferenceDataGenerator.allReferenceData() + NsiGenerator.OUTCOMES.values + NsiGenerator.WITHDRAWN_OUTCOMES.values)

        save(SentenceGenerator.MAIN_CAT_F)

        save(ProviderGenerator.NON_CRS_PROVIDER)
        save(ProviderGenerator.INACTIVE_PROVIDER)
        val provider = save(ProviderGenerator.INTENDED_PROVIDER)
        saveAll(
            listOf(
                ProviderGenerator.PROBATION_BOROUGH.let {
                    DeliveryUnit(
                        it.code,
                        it.description,
                        provider,
                        true,
                        it.id
                    )
                },
                ProviderGenerator.PRISON_BOROUGH.let {
                    DeliveryUnit(
                        it.code,
                        it.description,
                        provider,
                        false,
                        it.id
                    )
                }
            )
        )
        saveAll(listOf(ProviderGenerator.PROBATION_DISTRICT, ProviderGenerator.PRISON_DISTRICT))

        saveAll(
            listOf(
                ProviderGenerator.INTENDED_TEAM,
                ProviderGenerator.PROBATION_TEAM,
                ProviderGenerator.PRISON_TEAM
            )
        )
        saveAll(
            listOf(
                ProviderGenerator.INTENDED_STAFF,
                ProviderGenerator.JOHN_SMITH,
                ProviderGenerator.PRISON_MANAGER
            )
        )

        saveAll(ProviderGenerator.LOCATIONS + ProviderGenerator.DEFAULT_LOCATION)

        saveAll(
            listOf(
                PersonGenerator.DEFAULT,
                PersonGenerator.SENTENCED_WITHOUT_NSI,
                PersonGenerator.COMMUNITY_RESPONSIBLE,
                PersonGenerator.COMMUNITY_NOT_RESPONSIBLE,
                PersonGenerator.NO_APPOINTMENTS
            )
        )

        val roCom = PersonGenerator.generatePersonManager(
            PersonGenerator.COMMUNITY_RESPONSIBLE,
            ProviderGenerator.JOHN_SMITH,
            ProviderGenerator.PROBATION_TEAM
        )
        saveAll(
            listOf(
                roCom,
                PersonGenerator.generatePersonManager(
                    PersonGenerator.COMMUNITY_NOT_RESPONSIBLE,
                    ProviderGenerator.JOHN_SMITH,
                    ProviderGenerator.PROBATION_TEAM
                )
            )
        )

        val pom = save(
            PersonGenerator.generatePrisonManager(
                PersonGenerator.COMMUNITY_NOT_RESPONSIBLE,
                ProviderGenerator.PRISON_MANAGER,
                ProviderGenerator.PRISON_TEAM
            )
        )

        save(PersonGenerator.generateResponsibleOfficer(roCom))
        save(PersonGenerator.generateResponsibleOfficer(null, pom))

        save(ProviderGenerator.JOHN_SMITH_USER)

        saveAll(listOf(PersonGenerator.DEFAULT, PersonGenerator.SENTENCED_WITHOUT_NSI))

        saveAll(listOf(SentenceGenerator.EVENT_WITHOUT_NSI, SentenceGenerator.EVENT_WITH_NSI))
        saveAll(listOf(SentenceGenerator.SENTENCE_WITHOUT_NSI, SentenceGenerator.SENTENCE_WITH_NSI))
        save(SentenceGenerator.generateRequirement(SentenceGenerator.SENTENCE_WITHOUT_NSI))

        val rfn = save(SentenceGenerator.generateRequirement(SentenceGenerator.SENTENCE_WITH_NSI))
        val nsi = NsiGenerator.WITHDRAWN
        NsiGenerator.WITHDRAWN = save(
            NsiGenerator.generate(
                nsi.type,
                externalReference = nsi.externalReference,
                eventId = nsi.eventId,
                requirementId = rfn.id,
                rarCount = nsi.rarCount
            )
        )

        val nsiNa = NsiGenerator.NO_APPOINTMENTS
        NsiGenerator.NO_APPOINTMENTS = save(
            NsiGenerator.generate(
                nsiNa.type,
                nsiNa.person,
                externalReference = nsiNa.externalReference,
                eventId = nsiNa.eventId,
                rarCount = nsiNa.rarCount
            )
        )

        saveAll(
            listOf(
                NsiGenerator.generateManager(NsiGenerator.WITHDRAWN),
                NsiGenerator.generateManager(NsiGenerator.NO_APPOINTMENTS)
            )
        )

        val crsA = ContactGenerator.CRSAPT_NON_COMPLIANT
        ContactGenerator.CRSAPT_NON_COMPLIANT = save(
            ContactGenerator.generate(
                crsA.type,
                date = crsA.date,
                notes = crsA.notes,
                nsi = NsiGenerator.WITHDRAWN,
                rarActivity = crsA.rarActivity
            )
        )

        val crsB = ContactGenerator.CRSAPT_COMPLIANT
        ContactGenerator.CRSAPT_COMPLIANT = save(
            ContactGenerator.generate(
                crsB.type,
                date = crsB.date,
                notes = crsB.notes,
                nsi = NsiGenerator.WITHDRAWN,
                rarActivity = crsB.rarActivity,
                externalReference = crsB.externalReference
            )
        )

        val crsC = ContactGenerator.CRSAPT_NOT_ATTENDED
        ContactGenerator.CRSAPT_NOT_ATTENDED = save(
            ContactGenerator.generate(
                crsC.type,
                date = crsC.date,
                notes = crsC.notes,
                nsi = NsiGenerator.WITHDRAWN,
                rarActivity = crsC.rarActivity
            )
        )

        val crsD = ContactGenerator.CRSAPT_NO_SESSION
        ContactGenerator.CRSAPT_NO_SESSION = save(
            ContactGenerator.generate(
                crsD.type,
                date = crsD.date,
                notes = crsD.notes,
                nsi = NsiGenerator.WITHDRAWN,
                rarActivity = crsD.rarActivity,
                externalReference = crsD.externalReference
            )
        )

        save(PersonGenerator.FUZZY_SEARCH)
        save(NsiGenerator.FUZZY_SEARCH)
        save(NsiGenerator.generateManager(NsiGenerator.FUZZY_SEARCH))

        save(NsiGenerator.TERMINATED)

        save(UserGenerator.LIMITED_ACCESS_USER)
        saveAll(
            listOf(
                PersonGenerator.EXCLUSION,
                PersonGenerator.RESTRICTION,
                PersonGenerator.RESTRICTION_EXCLUSION
            )
        )

        save(LimitedAccessGenerator.EXCLUSION)
        save(LimitedAccessGenerator.RESTRICTION)
        save(generateExclusion(person = PersonGenerator.RESTRICTION_EXCLUSION))
        save(generateRestriction(person = PersonGenerator.RESTRICTION_EXCLUSION))

        saveAll(listOf(CaseDetailsGenerator.MINIMAL_PERSON, CaseDetailsGenerator.FULL_PERSON))
        saveAll(
            listOf(
                CaseDetailsGenerator.generateAddress(
                    ReferenceDataGenerator.ADDRESS_MAIN,
                    buildingName = "Some Building",
                    streetName = "Some Street",
                    postcode = "SB1 1SS"
                ),
                CaseDetailsGenerator.generateAddress(
                    ReferenceDataGenerator.ADDRESS_OTHER,
                    buildingName = "No Such Place",
                    postcode = "NS1 1SP"
                )
            )
        )
        saveAll(
            listOf(
                CaseDetailsGenerator.generateDisability(
                    ReferenceDataGenerator.DISABILITY1,
                    notes = "Some notes about the disability"
                ),
                CaseDetailsGenerator.generateDisability(ReferenceDataGenerator.DISABILITY2, softDeleted = true),
                CaseDetailsGenerator.generateDisability(
                    ReferenceDataGenerator.DISABILITY2,
                    endDate = LocalDate.now().minusDays(1)
                )
            )
        )

        save(SentenceGenerator.OFFENCE)
        save(SentenceGenerator.FULL_DETAIL_EVENT)
        save(SentenceGenerator.FULL_DETAIL_MAIN_OFFENCE)
        save(SentenceGenerator.FULL_DETAIL_SENTENCE)

        saveAll(
            listOf(
                ProviderGenerator.linkTeamAndOfficeLocation(
                    ProviderGenerator.INTENDED_TEAM, ProviderGenerator.DEFAULT_LOCATION
                ),
                ProviderGenerator.linkTeamAndOfficeLocation(
                    ProviderGenerator.PROBATION_TEAM, ProviderGenerator.DEFAULT_LOCATION
                )
            )
        )
    }
}
