package ro.cipri.realmtree

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.reactivex.disposables.Disposable
import io.realm.OrderedCollectionChangeSet
import io.realm.RealmResults
import io.realm.rx.CollectionChange
import kotlinx.android.synthetic.main.city_list_content.view.*
import ro.cipri.realmtree.model.City
import java.util.*


class CityRecyclerViewAdapter(
    private val repository: Repository,
    private val parentActivity: CityListActivity,
    private val twoPane: Boolean
) : RecyclerView.Adapter<CityRecyclerViewAdapter.ViewHolder>() {

    private var cities: List<City> = Collections.emptyList()

    //    private val onClickListener: View.OnClickListener
    private val updateOnModification = false

    init {
        setHasStableIds(true)
//        onClickListener = View.OnClickListener { v ->
//            val item = v.tag as DummyContent.DummyItem
//            if (twoPane) {
//                val fragment = CityDetailFragment().apply {
//                    arguments = Bundle().apply {
//                        putString(CityDetailFragment.ARG_ITEM_ID, item.id)
//                    }
//                }
//                parentActivity.supportFragmentManager
//                    .beginTransaction()
//                    .replace(R.id.city_detail_container, fragment)
//                    .commit()
//            } else {
//                val intent = Intent(v.context, CityDetailActivity::class.java).apply {
//                    putExtra(CityDetailFragment.ARG_ITEM_ID, item.id)
//                }
//                v.context.startActivity(intent)
//            }
//        }
    }

    override fun getItemId(position: Int): Long = cities[position].name.hashCode().toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.city_list_content, parent, false)

//        view.setOnClickListener(onClickListener)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.setCity(cities[position])

    override fun getItemCount() = cities.size

    fun updateValues(list: RealmResults<City>) {
        this.cities = list
        notifyDataSetChanged()
    }

    fun updateValues(collectionChange: CollectionChange<RealmResults<City>>) {
        val changeSet = collectionChange.changeset
        cities = collectionChange.collection

        if (changeSet == null || changeSet.state === OrderedCollectionChangeSet.State.INITIAL) {
            notifyDataSetChanged()
            return
        }
        // For deletions, the adapter has to be notified in reverse order.
        val deletions = changeSet.deletionRanges
        for (i in deletions.indices.reversed()) {
            val range = deletions[i]
            notifyItemRangeRemoved(range.startIndex + dataOffset(), range.length)
        }

        val insertions = changeSet.insertionRanges
        for (range in insertions) {
            notifyItemRangeInserted(range.startIndex + dataOffset(), range.length)
        }

        if (!updateOnModification) {
            return
        }

        val modifications = changeSet.changeRanges
        for (range in modifications) {
            notifyItemRangeChanged(range.startIndex + dataOffset(), range.length)
        }
    }

    private fun dataOffset() = 0;

    override fun onViewRecycled(holder: ViewHolder) = holder.dispose()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val idView: TextView = view.id_text
        val contentView: TextView = view.content
        val personRecyle: RecyclerView = view.person_list

        val adapter = PersonRecyclerViewAdapter()

        var disposable: Disposable? = null

        init {
            personRecyle.adapter = adapter
        }

        fun setCity(city: City) {
            idView.text = city.name
            contentView.text = city.votes.toString()
            itemView.tag = city

            disposable = repository.getPersonsChangeset(city).subscribe(adapter::updateValues)
        }

        fun dispose() {
            disposable?.dispose()
            //adapter.clear()
        }
    }
}
