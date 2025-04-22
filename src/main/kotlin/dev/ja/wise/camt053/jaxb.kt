package dev.ja.wise.camt053

import dev.ja.model.Currency
import jakarta.xml.bind.annotation.adapters.XmlAdapter
import kotlinx.datetime.Instant
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList

class InstantAdapter : XmlAdapter<String, Instant>() {
    @Throws(Exception::class)
    override fun marshal(v: Instant): String {
        return v.format(DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET)
    }

    @Throws(Exception::class)
    override fun unmarshal(v: String): Instant {
        return Instant.parse(v, DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET)
    }
}

class CurrencyAdapter : XmlAdapter<String, Currency>() {
    @Throws(Exception::class)
    override fun marshal(v: Currency): String {
        return v.id
    }

    @Throws(Exception::class)
    override fun unmarshal(v: String): Currency {
        return Currency.of(v)
    }
}

fun List<Element>.jaxbQuery(vararg path: String): Any? {
    var choices: List<Node> = this
    var pathIndex = 0
    pathLoop@ for (p in path) {
        val isLastPath = pathIndex++ == path.size - 1

        for (e in choices) {
            if (p == e.localName) {
                if (isLastPath) {
                    return when (e) {
                        is Element -> e.textContent
                        else -> e.nodeValue
                    }
                }
                choices = e.childNodes.asList()
                continue@pathLoop
            }
        }
    }

    return null
}

private fun NodeList.asList(): List<Node> {
    val list = mutableListOf<Node>()
    for (i in 0 until length) {
        list.add(item(i))
    }
    return list
}