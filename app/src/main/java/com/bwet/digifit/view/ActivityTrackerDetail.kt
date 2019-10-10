package com.bwet.digifit.view

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.se.omapi.Session
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
    private var geoPoints : List<GeoPoint> = listOf()
    private lateinit var mLocationOverlay: MyLocationNewOverlay

    override fun onCreate(savedInstanceState: Bundle?) {
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracker_detail)

        /*actionBar?.setDisplayHomeAsUpEnabled(true)
        val bar = supportActionBar
        bar?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        */

        supportActionBar?.let {
            it.title = "Tracker Details"
            it.setBackgroundDrawable(ColorDrawable(Color.WHITE))
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }

        val id = intent.getIntExtra(ACTIVITY_TRACKER_DETAIL_KEY,-1)

        activitySessionViewModel = ViewModelProviders.of(this).get(ActivitySessionViewModel::class.java)

        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(9.0)
        map.controller.setCenter(GeoPoint(60.17, 24.95))


        line = Polyline(map)
        line.paint.color = Color.BLUE
        line.paint.strokeJoin = Paint.Join.ROUND

        line.infoWindow = BasicInfoWindow(org.osmdroid.library.R.layout.bonuspack_bubble, map)
        line.showInfoWindow()
        mLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), map)
        mLocationOverlay.enableMyLocation()


        GlobalScope.launch(Dispatchers.IO){
            session = async {
               activitySessionViewModel.getSessionById(id)
            }.await()
            setUpDAta()
        }

    }

    private fun setUpDAta() {

        geoPoints = session?.locationList!!.map { GeoPoint(it.latitude, it.longitude) }
       /* geoPoints.add(GeoPoint(60.238927, 24.913689))
        geoPoints.add(GeoPoint(60.241917, 24.933143))
*/
        line.setPoints(geoPoints)
        map.overlayManager.add(line)
        line.title = session?.activityType
        line.subDescription = "Distance = ${session?.distance} meters"
        show_activity.text = session?.activityType
        show_distance.text = session?.distance.toString()
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
}
