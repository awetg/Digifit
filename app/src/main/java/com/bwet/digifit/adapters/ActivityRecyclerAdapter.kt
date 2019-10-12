package com.bwet.digifit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bwet.digifit.R
import com.bwet.digifit.model.ActivitySession
import com.bwet.digifit.utils.TimeUtil
import kotlinx.android.synthetic.main.activity_list_items.view.*


class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

class ActivityRecyclerAdapter(private var sessions: List<ActivitySession>) : RecyclerView.Adapter<ActivityViewHolder>() {

    private var clickListener: (ActivitySession) -> Unit = { _ -> }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        return ActivityViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.activity_list_items,parent,false) as ConstraintLayout
        )
    }

    override fun getItemCount(): Int  = sessions.size

    fun setClickListener(newClickListener: (ActivitySession) -> Unit) {
        clickListener = newClickListener
    }

//    fun addSession(session: ActivitySession) {
//        sessions.add(session)
//        notifyDataSetChanged()
//    }

    fun purgeAdd(sessionList: List<ActivitySession>) {
        sessions = sessionList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {

        val activitySession = sessions[position]
        val icon = if (activitySession.activityType == "Running") R.drawable.ic_directions_run_black_24dp else R.drawable.ic_directions_walk_black_24dp
        holder.itemView.activity_name.setBackgroundResource(icon)
        holder.itemView.activity_distance.text = "${String.format("%.2f", activitySession.distance)} m(s)"
        holder.itemView.activity_time.text = TimeUtil.getDuration(activitySession.startTimeMills, activitySession.endTimeMills)
        holder.itemView.session_date.text = TimeUtil.getDate(activitySession.startTimeMills)
        holder.itemView.setOnClickListener{ clickListener(activitySession) }
    }

}