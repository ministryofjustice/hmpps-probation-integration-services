package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integration.delius.EventRepository
import uk.gov.justice.digital.hmpps.integration.delius.entity.*
import uk.gov.justice.digital.hmpps.ldap.findByUsername
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.model.CourtAppearance
import uk.gov.justice.digital.hmpps.model.Offence
import uk.gov.justice.digital.hmpps.model.Provider
import uk.gov.justice.digital.hmpps.model.Team

@Service
class CaseDetailsService(
    private val comRepository: PersonManagerRepository,
    private val registrationRepository: RegistrationRepository,
    private val eventRepository: EventRepository,
    private val ldapTemplate: LdapTemplate
) {
    fun getSupervisions(crn: String): SupervisionResponse = with(comRepository.getForCrn(crn)) {
        return SupervisionResponse(
            communityManager = toCommunityManagerResponse(),
            mappaDetail = registrationRepository.findMappa(person.id).toMappaResponse(),
            supervisions = eventRepository.findByPersonIdOrderByConvictionDateDesc(person.id).toSupervisionResponse(),
            dynamicRisks = registrationRepository.findDynamicRiskRegistrations(person.id)
                .toDynamicRiskRegistrationResponse(),
            personStatus = registrationRepository.findPersonStatusRegistrations(person.id)
                .toPersonStatusRegistrationResponse(),
        )
    }

    private fun PersonManager.toCommunityManagerResponse(): Manager {
        staff.user?.apply {
            ldapTemplate.findByUsername<LdapUser>(username)?.let {
                email = it.email
                telephone = it.telephone
            }
        }
        return Manager(
            code = staff.code,
            name = staff.name(),
            username = staff.user?.username,
            email = staff.user?.email,
            telephoneNumber = staff.user?.telephone,
            team = Team(
                code = team.code,
                description = team.description,
                email = team.emailAddress,
                telephoneNumber = team.telephone,
                provider = Provider(provider.code, provider.description)
            )
        )
    }

    private fun RegistrationEntity?.toMappaResponse() = this?.let {
        MappaDetail(
            level = it.level?.code?.toMappaLevel(),
            levelDescription = it.level?.description,
            category = it.category?.code?.toMappaCategory(),
            categoryDescription = it.category?.description,
            startDate = it.date,
            reviewDate = it.reviewDate,
            notes = it.notes
        )
    }

    private fun List<RegistrationEntity>.toDynamicRiskRegistrationResponse() = this.map {
        DynamicRiskRegistration(
            code = it.type.code,
            description = it.type.description,
            startDate = it.date,
            reviewDate = it.reviewDate,
            notes = it.notes
        )
    }

    private fun List<RegistrationEntity>.toPersonStatusRegistrationResponse() = this.map {
        PersonStatusRegistration(
            code = it.type.code,
            description = it.type.description,
            startDate = it.date,
            reviewDate = it.reviewDate,
            notes = it.notes
        )
    }

    private fun List<Event>.toSupervisionResponse() = this.map { event ->
        Supervision(
            number = event.number.toInt(),
            active = event.active,
            date = event.convictionDate,
            sentence = event.disposal?.let { disposal ->
                Sentence(
                    description = disposal.type.description,
                    date = disposal.date,
                    length = disposal.length?.toInt(),
                    lengthUnits = disposal.lengthUnits?.let { LengthUnit.valueOf(it.description) },
                    custodial = disposal.type.isCustodial()
                )
            },
            mainOffence = event.mainOffence.let { Offence.of(it.date, it.count, it.offence) },
            additionalOffences = event.additionalOffences.map { Offence.of(it.date, it.count, it.offence) },
            courtAppearances = event.courtAppearances.map {
                CourtAppearance(
                    type = it.type.description,
                    date = it.date,
                    court = it.court.name,
                    plea = it.plea?.description
                )
            }
        )
    }
}