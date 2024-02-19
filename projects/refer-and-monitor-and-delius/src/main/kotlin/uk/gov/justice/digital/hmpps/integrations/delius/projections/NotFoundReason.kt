package uk.gov.justice.digital.hmpps.integrations.delius.projections

interface ContactNotFoundReason : NsiNotFoundReason {
    val softDeleted: Int?
}

interface NsiNotFoundReason {
    val nsiActive: Int?
    val nsiSoftDeleted: Int?
    val nsiLastUpdatedBy: String?
}
