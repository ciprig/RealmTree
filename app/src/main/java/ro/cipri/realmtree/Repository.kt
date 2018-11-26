package ro.cipri.realmtree

import android.content.Context
import android.util.Log
import io.reactivex.Flowable
import io.reactivex.Observable
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmResults
import io.realm.kotlin.where
import io.realm.rx.CollectionChange
import ro.cipri.realmtree.model.City
import ro.cipri.realmtree.model.Person

import java.io.IOException

class Repository(private val realm: Realm) {

    fun fillData(context: Context) {
        realm.executeTransactionAsync( {
            try {
                it.deleteAll()
                context.assets.open("cities.json").use { stream ->
                    // Open a transaction to store items into the realm
                    it.createOrUpdateAllFromJson(City::class.java, stream)
                }
            } catch (e: IOException) {
                Log.e("realm", "error adding data", e)
                throw RuntimeException(e)
            }
        }, {error -> Log.e("realm", "write error", error) })
    }

    fun addMore() {
        realm.executeTransactionAsync {
            Thread.sleep(2000)
            var cluj = it.copyToRealmOrUpdate(City("Cluj", 20))
            cluj.persons?.add(Person("Cristi", 42));

            it.copyToRealmOrUpdate(City("Baia Mare", 21))

            var melborne = it.where<City>().equalTo("name", "Melbourne").findFirst()
            melborne?.persons?.add(1, Person("Ollie", 25))


            var barcelona = it.where<City>().equalTo("name", "Barcelona").findFirst()
            barcelona?.persons?.add(1, Person("Adi", 25))
            barcelona?.persons?.add(0, Person("Ollie", 25))

            it.where<City>().lessThan("votes", 15).findAll().deleteAllFromRealm()

        }
    }

    fun getCitiesChangeset(): Observable<CollectionChange<RealmResults<City>>> {
        return realm.where<City>()
            .isNotEmpty("persons")
            .sort("name")
            .findAllAsync()
            .asChangesetObservable()
            .filter { it.collection.isValid }
    }

    fun getCitiesFlowable(): Flowable<RealmResults<City>> {
        return realm.where<City>().findAllAsync().asFlowable()//.filter { list -> list.isLoaded}
    }

    fun getCity(name: String): City? {
        return realm.where<City>().endsWith("name", name).findFirst()
    }

    fun getPersonsChangeset(city: City): Observable<CollectionChange<RealmList<Person>>> {
//        return realm.where<City>()
//            .equalTo("name", city.name)
//            .findFirstAsync()
//            .asFlowable<City>()
//            .filter { it.isLoaded }
//            .firstElement()
//            .flatMapObservable {it.persons!!.asChangesetObservable()}

        return city.persons!!
            .asChangesetObservable()
            .doOnNext { Log.i("realm", "persons: ${it.collection} ${it.collection.isValid} ${it.collection.isLoaded}") }
    }
}
