package uk.gov.justice.digital.hmpps.entity

import java.sql.Clob

interface ContactJson {
    val contactId: Long
    val json: Clob
}