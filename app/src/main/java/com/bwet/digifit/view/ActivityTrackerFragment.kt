package com.bwet.digifit.view




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
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.util.Log
import android.view.WindowManager
import androidx.recyclerview.widget.DividerItemDecoration

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
                Log.d("DBG", "stop is false, pause is false")
                sharedPreferenceUtil?.saveBoolean(SETTING_PREFERENCE_FILE_KEY, STOP_SERVICE_FLAG_KEY, false)
                sharedPreferenceUtil?.saveBoolean(SETTING_PREFERENCE_FILE_KEY, PUASE_SERVICE_FLAG_KEY, false)
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
                sharedPreferenceUtil?.saveBoolean(SETTING_PREFERENCE_FILE_KEY, STOP_SERVICE_FLAG_KEY, false)
                sharedPreferenceUtil?.saveBoolean(SETTING_PREFERENCE_FILE_KEY, PUASE_SERVICE_FLAG_KEY, true)
                Log.d("DBG", "stop is false, pause is true")
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
        view.activity_recyclerView.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
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

    private fun registerReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BROADCAST_ACTION_GPS_PROVIDER)
        activity!!.registerReceiver(activityTrackerBroadcastReceiver, intentFilter)
    }

    private fun startTracking() {

        if (runtimePermissionUtil.isPermissionAvailable(ACCESS_FINE_LOCATION) && runtimePermissionUtil.isPermissionAvailable(ACCESS_COARSE_LOCATION))
            runtimePermissionUtil.requestPermissions(arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION))
        else {
            Log.d("DBG", "stop is false, pause is false")
            sharedPreferenceUtil?.saveBoolean(SETTING_PREFERENCE_FILE_KEY, STOP_SERVICE_FLAG_KEY, false)
            sharedPreferenceUtil?.saveBoolean(SETTING_PREFERENCE_FILE_KEY, PUASE_SERVICE_FLAG_KEY, false)
            ContextCompat.startForegroundService(activity?.applicationContext!!, Intent(activity, ActivityTrackerService::class.java))
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    if (gpsProviderEnable) {
                        if (pauseOffset == 0L) {
                            try {
                                runtimePermissionUtil.showDialogAndAsk(
                                    "",
                                    getString(R.string.gpsLimitationMessage)
                                )
                            } catch (e: WindowManager.BadTokenException) {}
                        }
                        startTime = System.currentTimeMillis()
                        startChronometer()
                        activity_stop_btn.visibility = View.INVISIBLE
                        start_session_btn.setImageDrawable(ContextCompat.getDrawable(activity!!, R.drawable.ic_pause_black_24dp))
                        sessionOn = true
                        elapsedTime = 0L
                        pauseOffset = 0L
                    } else {
                        runtimePermissionUtil.showDialogAndAsk(
                            getString(R.string.gpsDisabledTitle),
                            getString(R.string.gpsDisabledMessag),
                            DialogInterface.OnClickListener{_ , _-> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))}
                        )
                    }

                }, 60)
        }
    }

    private fun pauseTracking() {
        pauseChronometer()
        Log.d("DBG", "stop is false, pause is true")
        sharedPreferenceUtil?.saveBoolean(SETTING_PREFERENCE_FILE_KEY, PUASE_SERVICE_FLAG_KEY, true)
        sharedPreferenceUtil?.saveBoolean(SETTING_PREFERENCE_FILE_KEY, STOP_SERVICE_FLAG_KEY, false)
        start_session_btn.setImageDrawable(ContextCompat.getDrawable(activity!!, R.drawable.ic_play_arrow_black_24dp))
        activity_stop_btn.visibility = View.VISIBLE
        sessionOn = false
        elapsedTime = SystemClock.elapsedRealtime() - chronometer.base
    }

    private fun saveActivitySession() {
        elapsedTime = SystemClock.elapsedRealtime() - chronometer.base
        sharedPreferenceUtil?.let {
            it.saveSessionSate(SessionState(startTime, elapsedTime, activitySelected))
            it.saveBoolean(SETTING_PREFERENCE_FILE_KEY, STOP_SERVICE_FLAG_KEY, true)
            it.saveBoolean(SETTING_PREFERENCE_FILE_KEY, PUASE_SERVICE_FLAG_KEY, false)
            Log.d("DBG", "stop is true, pause is false")

        }
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
                            "",
                            getString(R.string.locationPermissionMessage),
                            DialogInterface.OnClickListener{ _, _ -> runtimePermissionUtil.requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION))}
                        )
                    }
                }
            }
        }
    }
}