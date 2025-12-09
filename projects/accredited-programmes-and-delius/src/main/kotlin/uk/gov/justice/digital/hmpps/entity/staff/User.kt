package uk.gov.justice.digital.hmpps.entity.staff

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.NumericBooleanConverter

@Entity
@Immutable
@Table(name = "user_")
class User(
    @Id
    @Column(name = "user_id")
    val id: Long,

    @Column(name = "distinguished_name")
    val username: String,

    @OneToOne
    @JoinColumn(name = "staff_id")
    val staff: Staff? = null,

    @Column(name = "\"system_user\"", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val systemUser: Boolean = false,
)