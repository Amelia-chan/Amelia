package pw.mihou.amelia.models.interfaces

import org.bson.Document

interface ObjectModel<Self> {

    fun from(bson: Document): Self

}