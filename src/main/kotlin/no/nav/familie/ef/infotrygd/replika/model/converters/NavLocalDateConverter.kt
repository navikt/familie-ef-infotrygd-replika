package no.nav.familie.ef.infotrygd.replika.model.converters

import jakarta.persistence.Converter

@Converter
class NavLocalDateConverter : AbstractNavLocalDateConverter("yyyyMMdd")
