package uk.gov.justice.digital.hmpps.xml

import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

object DocumentUtils {
    // Source filename has the following format 146_27072020_2578_B01OB00_ADULT_COURT_LIST_DAILY
    // These constants relate to that string
    private const val OU_CODE_LENGTH = 5
    private const val COURT_DETAIL_LENGTH = 7
    private const val OU_CODE_POSITION = 3
    private const val HEARING_DATE_POSITION = 1
    private const val SOURCE_FILE_NAME = "source_file_name"
    private const val SOURCE_FILE_NAME_EXPR = "//source_file_name/text()"
    private const val DELIMITER = "_"

    private val HEARING_DATE_FORMATTER = DateTimeFormatter.ofPattern("ddMMyyyy")

    fun getMessageDetail(documents: Element): MessageDetail? = getMessageDetailByXPath(documents)

    private fun getMessageDetailByXPath(documents: Element): MessageDetail? {
        // XPathFactory not thread safe so make one each time
        val xPath: XPath = XPathFactory.newInstance().newXPath()
        val exp = xPath.compile(SOURCE_FILE_NAME_EXPR)
        val nodeList = exp.evaluate(documents, XPathConstants.NODESET) as NodeList
        for (i in 0 until nodeList.length) {
            getMessageDetail(nodeList.item(i))?.let { messageDetail ->
                return messageDetail
            }
        }
        return null
    }

    private fun getMessageDetail(documents: Element): MessageDetail? {
        val sourceFileNameNodes = documents.getElementsByTagName(SOURCE_FILE_NAME)
        for (j in 0 until sourceFileNameNodes.length) {
            val sourceFileNameElement = sourceFileNameNodes.item(j) as Element
            val childTextNodes: NodeList = sourceFileNameElement.childNodes
            for (k in 0 until childTextNodes.length) {
                return getMessageDetail(childTextNodes.item(k))
            }
        }
        return null
    }

    private fun getMessageDetail(item: Node): MessageDetail? {
        val nodeValue = item.nodeValue
        val fileNameParts: Array<String> = nodeValue?.split(DELIMITER)?.toTypedArray() ?: emptyArray()
        if (fileNameParts.size <= 4 || fileNameParts[OU_CODE_POSITION].length < OU_CODE_LENGTH) {
            return null
        }

        val ouCode = fileNameParts[OU_CODE_POSITION].uppercase()
        val courtCode = ouCode.substring(0, OU_CODE_LENGTH)
        val hearingDate = LocalDate.parse(fileNameParts[HEARING_DATE_POSITION], HEARING_DATE_FORMATTER).format(DateTimeFormatter.ISO_DATE)
        return MessageDetail(courtCode, getRoomFromFileName(ouCode), hearingDate)
    }

    fun getFileName(documents: Element): String? {
        val xPath: XPath = XPathFactory.newInstance().newXPath()
        val exp = xPath.compile(SOURCE_FILE_NAME_EXPR)
        val nodeList = exp.evaluate(documents, XPathConstants.NODESET) as NodeList
        for (i in 0 until nodeList.length) {
            return nodeList.item(i).textContent
        }
        return null
    }

    private fun getRoomFromFileName(ouCode: String): Int {
        if (ouCode.length >= COURT_DETAIL_LENGTH) {
            return ouCode.substring(OU_CODE_LENGTH, OU_CODE_LENGTH + 2).toIntOrNull() ?: 0
        }
        return 0
    }
}