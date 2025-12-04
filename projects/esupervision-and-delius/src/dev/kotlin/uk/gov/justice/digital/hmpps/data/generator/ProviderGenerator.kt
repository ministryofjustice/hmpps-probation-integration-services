package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.*

object ProviderGenerator {
    val DEFAULT_PROVIDER = generateProvider("DEF")
    val DEFAULT_PDU = generatePdu("PDU")
    val DEFAULT_LDU = generateLdu("LDU")
    val DEFAULT_TEAM = generateTeam("DEFTE1")
    val DEFAULT_STAFF = generateStaff("DEFTE1S", "John", "Smith")
    val DEFAULT_STAFF_USER = generateStaffUser("john-smith", DEFAULT_STAFF)

    fun generateProvider(
        code: String,
        description: String = "Provider of $code",
        id: Long = IdGenerator.getAndIncrement()
    ) = Provider(code, description, id)

    fun generatePdu(
        code: String,
        description: String = "PDU of $code",
        id: Long = IdGenerator.getAndIncrement()
    ) = Pdu(code, description, id)

    fun generateLdu(
        code: String,
        description: String = "LDU of $code",
        pdu: Pdu = DEFAULT_PDU,
        id: Long = IdGenerator.getAndIncrement()
    ) = Ldu(code, description, pdu, id)

    fun generateTeam(
        code: String,
        description: String = "Team of $code",
        ldu: Ldu = DEFAULT_LDU,
        id: Long = IdGenerator.getAndIncrement()
    ) = Team(code, description, ldu, id)

    fun generateStaff(
        code: String,
        forename: String = "first$code",
        surname: String = "second$code",
        id: Long = IdGenerator.getAndIncrement()
    ) = Staff(code, forename, surname, null, id)

    fun generateStaffUser(
        username: String,
        staff: Staff,
        id: Long = IdGenerator.getAndIncrement()
    ) = StaffUser(username, staff, id)
}