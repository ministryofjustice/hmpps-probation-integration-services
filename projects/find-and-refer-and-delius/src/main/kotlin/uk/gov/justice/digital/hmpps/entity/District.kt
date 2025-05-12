package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable

@Entity
@Immutable
class District(
    @Id
    @Column(name = "district_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "borough_id")
    val borough: Borough,
)
