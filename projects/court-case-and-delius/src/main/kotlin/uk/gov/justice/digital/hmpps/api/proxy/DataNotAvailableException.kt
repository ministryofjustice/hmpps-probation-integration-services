package uk.gov.justice.digital.hmpps.api.proxy

class DataNotAvailableException(param: String) :
    RuntimeException("No Data available for param $param")

class ComparisonException(message: String?) :
    RuntimeException("Calling new api failed with $message")