package uk.gov.justice.digital.hmpps.exceptions

class InvalidEstablishmentCodeException(establishmentCode: String) :
    RuntimeException("Invalid establishment: $establishmentCode")
