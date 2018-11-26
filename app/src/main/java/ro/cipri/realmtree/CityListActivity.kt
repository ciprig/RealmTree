package ro.cipri.realmtree

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_city_list.*
import kotlinx.android.synthetic.main.city_list.*

/**
 * An activity representing a list of Pings. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [CityDetailActivity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
class CityListActivity : AppCompatActivity() {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private var twoPane: Boolean = false
    private var disp = CompositeDisposable()
    private lateinit var repository: Repository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        repository = (applicationContext as CityApplication).repository;
        setContentView(R.layout.activity_city_list)

        setSupportActionBar(toolbar)
        toolbar.title = title

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()

            repository.addMore()
        }

        if (city_detail_container != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            twoPane = true
        }

        setupRecyclerView(city_list)
    }

    private lateinit var adapter: CityRecyclerViewAdapter

    private fun setupRecyclerView(recyclerView: RecyclerView) {

        adapter = CityRecyclerViewAdapter(repository, this, twoPane)
        recyclerView.adapter = adapter

        disp.add(repository.getCitiesChangeset().doOnNext {Log.i("realm",
            "$it ${it.collection.isLoaded} ${it.collection.isValid} ${it.collection.size}" )}
            .filter { list -> list.collection.isLoaded}
            .subscribe(adapter::updateValues))


//        disp.add(repository.getCitiesFlowable()
//            //.doOnNext( {Log.i("realm", "$it ${it.isLoaded} ${it.isValid} ${it.size}" )})
//            //.filter( { it.isLoaded})
//            .subscribe {
//                adapter.updateValues(it) })

    }

    override fun onDestroy() {
        disp.clear()
        super.onDestroy()
    }
}
