package no.nav.familie.ef.infotrygd.replika.model.converters

import jakarta.persistence.AttributeConverter

class ReversedLongFoedselNrConverter : AttributeConverter<String?, Long?> {
    private val converter = ReversedFoedselNrConverter()

    override fun convertToDatabaseColumn(attribute: String?): Long? = converter.convertToDatabaseColumn(attribute)?.toLong() ?: 0

    override fun convertToEntityAttribute(dbData: Long?): String? =
        converter.convertToEntityAttribute(dbData?.toString()?.padStart(11, '0'))
}
