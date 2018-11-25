package ro.cipri.realmtree

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.realm.RealmResults
import io.realm.rx.CollectionChange
import kotlinx.android.synthetic.main.city_list_content.view.*
import ro.cipri.realmtree.dummy.DummyContent
import ro.cipri.realmtree.model.City
import java.util.*
import io.realm.OrderedCollectionChangeSet


class CityRecyclerViewAdapter(
    private val parentActivity: CityListActivity,
    private val twoPane: Boolean) : RecyclerView.Adapter<CityRecyclerViewAdapter.ViewHolder>() {

    private var cities: RealmResults<City>? = null
    private val onClickListener: View.OnClickListener
    private val updateOnModification = true

    init {
        setHasStableIds(true)
        onClickListener = View.OnClickListener { v ->
            val item = v.tag as DummyContent.DummyItem
            if (twoPane) {
                val fragment = CityDetailFragment().apply {
                    arguments = Bundle().apply {
                        putString(CityDetailFragment.ARG_ITEM_ID, item.id)
                    }
                }
                parentActivity.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.city_detail_container, fragment)
                    .commit()
            } else {
                val intent = Intent(v.context, CityDetailActivity::class.java).apply {
                    putExtra(CityDetailFragment.ARG_ITEM_ID, item.id)
                }
                v.context.startActivity(intent)
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return cities?.get(position)?.name.hashCode().toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.city_list_content, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = cities?.get(position)
        holder.idView.text = item?.name
        holder.contentView.text = item?.votes.toString()

        with(holder.itemView) {
            tag = item
            setOnClickListener(onClickListener)
        }
    }

    override fun getItemCount():Int {
        return  cities.orEmpty().size
    }

    fun updateValues(list: RealmResults<City>) {
        this.cities = list
        notifyDataSetChanged()
    }

    fun updateValues(collectionChange: CollectionChange<RealmResults<City>>) {
        var changeSet = collectionChange.changeset
        cities = collectionChange.collection

        if (changeSet == null || changeSet.getState() === OrderedCollectionChangeSet.State.INITIAL) {
            notifyDataSetChanged()
            return
        }
        // For deletions, the adapter has to be notified in reverse order.
        val deletions = changeSet.getDeletionRanges()
        for (i in deletions.indices.reversed()) {
            val range = deletions[i]
            notifyItemRangeRemoved(range.startIndex + dataOffset(), range.length)
        }

        val insertions = changeSet.getInsertionRanges()
        for (range in insertions) {
            notifyItemRangeInserted(range.startIndex + dataOffset(), range.length)
        }

        if (!updateOnModification) {
            return
        }

        val modifications = changeSet.getChangeRanges()
        for (range in modifications) {
            notifyItemRangeChanged(range.startIndex + dataOffset(), range.length)
        }
    }

    private fun dataOffset() = 0;


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val idView: TextView = view.id_text
        val contentView: TextView = view.content
    }
}
