package uk.gov.justice.digital.hmpps.datasource

object OptimisationContext {
    val offenderId: ThreadLocal<Long> = ThreadLocal()
}
