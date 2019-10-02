package com.bwet.digifit.view




import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bwet.digifit.R


class ActivityTracker : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_activity_tracker, container, false)

        return view
    }


    companion object {
        @JvmStatic
        fun newInstance() =
            ActivityTracker().apply {
                arguments = Bundle().apply {
                }
            }
    }



}