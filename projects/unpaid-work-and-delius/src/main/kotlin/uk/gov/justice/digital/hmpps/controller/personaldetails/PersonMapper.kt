package uk.gov.justice.digital.hmpps.controller.personaldetails
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import uk.gov.justice.digital.hmpps.controller.personaldetails.entity.Person
import uk.gov.justice.digital.hmpps.controller.personaldetails.entity.PersonalCircumstanceEntity
import uk.gov.justice.digital.hmpps.controller.personaldetails.entity.PersonalContactEntity
import uk.gov.justice.digital.hmpps.controller.personaldetails.model.PersonalDetails
import uk.gov.justice.digital.hmpps.integrations.common.mapper.AddressMapper
import uk.gov.justice.digital.hmpps.integrations.common.model.PersonalCircumstance
import uk.gov.justice.digital.hmpps.integrations.common.model.PersonalContact

@Mapper(componentModel = "spring", uses = [PersonalCircumstanceMapper::class, PersonalContactMapper::class, AddressMapper::class])
interface PersonMapper {
    fun convertToModel(person: Person): PersonalDetails
}

@Mapper(componentModel = "spring")
interface PersonalCircumstanceMapper {
    fun convertToModel(personalCircumstanceEntity: PersonalCircumstanceEntity): PersonalCircumstance
}

@Mapper(componentModel = "spring")
interface PersonalContactMapper {
    @Mapping(source = "surname", target = "name.surname")
    @Mapping(source = "forename", target = "name.forename")
    @Mapping(source = "middleName", target = "name.middleName")
    @Mapping(source = "address.telephoneNumber", target = "telephoneNumber")
    fun convertToModel(personalContactEntity: PersonalContactEntity): PersonalContact
}
