package ro.cipri.realmtree.model

import io.realm.RealmObject

open class Address(): RealmObject() {


    var street: String? = null

    var number: Int? = null

    constructor(street: String, number: Int) : this() {
        this.street = street
        this.number = number
    }

    override fun toString(): String {
        return "$street no $number"
    }
}
