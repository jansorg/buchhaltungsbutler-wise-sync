package dev.ja.wise.camt053

import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.JAXBException
import jakarta.xml.bind.Marshaller
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path


class Camt053Parser {
    /**
     * Parse a CAMT.053 formatted bank statement from the given input stream.
     *
     * @param inputStream input stream containing the CAMT.053 formatted bank statement
     * @return document holding CAMT.053 parsed bank statement
     * @throws JAXBException
     */
    @Throws(JAXBException::class)
    fun parse(inputStream: InputStream?): Document? {
        val jc: JAXBContext = JAXBContext.newInstance(Document::class.java)

        val unmarshaller = jc.createUnmarshaller()
        val camt053Document: Document? = unmarshaller.unmarshal(inputStream) as Document

        val marshaller = jc.createMarshaller()
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
        return camt053Document
    }
}

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val camt053Parser = Camt053Parser()
        val inputStream: InputStream = Files.newInputStream(Path.of(args[0]))
        val document = camt053Parser.parse(inputStream)
        println(document)
    }
}