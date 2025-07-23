package uk.gov.justice.digital.hmpps.crimeportalgateway.model.externaldocumentrequest

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import java.util.Objects
import java.util.stream.Collectors
import java.util.stream.Stream

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Name(
    val title: String? = null,
    val forename1: String? = null,
    val forename2: String? = null,
    val forename3: String? = null,
    val surname: String? = null,
) {
    @JsonIgnore
    fun getForenames(): String =
        Stream
            .of(forename1, forename2, forename3)
            .filter { obj: String? ->
                Objects.nonNull(
                    obj,
                )
            }.collect(Collectors.joining(" "))
            .trim { it <= ' ' }

    @JsonIgnore
    fun getFullName(): String =
        Stream
            .of(title, forename1, forename2, forename3, surname)
            .filter { obj: String? ->
                Objects.nonNull(
                    obj,
                )
            }.collect(Collectors.joining(" "))
            .trim { it <= ' ' }
}
