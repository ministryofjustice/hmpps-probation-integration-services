package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Immutable
@Entity
@Table(name = "district")
class District(
    @Id
    @Column(name = "district_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "borough_id")
    val borough: Pdu,
)

