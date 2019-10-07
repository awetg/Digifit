package com.bwet.digifit.view




import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.bwet.digifit.R
import com.bwet.digifit.adapters.ActivityRecyclerAdapter
import kotlinx.android.synthetic.main.fragment_activity_tracker.*
import kotlinx.android.synthetic.main.fragment_activity_tracker.view.*
import java.io.IOException

data class Session(var duration:Long, var location:ArrayList<Location>, var distance:Long, var activity:String){}

class ActivityTrackerFragment : Fragment(), LocationListener, AdapterView.OnItemSelectedListener{

    val TAG = "DBG"
    private val path: ArrayList<Location> = arrayListOf()
    var session: MutableList<Session> = mutableListOf()
    val tracks = arrayOf("Running","Walking")

    lateinit var locationManager: LocationManager
    lateinit var myAdapter: ArrayAdapter<String>
    lateinit var listAdapter: ActivityRecyclerAdapter

    var sessionOn = false
    var startTime = 0L
    var timeInMiliSeconds = 0L
    var timeSwapBuff = 0L
    var timeElapsed = 0L
    lateinit var customTimeHandler: Handler

    var distance_Travelled = 0L
    var activitySelected : String = ""

   private val updateTimerThread = object : Runnable {
       override fun run() {
            timeInMiliSeconds = System.currentTimeMillis() - startTime
            timeElapsed = timeSwapBuff + timeInMiliSeconds
            var secs = timeElapsed/1000
            var mins = secs/60
            var hrs = mins/60
            secs %= 60
            mins %= 60
            clock.setText(
                String.format("%02d", hrs) +  ":" +
                        String.format("%02d", mins)+":" +
                        String.format("%02d",secs)
            )

            customTimeHandler.postDelayed(this,1000)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}
        locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
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
            view.spinner.onItemSelectedListener = this
        }

        customTimeHandler = Handler()
        listAdapter = ActivityRecyclerAdapter(mutableListOf())
        view.activity_recyclerView.adapter = listAdapter
        view.activity_recyclerView.layoutManager = LinearLayoutManager(activity)

        view.startSessionbtn.setOnClickListener {
            Log.d("log","button pressed")
             if (sessionOn) {
                startSessionbtn?.setBackgroundResource(R.drawable.ic_play_circle_filled_black_24dp)
                timeSwapBuff += timeInMiliSeconds
                 customTimeHandler.removeCallbacks(updateTimerThread)
                sessionOn = false
                 savebtn.visibility = View.VISIBLE
            } else {
                 checkPermission()
                 listenToLocation()
                 savebtn.visibility = View.INVISIBLE
                startSessionbtn?.setBackgroundResource(R.drawable.ic_pause_circle_filled_black_24dp)
                startTime = System.currentTimeMillis()
                customTimeHandler.postDelayed(updateTimerThread,0)
                sessionOn = true
            }

        }


        view.savebtn.setOnClickListener {
            val s = Session(timeElapsed, path, distance_Travelled, activitySelected)
           session.add(s)
            listAdapter.addSession(s)
            savebtn.visibility = View.INVISIBLE
            clock.text = "00:00:00"
            Log.d("log", "session saving in data class")
        }

        return view
    }


    fun listenToLocation(){
            Log.d("log", "listening to location")
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

    }
    private fun checkPermission() {

        Log.d("log", "checking permission")
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

    private fun getDistance(): Double {
        var distance: Double = 0.0

        path.reduce { current, next ->
            distance += current.distanceTo(next)
            next
        }
        return distance
    }

    override fun onLocationChanged(location: Location?) {
        location?.let {

            path.add(location)
            val dist = getDistance()
                distance_Travelled = dist.toLong()

        }
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
            requestLocationUpdates()
        }

    override fun onProviderEnabled(p0: String?) {}

    override fun onProviderDisabled(p0: String?) {}

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
        activitySelected = parent!!.getItemAtPosition(pos).toString()
        Log.d("log", "$activitySelected")
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {}

    companion object {
        @JvmStatic
        fun newInstance() =
            ActivityTrackerFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}