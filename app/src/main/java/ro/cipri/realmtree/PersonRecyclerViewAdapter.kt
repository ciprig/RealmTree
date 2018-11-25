package ro.cipri.realmtree

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.realm.RealmResults
import io.realm.rx.CollectionChange
import kotlinx.android.synthetic.main.city_list_content.view.*
import ro.cipri.realmtree.model.City
import java.util.*
import io.realm.OrderedCollectionChangeSet
import ro.cipri.realmtree.model.Person


class PersonRecyclerViewAdapter(
    private val parentActivity: CityListActivity,
    private val twoPane: Boolean) : RecyclerView.Adapter<PersonRecyclerViewAdapter.ViewHolder>() {

    private var persons: List<Person> = Collections.emptyList()

    private val updateOnModification = true

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return persons[position].name.hashCode().toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.city_list_content, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = persons.get(position)
        holder.idView.text = item.name
        holder.contentView.text = item.age.toString()
    }

    override fun getItemCount():Int {
        return  persons.size
    }

    fun updateValues(list: RealmResults<Person>) {
        this.persons = list
        notifyDataSetChanged()
    }

    fun updateValues(collectionChange: CollectionChange<RealmResults<Person>>) {
        val changeSet = collectionChange.changeset
        persons = collectionChange.collection

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


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val idView: TextView = view.id_text
        val contentView: TextView = view.content
    }
}
