package ro.cipri.realmtree

import android.content.Context
import android.util.Log
import io.reactivex.Flowable
import io.reactivex.Observable
import io.realm.Realm
import io.realm.RealmResults
import io.realm.kotlin.delete
import io.realm.kotlin.where
import io.realm.rx.CollectionChange
import ro.cipri.realmtree.model.City

import java.io.IOException
import java.io.InputStream

class Repository(private val realm: Realm) {

    fun fillData(context: Context) {
        realm.executeTransactionAsync( { realm ->
//            try {
//
//                realm.deleteAll()
//                context.assets.open("cities.json").use { stream ->
//                    // Open a transaction to store items into the realm
//                    realm.createAllFromJson(City::class.java, stream)
//                }
//            } catch (e: IOException) {
//                Log.e("realm", "error adding data", e)
//                throw RuntimeException(e)
//            }
            realm.copyToRealmOrUpdate(City("Cluj",10))
            realm.copyToRealmOrUpdate(City("Baia Mare",10))

        }, {error -> Log.e("realm", "write error", error) })
    }

    fun getCitiesChangeset(): Observable<CollectionChange<RealmResults<City>>> {
        return realm.where<City>().findAllAsync().asChangesetObservable()
    }

    fun getCitiesFlowable(): Flowable<RealmResults<City>> {
        return realm.where<City>().findAllAsync().asFlowable()//.filter { list -> list.isLoaded}
    }

    fun getCity(name: String): City? {
        return realm.where<City>().endsWith("name", name).findFirst()
    }
}
