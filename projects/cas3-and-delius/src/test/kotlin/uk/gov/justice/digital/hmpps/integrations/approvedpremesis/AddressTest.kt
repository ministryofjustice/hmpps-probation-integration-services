package uk.gov.justice.digital.hmpps.integrations.approvedpremesis

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class AddressTest {

    @ParameterizedTest
    @MethodSource("addressLines")
    fun testAddressLines(address: Address, addressLines: AddressLines) {
        assertThat(address.addressLines, equalTo(addressLines))
    }

    companion object {

        private val address = Address(" ", null, "NN1 1NN", "town", "region")
        private const val thirtyFive = "9 this is quite a long address line"
        private const val extension = " with an even longer bit on the end"
        private const val seventy = thirtyFive + extension

        @JvmStatic
        fun addressLines() = listOf(
            Arguments.of(address.copy(addressLine1 = thirtyFive), AddressLines(null, thirtyFive, null)),
            Arguments.of(
                address.copy(addressLine1 = thirtyFive, addressLine2 = extension),
                AddressLines(null, thirtyFive, extension)
            ),
            Arguments.of(address.copy(addressLine1 = seventy), AddressLines(null, thirtyFive, extension)),
            Arguments.of(
                address.copy(addressLine1 = seventy, addressLine2 = "with an extra line"),
                AddressLines(thirtyFive, extension, "with an extra line")
            )
        )
    }
}
