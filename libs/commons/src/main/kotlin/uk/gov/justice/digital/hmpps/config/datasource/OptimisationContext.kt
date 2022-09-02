package uk.gov.justice.digital.hmpps.config.datasource

object OptimisationContext {
    val offenderId: ThreadLocal<Long> = ThreadLocal()
}
