package com.phinion.dailyjournal.model

import com.phinion.dailyjournal.util.toRealmInstant
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.ObjectId
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import java.time.Instant

open class Diary: RealmObject {
    //This will generate a unique identifier for each and every item
    @PrimaryKey
    var _id: ObjectId = ObjectId.create()
    //Unique for each person
    var ownerId: String = ""
    //From the name we will get the name of the constant itself for eg in this case we will get Neutral as a string
    var mood: String = Mood.Neutral.name
    var title: String = ""
    var description: String = ""
    var images: RealmList<String> = realmListOf()
    var date: RealmInstant = Instant.now().toRealmInstant()

}