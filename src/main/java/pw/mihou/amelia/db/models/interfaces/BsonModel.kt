package pw.mihou.amelia.db.models.interfaces

import org.bson.Document

interface BsonModel {
    fun bson(): Document
}
