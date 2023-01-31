package uk.gov.justice.digital.hmpps.controller.common.mapper

import org.mapstruct.Mapper
import uk.gov.justice.digital.hmpps.controller.common.entity.AddressEntity
import uk.gov.justice.digital.hmpps.controller.common.model.Address

@Mapper(componentModel = "spring")
interface AddressMapper {
    fun convertToModel(addressEntity: AddressEntity): Address
}