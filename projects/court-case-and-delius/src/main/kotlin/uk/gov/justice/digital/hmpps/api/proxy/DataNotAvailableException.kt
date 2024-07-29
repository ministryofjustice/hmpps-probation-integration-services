package uk.gov.justice.digital.hmpps.api.proxy

class DataNotAvailableException(param: String) :
    RuntimeException("No Data available for param $param")