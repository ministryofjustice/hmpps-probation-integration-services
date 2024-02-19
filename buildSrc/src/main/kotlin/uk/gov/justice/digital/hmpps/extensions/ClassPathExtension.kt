package uk.gov.justice.digital.hmpps.extensions

abstract class ClassPathExtension(
    var jacocoExclusions: List<String>,
    var sonarExclusions: List<String>
)