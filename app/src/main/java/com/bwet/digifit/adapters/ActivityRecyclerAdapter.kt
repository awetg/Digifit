package com.bwet.digifit.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bwet.digifit.R
import com.bwet.digifit.view.Session
import kotlinx.android.synthetic.main.activity_list_items.view.*


class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

class ActivityRecyclerAdapter(private val sessions: MutableList<Session>) : RecyclerView.Adapter<ActivityViewHolder>() {

    private var clickListner: (Session) -> Unit = { _ -> }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        return ActivityViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.activity_list_items,parent,false) as ConstraintLayout
        )
    }

    override fun getItemCount(): Int  = sessions.size

    fun setClickListener(newClickListener: (Session) -> Unit) {
        //clickListner = newClickListener
        Log.d("on item clicked", "item on the list was clicked")
    }

    fun addSession(session: Session) {
        sessions.add(session)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        Log.d("bog","in adapter binding view holder")
        if(sessions[position].activity == "Running") {
            holder.itemView.activity_name.setBackgroundResource(R.drawable.ic_directions_run_black_24dp)
        }else {
            holder.itemView.activity_name.setBackgroundResource(R.drawable.ic_directions_walk_black_24dp)
        }
        holder.itemView.activity_distance.text = sessions[position].distance.toString() + "Km(s)"

        if(sessions[position].duration > 60){
            val time = sessions[position].duration
            var secs = time/1000
            var mins = secs/60
            var hrs = mins/60
            secs %= 60
            mins %= 60

            holder.itemView.activity_duration.text = String.format("%02d",hrs) + ":"+String.format("%02d",mins) +":"+String.format("%02d",secs)
        }


        holder.itemView.setOnClickListener{ clickListner(sessions[position]) }
    }

}