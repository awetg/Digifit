package com.bwet.digifit.view




import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bwet.digifit.R
import com.bwet.digifit.adapters.ActivityRecyclerAdapter
import com.bwet.digifit.model.ActivitySession
import com.bwet.digifit.utils.ACTIVITY_TRACKER_DETAIL_KEY
import com.bwet.digifit.utils.DEBUG_TAG
import com.bwet.digifit.utils.RuntimePermissionUtil
import com.bwet.digifit.viewModel.ActivitySessionViewModel
import kotlinx.android.synthetic.main.fragment_activity_tracker.*
import kotlinx.android.synthetic.main.fragment_activity_tracker.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList

class ActivityTrackerFragment : Fragment(), LocationListener, AdapterView.OnItemSelectedListener{

    private lateinit var runtimePermissionUtil: RuntimePermissionUtil
    private lateinit var locationManager: LocationManager
    private var gpsProviderEnable: Boolean = false
    private lateinit var activityRecyclerAdapter: ActivityRecyclerAdapter
    private lateinit var activitySessionViewModel: ActivitySessionViewModel

    private var locationList: ArrayList<Location> = arrayListOf()
    private var totalDistance = 0.0
    private var activitySelected : String = ""

    private var sessionOn = false
    private var startTime = 0L
    private var pauseOffset = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {}
        locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        gpsProviderEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        runtimePermissionUtil = RuntimePermissionUtil.getInstance(activity!!)
        activitySessionViewModel = ViewModelProviders.of(this).get(ActivitySessionViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_activity_tracker, container, false)

        activity?.let {
            val activitiesSpinnerAdapter = ArrayAdapter.createFromResource(it, R.array.activities, android.R.layout.simple_spinner_item)

            activitiesSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            view.activities_spinner.adapter = activitiesSpinnerAdapter
            view.activities_spinner.onItemSelectedListener = this
        }

        activityRecyclerAdapter = ActivityRecyclerAdapter(mutableListOf())
        activityRecyclerAdapter.setClickListener {
            val intent = Intent(activity, ActivityTrackerDetail::class.java)
            intent.putExtra(ACTIVITY_TRACKER_DETAIL_KEY, it.id)
            startActivity(intent)

        }
        view.activity_recyclerView.adapter = activityRecyclerAdapter
        view.activity_recyclerView.layoutManager = LinearLayoutManager(activity)

        activitySessionViewModel.getAllSessions().observe(this, Observer { activityRecyclerAdapter.purgeAdd(it.reversed()) })

        view.start_session_btn.setOnClickListener {
             if (sessionOn) {
                 chronometer.stop()
                 pauseOffset = SystemClock.elapsedRealtime() - chronometer.base
                start_session_btn?.setImageDrawable(resources.getDrawable(R.drawable.ic_play_arrow_black_24dp))
                sessionOn = false
                 activity_stop_btn.visibility = View.VISIBLE
            } else {
                 if (runtimePermissionUtil.isPermissionAvailable(android.Manifest.permission.ACCESS_FINE_LOCATION))
                     runtimePermissionUtil.requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION))
                 else {
                     if (gpsProviderEnable) {
                         requestLocationUpdates()
                         activity_stop_btn.visibility = View.INVISIBLE
                         start_session_btn?.setImageDrawable(resources.getDrawable(R.drawable.ic_pause_black_24dp))
                         startTime = System.currentTimeMillis()
                         chronometer.base = SystemClock.elapsedRealtime() - pauseOffset
                         chronometer.start()
                         sessionOn = true
                     } else {
                         showEnablProviderDialog()
                     }
                 }
            }

        }


        view.activity_stop_btn.setOnClickListener {
            val timeElapsed = SystemClock.elapsedRealtime() - chronometer.base
            val endTime = startTime + timeElapsed
            chronometer.base = SystemClock.elapsedRealtime()
            pauseOffset = 0L
            GlobalScope.launch(Dispatchers.IO) {
                activitySessionViewModel.insertActivitySession(
                    ActivitySession(startTime, endTime, locationList, totalDistance, activitySelected)
                )
            }
            locationList = arrayListOf()
            activity_stop_btn.visibility = View.INVISIBLE
        }

        return view
    }

    private fun requestLocationUpdates() {
        locationManager.removeUpdates(this)
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 1.0f, this)

//            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 5.0f, this)

        } catch (e: SecurityException) {
            e.printStackTrace()
            Log.d(DEBUG_TAG, "security exception ${e.message}")
        }
    }

    private fun calculateDistance(): Double {
        var distance = 0.0
        locationList.reduce { current, next ->
            distance += current.distanceTo(next)
            next
        }
        return distance
    }

    override fun onLocationChanged(location: Location?) {
        location?.let {
            Toast.makeText(activity, "${location.latitude.toString()}",Toast.LENGTH_LONG).show()
            locationList.add(it)
            totalDistance += calculateDistance()
        }
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
            requestLocationUpdates()
        }

    override fun onProviderEnabled(provider: String?) {
        if (provider == "gps") gpsProviderEnable = true
    }

    override fun onProviderDisabled(provider: String?) {
        if (provider == "gps") gpsProviderEnable = false
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
        activitySelected = parent!!.getItemAtPosition(pos).toString()
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {}

    companion object {
        @JvmStatic
        fun newInstance() = ActivityTrackerFragment()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            RuntimePermissionUtil.PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //permission granted
                } else {
                    activity?.let {
                        val runtimePermissionUtil = RuntimePermissionUtil.getInstance(it)
                        runtimePermissionUtil.showDialogAndAsk(
                            getString(R.string.locationPermissionMessage),
                            DialogInterface.OnClickListener{ _, _ -> runtimePermissionUtil.requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION))}
                        )
                    }
                }
            }
        }
    }

    private fun showEnablProviderDialog() {
        AlertDialog.Builder(activity)
            .setTitle("GPS Disabled")
            .setMessage("GPS is disabled.Please enable GPS to continue. Do you want to go to setting to enable it?")
            .setPositiveButton("OK") {_ , _-> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))}
            .show()
    }
}