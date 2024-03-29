package com.bwet.digifit.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.bwet.digifit.view.PedometerFragment
import com.bwet.digifit.R
import com.bwet.digifit.view.ActivityTrackerFragment
import com.bwet.digifit.view.CaloriesFragment
import com.bwet.digifit.view.DistanceFragment

private val TAB_TITLES = arrayOf(
    R.string.pedometer_tab,
    R.string.tracker,
    R.string.calories,
    R.string.distanceTab
)

private val TAB_FRAGMENTS = arrayOf(
    PedometerFragment.newInstance(),
    ActivityTrackerFragment.newInstance(),
    CaloriesFragment.newInstance(),
    DistanceFragment.newInstance()
)

class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return TAB_FRAGMENTS[position]

    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        // Show 2 total pages.
        return TAB_TITLES.size
    }
}