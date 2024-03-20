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
        staff.user?.apply {
            ldapTemplate.findByUsername<LdapUser>(username)?.let {
                email = it.email
                telephone = it.telephone
            }
        }
        val supervisions = eventRepository.findByPersonIdOrderByConvictionDateDesc(person.id).map { event ->
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
        val mappaDetail = registrationRepository.findMappa(person.id)?.let {
            MappaDetail(
                it.level?.code?.toMappaLevel(),
                it.level?.description,
                it.category?.code?.toMappaCategory(),
                it.category?.description,
                it.date,
                it.reviewDate,
                it.notes
            )
        }
        return SupervisionResponse(asCom(), mappaDetail, supervisions)
    }
}

enum class Category(val number: Int) { X9(0), M1(1), M2(2), M3(3), M4(4) }
enum class Level(val number: Int) { M0(0), M1(1), M2(2), M3(3) }

private fun String.toMappaLevel() = Level.entries.find { it.name == this }?.number
    ?: throw IllegalStateException("Unexpected MAPPA level: $this")

private fun String.toMappaCategory() = Category.entries.find { it.name == this }?.number
    ?: throw IllegalStateException("Unexpected MAPPA category: $this")

private fun PersonManager.asCom() = Manager(
    staff.code,
    staff.name(),
    staff.user?.username,
    staff.user?.email,
    staff.user?.telephone,
    team.asModel(provider())
)

private fun PersonManager.provider() = Provider(provider.code, provider.description)

private fun uk.gov.justice.digital.hmpps.integration.delius.entity.Team.asModel(provider: Provider) =
    Team(code, description, emailAddress, telephone, provider)