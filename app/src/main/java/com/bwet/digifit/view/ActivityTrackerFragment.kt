package com.bwet.digifit.view




import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.bwet.digifit.R
import kotlinx.android.synthetic.main.fragment_activity_tracker.*
import kotlinx.android.synthetic.main.fragment_activity_tracker.view.*
import java.io.IOException
import kotlin.concurrent.timer


class ActivityTrackerFragment : Fragment(), LocationListener {
data class Session(val startTime:Long, val endTime:Long, var location:ArrayList<Location>){}

    val TAG = "DBG"
    private val path: ArrayList<Location> = arrayListOf()
    lateinit var locationManager: LocationManager
    val tracks = arrayOf("Running","Walking")
    lateinit var myAdapter: ArrayAdapter<String>
    var session: ArrayList<Session> = arrayListOf()
    var sessionOn = false
    var currentActivity: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_activity_tracker, container, false)
        activity?.let {
            myAdapter = ArrayAdapter(it, android.R.layout.simple_spinner_item, tracks)
            myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            view.spinner.adapter = myAdapter
        }

        locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        checkPermission()

        if ((activity?.checkSelfPermission(
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)) {
            try {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    500,
                    5.0f,
                    this
                )

                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    500,
                    5.0f,
                    this
                )
            } catch (e: IOException) {
                Log.d("Error", "IO exception caught")
            }
        }


        return view

    }


        private fun checkPermission() {
            if ((Build.VERSION.SDK_INT >= 23 && activity?.checkSelfPermission(
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED))
            {
                requestPermissions(
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 0
                )
            }
        }

        private fun requestLocationUpdates() {
            locationManager.removeUpdates(this)
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 5.0f, this)

                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    500,
                    5.0f,
                    this
                )

            } catch (e: SecurityException) {
                Log.d(TAG, "security exception ${e.message}")
            }
        }

        override fun onLocationChanged(location: Location?) {
            location?.let {

                path.add(location)
                val dist = getDistance()
                if(dist!= null){
                    distance_travelled.text = dist.toString()
                }
            }
        }

    private fun getDistance(): Double {
        var distance: Double = 0.0

        path.reduce { current, next ->
            distance += current.distanceTo(next)
            next
        }
        return distance
    }


    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
            requestLocationUpdates()
        }

        override fun onProviderEnabled(p0: String?) {}

        override fun onProviderDisabled(p0: String?) {}


    companion object {
        @JvmStatic
        fun newInstance() =
            ActivityTrackerFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }



}