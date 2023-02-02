package uk.gov.justice.digital.hmpps.controller.casedetails

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import uk.gov.justice.digital.hmpps.controller.casedetails.entity.AliasEntity
import uk.gov.justice.digital.hmpps.controller.casedetails.entity.CaseAddress
import uk.gov.justice.digital.hmpps.controller.casedetails.entity.CaseEntity
import uk.gov.justice.digital.hmpps.controller.casedetails.entity.CasePersonalCircumstanceEntity
import uk.gov.justice.digital.hmpps.controller.casedetails.entity.CasePersonalContactEntity
import uk.gov.justice.digital.hmpps.controller.casedetails.entity.DisabilityEntity
import uk.gov.justice.digital.hmpps.controller.casedetails.model.Alias
import uk.gov.justice.digital.hmpps.controller.casedetails.model.CaseDetails
import uk.gov.justice.digital.hmpps.controller.casedetails.model.Disability
import uk.gov.justice.digital.hmpps.controller.casedetails.model.Language
import uk.gov.justice.digital.hmpps.controller.casedetails.model.MappaRegistration
import uk.gov.justice.digital.hmpps.controller.casedetails.model.PhoneNumber
import uk.gov.justice.digital.hmpps.controller.casedetails.model.RegisterFlag
import uk.gov.justice.digital.hmpps.controller.common.mapper.AddressMapper
import uk.gov.justice.digital.hmpps.controller.common.model.Address
import uk.gov.justice.digital.hmpps.controller.common.model.PersonalCircumstance
import uk.gov.justice.digital.hmpps.controller.common.model.PersonalContact
import uk.gov.justice.digital.hmpps.controller.common.model.Type

@Mapper(
    componentModel = "spring",
    uses = [
        CasePersonalCircumstanceMapper::class,
        CasePersonalContactMapper::class,
        AddressMapper::class,
        CaseAddressMapper::class,
        AliasMapper::class,
        DisabilityMapper::class
    ]
)
interface CaseMapper {
    @Mapping(source = "surname", target = "name.surname")
    @Mapping(source = "forename", target = "name.forename")
    @Mapping(source = "secondName", target = "name.middleName")
    @Mapping(source = "gender.description", target = "gender")
    @Mapping(source = "genderIdentity.description", target = "genderIdentity")
    @Mapping(source = "ethnicity.description", target = "ethnicity")
    fun convertToModel(case: CaseEntity): CaseDetails
}

fun CaseMapper.withAdditionalMappings(case: CaseEntity): CaseDetails {
    fun String.language(requiresInterpreter: Boolean) = Language(requiresInterpreter, this)

    val phoneNumbers = listOfNotNull(
        case.mobileNumber?.let { PhoneNumber("MOBILE", it) },
        case.telephoneNumber?.let { PhoneNumber("TELEPHONE", it) }
    )

    val model = convertToModel(case)

    return model.copy(
        phoneNumbers = phoneNumbers,
        mappaRegistration = populateMappaRegistration(case),
        registerFlags = populateRegisterFlags(case),
        language = case.primaryLanguage?.description?.language(case.requiresInterpreter ?: false)
    )
}

fun populateRegisterFlags(case: CaseEntity): List<RegisterFlag> {
    return case.registrations.map {
        RegisterFlag(
            it.type.code,
            it.type.description,
            it.type.riskColour
        )
    }
}

fun populateMappaRegistration(case: CaseEntity): MappaRegistration? {
    val mappaRegistrations =
        case.registrations.sortedByDescending { it.startDate }.stream().filter {
            it.type.code == "MAPPA"
        }

        return mappaRegistrations.findFirst().map {
            MappaRegistration(
                it.startDate,
                Type(it.level.code, it.level.description),
                Type(it.category.code, it.category.description)
            )
        }.orElse(null)
}

@Mapper(componentModel = "spring")
interface CasePersonalCircumstanceMapper {
    fun convertToModel(personalCircumstanceEntity: CasePersonalCircumstanceEntity): PersonalCircumstance
}

@Mapper(componentModel = "spring")
interface CaseAddressMapper {
    fun convertToModel(caseAddress: CaseAddress): Address
}

@Mapper(componentModel = "spring")
interface CasePersonalContactMapper {

    @Mapping(source = "surname", target = "name.surname")
    @Mapping(source = "forename", target = "name.forename")
    @Mapping(source = "middleName", target = "name.middleName")
    @Mapping(source = "address.telephoneNumber", target = "telephoneNumber")
    fun convertToModel(personalContactEntity: CasePersonalContactEntity): PersonalContact
}

@Mapper(componentModel = "spring")
interface AliasMapper {
    @Mapping(source = "surname", target = "name.surname")
    @Mapping(source = "forename", target = "name.forename")
    @Mapping(source = "secondName", target = "name.middleName") // TODO make this do both middlenames
    fun convertToModel(alias: AliasEntity): Alias
}

@Mapper(componentModel = "spring")
interface DisabilityMapper {
    fun convertToModel(disabilityEntity: DisabilityEntity): Disability
}
