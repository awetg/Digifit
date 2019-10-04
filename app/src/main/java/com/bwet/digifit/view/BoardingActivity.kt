package com.bwet.digifit.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.bwet.digifit.R
import com.bwet.digifit.utils.BoardingItem
import com.bwet.digifit.adapters.BoardingPagerAdapter
import kotlinx.android.synthetic.main.activity_boarding.*

class BoardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_boarding)

        val boardingItems = listOf(
            BoardingItem("Track your steps", "The app will uses builtin step detector to track your steps without draining your battery.", R.drawable.ic_directions_walk_black_24dp),
            BoardingItem("Track your activity", "You can track activities like running or walking with more details metrics like location distance and speed.", R.drawable.ic_directions_walk_black_24dp)
        )

        val boardingPagerAdapter =
            BoardingPagerAdapter(this, boardingItems)
        boarding_viewpager.adapter = boardingPagerAdapter
        boarding_tablayout.setupWithViewPager(boarding_viewpager)

        get_started_btn.setOnClickListener { startActivity(Intent(this, MainActivity::class.java)) }

        next_btn.setOnClickListener {
            if(boarding_viewpager.currentItem < boardingItems.size - 1) {
                boarding_viewpager.currentItem++
            } else {
                // Show get started button
                next_btn.visibility = View.INVISIBLE
                boarding_tablayout.visibility = View.INVISIBLE
                get_started_btn.visibility = View.VISIBLE
            }
        }

    }
}
