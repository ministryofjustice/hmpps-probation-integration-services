package uk.gov.justice.digital.hmpps.integrations.delius.manager

abstract class Manager {
    abstract val staffId: Long
    abstract val teamId: Long
    abstract val probationAreaId: Long
}
