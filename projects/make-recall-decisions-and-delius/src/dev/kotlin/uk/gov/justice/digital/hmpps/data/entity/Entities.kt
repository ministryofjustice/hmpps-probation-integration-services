package uk.gov.justice.digital.hmpps.data.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
class ApprovedPremisesReferral(
    @Id val approvedPremisesReferralId: Long,
    val referralDate: LocalDate,
    val eventId: Long?
)

@Entity(name = "TestUser")
@Table(name = "user_")
class User(
    @Id
    @Column(name = "user_id")
    val userId: Long,
    val forename: String,
    val surname: String
)
