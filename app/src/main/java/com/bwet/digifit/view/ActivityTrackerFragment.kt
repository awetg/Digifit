package com.bwet.digifit.view




import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bwet.digifit.R
import com.bwet.digifit.adapters.ActivityRecyclerAdapter
import com.bwet.digifit.utils.ACTIVITY_TRACKER_DETAIL_KEY
import com.bwet.digifit.utils.RuntimePermissionUtil
import com.bwet.digifit.services.ActivityTrackerService
import com.bwet.digifit.utils.*
import com.bwet.digifit.viewModel.ActivitySessionViewModel
import kotlinx.android.synthetic.main.fragment_activity_tracker.*
import kotlinx.android.synthetic.main.fragment_activity_tracker.view.*

class ActivityTrackerFragment : Fragment(), AdapterView.OnItemSelectedListener{

    private lateinit var runtimePermissionUtil: RuntimePermissionUtil
    private var sharedPreferenceUtil: SharedPreferenceUtil? = null
    private lateinit var activityRecyclerAdapter: ActivityRecyclerAdapter
    private lateinit var activitySessionViewModel: ActivitySessionViewModel
    private var gpsProviderEnable: Boolean = false

    private var activitySelected : String = ""

    private var startTime = 0L
    private var pauseOffset = 0L
    private var elapsedTime = 0L
    private var sessionOn = false

    private val activityTrackerBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BROADCAST_ACTION_GPS_PROVIDER) {
                gpsProviderEnable = intent.getBooleanExtra(GPS_PROVIDER_ENABLED, false)
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = ActivityTrackerFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runtimePermissionUtil = RuntimePermissionUtil.getInstance(activity!!)
        activitySessionViewModel = ViewModelProviders.of(this).get(ActivitySessionViewModel::class.java)
        activity?.let { sharedPreferenceUtil = SharedPreferenceUtil(it) }
        val chronometerState = sharedPreferenceUtil?.getChronometerStae()
        startTime = chronometerState?.startTime ?: 0L
        pauseOffset = chronometerState?.pauseOffset ?: 0L
        elapsedTime = chronometerState?.elapsedTime ?: 0L
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_activity_tracker, container, false)

        if (startTime > 0) {
            if (pauseOffset == 0L) {
                // resume tracking
                elapsedTime = System.currentTimeMillis() - startTime
                view.chronometer.base =  SystemClock.elapsedRealtime() - elapsedTime
                view.chronometer.start()
                sessionOn = true
                view.start_session_btn.setImageDrawable(ContextCompat.getDrawable(activity!!, R.drawable.ic_pause_black_24dp))
            } else {
                // set paused tracking
                view.chronometer.base = SystemClock.elapsedRealtime() - elapsedTime
                view.chronometer.stop()
                pauseOffset = SystemClock.elapsedRealtime() - view.chronometer.base
                sessionOn = false
                view.start_session_btn.setImageDrawable(ContextCompat.getDrawable(activity!!, R.drawable.ic_play_arrow_black_24dp))
                view.start_session_btn.setImageDrawable(ContextCompat.getDrawable(activity!!, R.drawable.ic_play_arrow_black_24dp))
                view.activity_stop_btn.visibility = View.VISIBLE
            }
        }

        // initialize activity type spinner
        activity?.let {
            val activitiesSpinnerAdapter = ArrayAdapter.createFromResource(it, R.array.activities, android.R.layout.simple_spinner_item)
            activitiesSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            view.activities_spinner.adapter = activitiesSpinnerAdapter
            view.activities_spinner.onItemSelectedListener = this
        }

        // initialize list of activity session recycler adapter
        activityRecyclerAdapter = ActivityRecyclerAdapter(mutableListOf())
        view.activity_recyclerView.adapter = activityRecyclerAdapter
        view.activity_recyclerView.layoutManager = LinearLayoutManager(activity)
        activityRecyclerAdapter.setClickListener {
            val intent = Intent(activity, ActivityTrackerDetail::class.java)
            intent.putExtra(ACTIVITY_TRACKER_DETAIL_KEY, it.id)
            startActivity(intent)

        }

        activitySessionViewModel.getAllSessions().observe(this, Observer { activityRecyclerAdapter.purgeAdd(it.reversed()) })

        view.start_session_btn.setOnClickListener {if (sessionOn) pauseTracking() else startTracking()}

        view.activity_stop_btn.setOnClickListener {saveActivitySession()}

        return view
    }

    override fun onStart() {
        registerReceiver()
        super.onStart()
    }

    override fun onStop() {
        activity?.unregisterReceiver(activityTrackerBroadcastReceiver)
        super.onStop()
    }

    override fun onDestroyView() {
        if (chronometer != null) {
            if (sessionOn)
                elapsedTime = SystemClock.elapsedRealtime() - chronometer.base
            sharedPreferenceUtil?.saveChronometerSate(ChronometerState(startTime, pauseOffset, elapsedTime))
        }
        super.onDestroyView()
    }


    // ------------------- AdapterView.OnItemSelectedListener methods----------------------------------//

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
        activitySelected = parent!!.getItemAtPosition(pos).toString()
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {}




    // ------------------------------------ TRACKING METHODS -------------------------------//

    private fun startChronometer() {
        chronometer.base = SystemClock.elapsedRealtime() - pauseOffset
        chronometer.start()
    }

    private fun pauseChronometer() {
        chronometer.stop()
        pauseOffset = SystemClock.elapsedRealtime() - chronometer.base
    }

    private fun pauseService() {
        ContextCompat.startForegroundService(
            activity?.applicationContext!!,
            Intent(activity, ActivityTrackerService::class.java)
                .putExtra(ACTIVITY_SERVICE_INTENT_PAUSE_SESSION, false)
        )
    }

    private fun registerReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BROADCAST_ACTION_GPS_PROVIDER)
        activity!!.registerReceiver(activityTrackerBroadcastReceiver, intentFilter)
    }

    private fun startTracking() {

        if (runtimePermissionUtil.isPermissionAvailable(android.Manifest.permission.ACCESS_FINE_LOCATION))
            runtimePermissionUtil.requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION))
        else {
            ContextCompat.startForegroundService(activity?.applicationContext!!, Intent(activity, ActivityTrackerService::class.java))
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    if (gpsProviderEnable) {
                        startTime = System.currentTimeMillis()
                        startChronometer()
                        activity_stop_btn.visibility = View.INVISIBLE
                        start_session_btn.setImageDrawable(ContextCompat.getDrawable(activity!!, R.drawable.ic_pause_black_24dp))
                        sessionOn = true
                        elapsedTime = 0L
                        pauseOffset = 0L
                    } else {
                        showEnableProviderDialog()
                    }

                }, 40)
        }
    }

    private fun pauseTracking() {
        pauseChronometer()
        pauseService()
        start_session_btn.setImageDrawable(ContextCompat.getDrawable(activity!!, R.drawable.ic_play_arrow_black_24dp))
        activity_stop_btn.visibility = View.VISIBLE
        sessionOn = false
        elapsedTime = SystemClock.elapsedRealtime() - chronometer.base
    }

    private fun saveActivitySession() {
        elapsedTime = SystemClock.elapsedRealtime() - chronometer.base
        ContextCompat.startForegroundService(
            activity?.applicationContext!!,
            Intent(activity, ActivityTrackerService::class.java)
                .putExtra(ACTIVITY_SERVICE_INTENT_START_TIME, startTime)
                .putExtra(ACTIVITY_SERVICE_INTENT_ELAPSED_TIME, elapsedTime)
                .putExtra(ACTIVITY_SERVICE_INTENT_SELECTED_ACTIVITY, activitySelected)
        )
        chronometer.base = SystemClock.elapsedRealtime()
        pauseOffset = 0L
        startTime = 0L
        elapsedTime = 0L
        sessionOn = false
        activity_stop_btn.visibility = View.INVISIBLE
    }



    //---------------------------------PERMISSION METHODS--------------------------------//

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

    private fun showEnableProviderDialog() {
        AlertDialog.Builder(activity)
            .setTitle("GPS Disabled")
            .setMessage("GPS is disabled.Please enable GPS to continue. Do you want to go to setting to enable it?")
            .setPositiveButton("OK") {_ , _-> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))}
            .show()
    }
}