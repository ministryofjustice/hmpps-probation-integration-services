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
import uk.gov.justice.digital.hmpps.controller.common.entity.ReferenceData
import uk.gov.justice.digital.hmpps.controller.common.mapper.AddressMapper
import uk.gov.justice.digital.hmpps.controller.common.model.Address
import uk.gov.justice.digital.hmpps.controller.common.model.PersonalCircumstance
import uk.gov.justice.digital.hmpps.controller.common.model.PersonalContact

@Mapper(
    componentModel = "spring",
    uses = [
        CasePersonalCircumstanceMapper::class,
        CasePersonalContactMapper::class,
        AddressMapper::class,
        CaseAddressMapper::class,
        AliasMapper::class,
        DisabilityMapper::class,
        LanguageMapper::class
    ]
)
interface CaseMapper {
    @Mapping(source = "surname", target = "name.surname")
    @Mapping(source = "forename", target = "name.forename")
    @Mapping(source = "secondName", target = "name.middleName")
    @Mapping(source = "gender.description", target = "gender")
    @Mapping(source = "genderIdentity.description", target = "genderIdentity")
    @Mapping(source = "ethnicity.description", target = "ethnicity")
    // @Mapping(source = "emailAddress", target = "emailAddresses") TODO how to map single to list?
    fun convertToModel(case: CaseEntity): CaseDetails
}

interface LanguageMapper {
    @Mapping(source = "primaryLanguage.description", target = "primaryLanguage")
    @Mapping(source = "requiresInterpreter", target = "requiresInterpreter")
    fun convertToModel(primaryLanguage: ReferenceData, requiresInterpreter: Boolean): Language
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
