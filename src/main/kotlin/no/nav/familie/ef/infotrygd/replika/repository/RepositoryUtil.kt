package no.nav.familie.ef.infotrygd.replika.repository

import java.sql.ResultSet

object RepositoryUtil {
    fun ResultSet.getNullableInt(columnName: String): Int? {
        val value = this.getInt(columnName)
        return if (this.wasNull()) null else value
    }
}
