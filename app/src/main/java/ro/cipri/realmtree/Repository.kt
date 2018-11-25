package ro.cipri.realmtree

import android.content.Context
import android.util.Log
import io.reactivex.Flowable
import io.reactivex.Observable
import io.realm.Realm
import io.realm.RealmResults
import io.realm.kotlin.where
import io.realm.rx.CollectionChange
import ro.cipri.realmtree.model.City

import java.io.IOException

class Repository(private val realm: Realm) {

    fun fillData(context: Context) {
        realm.executeTransactionAsync( {
            try {
                it.deleteAll()
                context.assets.open("cities.json").use { stream ->
                    // Open a transaction to store items into the realm
                    it.createAllFromJson(City::class.java, stream)
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
            it.copyToRealmOrUpdate(City("Cluj",12))
            it.copyToRealmOrUpdate(City("Baia Mare",15))

            it.where<City>().lessThan("votes", 10).findAll().deleteAllFromRealm()

        }
    }


    fun getCitiesChangeset(): Observable<CollectionChange<RealmResults<City>>> {
        return realm.where<City>().findAllAsync().sort("name").asChangesetObservable()
    }

    fun getCitiesFlowable(): Flowable<RealmResults<City>> {
        return realm.where<City>().findAllAsync().asFlowable()//.filter { list -> list.isLoaded}
    }

    fun getCity(name: String): City? {
        return realm.where<City>().endsWith("name", name).findFirst()
    }
}
