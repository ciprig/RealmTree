package ro.cipri.realmtree.model


import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass


open class Person(): RealmObject() {
    @PrimaryKey
    var name: String? = null

    var age: Int? = null

    var address: Address? = null

    constructor(name: String, age: Int) : this() {
        this.name = name
        this.age = age
        this.address = Address("Street $name", age / 10)
    }

    override fun toString(): String {
        return "Person{name=$name, age=$age, address=$address}"
    }
}

