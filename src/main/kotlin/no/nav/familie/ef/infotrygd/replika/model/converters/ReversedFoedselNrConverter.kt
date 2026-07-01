package no.nav.familie.ef.infotrygd.replika.model.converters

import jakarta.persistence.AttributeConverter
import no.nav.familie.ef.infotrygd.replika.utils.reverserFnr

class ReversedFoedselNrConverter : AttributeConverter<String?, String?> {
    override fun convertToDatabaseColumn(attribute: String?): String? = attribute?.reverserFnr() ?: "00000000000"

    override fun convertToEntityAttribute(dbData: String?): String? {
        if (dbData == null) {
            return null
        }

        if (dbData.toLong() == 0L) {
            return null
        }

        return dbData.reverserFnr()
    }
}
