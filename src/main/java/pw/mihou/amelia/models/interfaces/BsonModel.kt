package pw.mihou.amelia.models.interfaces

import org.bson.Document

interface BsonModel {


    fun bson(): Document

}