package uk.gov.justice.digital.hmpps.data.generatorimport

import uk.gov.justice.digital.hmpps.messaging.*
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

object CommonPlatformHearingGenerator {
    fun generate(hearing: Hearing = HearingGenerator.generate()): CommonPlatformHearing {
        return CommonPlatformHearing(hearing = hearing)
    }
}

object HearingGenerator {
    fun generate(
        id: String = UUID.randomUUID().toString(),
        courtCentre: CourtCentre = CourtCentreGenerator.generate(),
        type: HearingType = HearingTypeGenerator.generate(),
        jurisdictionType: String = "Magistrates",
        hearingDays: List<HearingDay> = listOf(HearingDayGenerator.generate()),
        prosecutionCases: List<ProsecutionCase> = listOf(ProsecutionCaseGenerator.generate())
    ): Hearing {
        return Hearing(
            id = id,
            courtCentre = courtCentre,
            type = type,
            jurisdictionType = jurisdictionType,
            hearingDays = hearingDays,
            prosecutionCases = prosecutionCases
        )
    }
}

object ProsecutionCaseGenerator {
    val DEFAULT = generate()
    fun generate(
        id: String = UUID.randomUUID().toString(),
        initiationCode: String? = "InitiationCode",
        prosecutionCaseIdentifier: ProsecutionCaseIdentifier = ProsecutionCaseIdentifierGenerator.generate(),
        defendants: List<Defendant> = listOf(DefendantGenerator.generate()),
        caseStatus: String? = "CaseStatus",
        caseMarkers: List<Any> = emptyList()
    ): ProsecutionCase {
        return ProsecutionCase(
            id = id,
            initiationCode = initiationCode,
            prosecutionCaseIdentifier = prosecutionCaseIdentifier,
            defendants = defendants,
            caseStatus = caseStatus,
            caseMarkers = caseMarkers
        )
    }
}

object ProsecutionCaseIdentifierGenerator {
    val DEFAULT = generate()
    fun generate(
        prosecutionAuthorityCode: String = "AuthorityCode",
        prosecutionAuthorityId: String = UUID.randomUUID().toString(),
        caseURN: String = "CASE" + (1000..9999).random()
    ): ProsecutionCaseIdentifier {
        return ProsecutionCaseIdentifier(
            prosecutionAuthorityCode = prosecutionAuthorityCode,
            prosecutionAuthorityId = prosecutionAuthorityId,
            caseURN = caseURN
        )
    }
}

object HearingTypeGenerator {
    val DEFAULT = generate()
    fun generate(
        id: String = UUID.randomUUID().toString(),
        description: String = "Hearing Type Description",
        welshDescription: String? = "Welsh Description"
    ): HearingType {
        return HearingType(
            id = id,
            description = description,
            welshDescription = welshDescription
        )
    }
}

object CourtCentreGenerator {
    val DEFAULT = generate()
    fun generate(
        id: String = UUID.randomUUID().toString(),
        code: String = "A00AA00",
        roomId: String = UUID.randomUUID().toString(),
        roomName: String = "Court Room Name"
    ): CourtCentre {
        return CourtCentre(
            id = id,
            code = code,
            roomId = roomId,
            roomName = roomName
        )
    }
}

object HearingDayGenerator {
    val DEFAULT = generate()
    fun generate(
        sittingDay: ZonedDateTime = ZonedDateTime.now(),
        listedDurationMinutes: Int = (30..120).random(),
        listingSequence: Int = 1
    ): HearingDay {
        return HearingDay(
            sittingDay = sittingDay,
            listedDurationMinutes = listedDurationMinutes,
            listingSequence = listingSequence
        )
    }
}

object PleaGenerator {
    val DEFAULT = generate()
    fun generate(
        pleaValue: String? = "GUILTY",
        pleaDate: LocalDate? = LocalDate.now()
    ): Plea {
        return Plea(
            pleaValue = pleaValue,
            pleaDate = pleaDate
        )
    }
}

object VerdictGenerator {
    val DEFAULT = generate()
    fun generate(
        verdictDate: LocalDate? = LocalDate.now(),
        verdictType: VerdictType = VerdictTypeGenerator.generate()
    ): Verdict {
        return Verdict(
            verdictDate = verdictDate,
            verdictType = verdictType
        )
    }
}

object VerdictTypeGenerator {
    val DEFAULT = generate()
    fun generate(
        description: String? = "GUILTY"
    ): VerdictType {
        return VerdictType(
            description = description
        )
    }
}

object DefendantGenerator {
    val DEFAULT = generate()
    fun generate(
        id: String = UUID.randomUUID().toString(),
        prosecutionCaseId: String = UUID.randomUUID().toString(),
        masterDefendantId: String = UUID.randomUUID().toString(),
        pncId: String = "PNC" + (1000000..9999999).random(),
        croNumber: String = "CRO" + (100000..999999).random(),
        offences: List<HearingOffence> = listOf(HearingOffenceGenerator.generate()),
        personDefendant: PersonDefendant? = PersonDefendantGenerator.generate()
    ): Defendant {
        return Defendant(
            id = id,
            offences = offences,
            prosecutionCaseId = prosecutionCaseId,
            personDefendant = personDefendant,
            legalEntityDefendant = null,
            masterDefendantId = masterDefendantId,
            pncId = pncId,
            croNumber = croNumber
        )
    }
}

object PersonDefendantGenerator {
    fun generate(personDetails: PersonDetails = PersonDetailsGenerator.generate()): PersonDefendant {
        return PersonDefendant(personDetails = personDetails)
    }
}

object PersonDetailsGenerator {
    fun generate(
        firstName: String = "John",
        lastName: String = "Doe",
        gender: String = "MALE",
        dateOfBirth: LocalDate = LocalDate.now().minusYears((18..70).random().toLong()),
        address: Address = AddressGenerator.generate(),
        contact: ContactDetails? = ContactDetailsGenerator.generate(),
        ethnicity: Ethnicity = EthnicityGenerator.generate()
    ): PersonDetails {
        return PersonDetails(
            gender = gender,
            lastName = lastName,
            firstName = firstName,
            dateOfBirth = dateOfBirth,
            address = address,
            contact = contact,
            ethnicity = ethnicity
        )
    }
}

object HearingOffenceGenerator {
    fun generate(
        id: String = UUID.randomUUID().toString(),
        offenceCode: String = "123",
        offenceTitle: String = "Sample Offence",
        judicialResults: List<JudicialResult> = listOf(JudicialResultGenerator.DEFAULT)
    ): HearingOffence {
        return HearingOffence(
            id = id,
            offenceDefinitionId = "123",
            offenceCode = offenceCode,
            offenceTitle = offenceTitle,
            wording = "Sample wording",
            offenceLegislation = "Some Legislation",
            listingNumber = 1,
            judicialResults = judicialResults,
            plea = PleaGenerator.DEFAULT
        )
    }
}

object JudicialResultGenerator {
    val DEFAULT = generate(label = "Remanded in custody")
    fun generate(label: String = "Judicial Result Label", isConvictedResult: Boolean = true): JudicialResult {
        return JudicialResult(
            isConvictedResult = isConvictedResult,
            label = label,
            judicialResultTypeId = "1234",
            resultText = "Result text"
        )
    }
}

object AddressGenerator {
    fun generate(
        address1: String = "Address1",
        address2: String = "Address2",
        address3: String = "Address3",
        address4: String = "Address4",
        address5: String = "Address5",
        postcode: String = "AB12 3CD"
    ): Address {
        return Address(
            address1 = address1,
            address2 = address2,
            address3 = address3,
            address4 = address4,
            address5 = address5,
            postcode = postcode
        )
    }
}

object ContactDetailsGenerator {
    fun generate(mobile: String = "07123456789"): ContactDetails {
        return ContactDetails(mobile = mobile)
    }
}

object EthnicityGenerator {
    fun generate(
        observedEthnicityDescription: String = "Ethnicity Description",
        selfDefinedEthnicityDescription: String = "Self Defined Ethnicity Description"
    ): Ethnicity {
        return Ethnicity(
            observedEthnicityDescription = observedEthnicityDescription,
            selfDefinedEthnicityDescription = selfDefinedEthnicityDescription
        )
    }
}