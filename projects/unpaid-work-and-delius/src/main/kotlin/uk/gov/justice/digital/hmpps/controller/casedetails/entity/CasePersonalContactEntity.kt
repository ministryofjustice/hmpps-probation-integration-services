package uk.gov.justice.digital.hmpps.controller.casedetails.entity

import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import uk.gov.justice.digital.hmpps.integrations.common.entity.AddressEntity
import uk.gov.justice.digital.hmpps.integrations.common.entity.PersonalContactBase
import uk.gov.justice.digital.hmpps.integrations.common.entity.ReferenceData
import java.time.LocalDate

@Entity
@Immutable
@Table(name = "personal_contact")
@SQLRestriction("soft_deleted = 0 and (end_date is null or end_date > current_date)")
class CasePersonalContactEntity(
    id: Long = 0,
    @ManyToOne
    @JoinColumn(name = "offender_id", nullable = false)
    val case: CaseEntity,
    relationship: String,
    relationshipType: ReferenceData,
    forename: String,
    middleName: String?,
    surname: String,
    mobileNumber: String?,
    address: AddressEntity?,
    start: LocalDate? = null,
    endDate: LocalDate? = null,
    softDeleted: Boolean = false,
) : PersonalContactBase(
        id,
        relationship,
        relationshipType,
        forename,
        middleName,
        surname,
        mobileNumber,
        address,
        start,
        endDate,
        softDeleted,
    )
