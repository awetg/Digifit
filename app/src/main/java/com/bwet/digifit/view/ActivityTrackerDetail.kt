package com.bwet.digifit.view

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.se.omapi.Session
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Toolbar
import androidx.appcompat.app.ActionBar
import androidx.core.content.getSystemService
import androidx.lifecycle.ViewModelProviders
import com.bwet.digifit.R
import com.bwet.digifit.model.ActivitySession
import com.bwet.digifit.utils.ACTIVITY_TRACKER_DETAIL_KEY
import com.bwet.digifit.utils.TimeUtil
import com.bwet.digifit.viewModel.ActivitySessionViewModel
import kotlinx.android.synthetic.main.activity_list_items.*
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.activity_tracker_detail.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.text.SimpleDateFormat
import java.util.*

class ActivityTrackerDetail : AppCompatActivity() {

    private lateinit var activitySessionViewModel: ActivitySessionViewModel
    private var session : ActivitySession? = null
    private lateinit var line: Polyline
    private var geoPoints : List<GeoPoint>? = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracker_detail)

        setSupportActionBar(detail_toolbar)
        supportActionBar?.let {
            it.title = "Activity Detail"
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }

        val id = intent.getIntExtra(ACTIVITY_TRACKER_DETAIL_KEY,-1)

        activitySessionViewModel = ViewModelProviders.of(this).get(ActivitySessionViewModel::class.java)

        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(9.5)


        line = Polyline(map)
        line.paint.color = Color.BLUE
        line.paint.strokeJoin = Paint.Join.ROUND

        line.infoWindow = BasicInfoWindow(org.osmdroid.library.R.layout.bonuspack_bubble, map)
        line.showInfoWindow()


        GlobalScope.launch(Dispatchers.IO){
            session = async {
               activitySessionViewModel.getSessionById(id)
            }.await()
            setUpData()
        }

    }

    private fun setUpData() {

        geoPoints = session?.locationList?.map { GeoPoint(it.latitude, it.longitude) }

        line.setPoints(geoPoints)
        line.title = session?.activityType
        line.subDescription = "Distance = ${session?.distance} meters"

        map.overlayManager.add(line)
        map.invalidate()
        if (geoPoints?.size != 0) {
            map.controller.setCenter(geoPoints?.first())
        } else {
            map.controller.setCenter(GeoPoint(60.1699, 24.9384))
        }
        map.controller.setZoom(19.0)


        show_activity.text = session?.activityType
        show_distance.text = "${String.format("%.2f", session?.distance)} meters"
        val time = SimpleDateFormat("MMM d, HH:mm").format(Date(session!!.startTimeMills))
        start_time.text = time
        show_duration.text = TimeUtil.getDuration(session!!.startTimeMills, session!!.endTimeMills)
    }



    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}
