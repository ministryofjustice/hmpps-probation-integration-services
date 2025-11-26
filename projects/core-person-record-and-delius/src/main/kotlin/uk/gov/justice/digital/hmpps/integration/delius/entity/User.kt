package uk.gov.justice.digital.hmpps.integration.delius.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Immutable
@Entity
@Table(name = "user_")
class User(
    @Id
    @Column(name = "user_id")
    val id: Long,
    @Column("distinguished_name")
    val distinguishedName: String
)