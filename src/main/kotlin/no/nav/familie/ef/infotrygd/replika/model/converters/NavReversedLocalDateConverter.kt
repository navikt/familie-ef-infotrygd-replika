package no.nav.familie.ef.infotrygd.replika.model.converters

import jakarta.persistence.Converter

@Converter
class NavReversedLocalDateConverter : AbstractNavLocalDateConverter("ddMMyyyy")
