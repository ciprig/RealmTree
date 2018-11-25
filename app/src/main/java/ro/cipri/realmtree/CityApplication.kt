package ro.cipri.realmtree

import android.app.Application
import io.realm.Realm

class CityApplication : Application() {

    val repository: Repository by lazy {
        var repo = Repository(Realm.getDefaultInstance());
        repo.fillData(this)

        repo
    }

    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
    }
}
