package uk.gov.justice.digital.hmpps.integrations.common.mapper

import org.mapstruct.Mapper
import uk.gov.justice.digital.hmpps.integrations.common.entity.AddressEntity
import uk.gov.justice.digital.hmpps.integrations.common.model.Address

@Mapper(componentModel = "spring")
fun interface AddressMapper {
    fun convertToModel(addressEntity: AddressEntity): Address
}
