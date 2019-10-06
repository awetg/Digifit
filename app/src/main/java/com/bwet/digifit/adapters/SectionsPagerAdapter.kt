package com.bwet.digifit.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.bwet.digifit.view.PedometerFragment
import com.bwet.digifit.R
import com.bwet.digifit.view.PlaceholderFragment
import com.bwet.digifit.view.ActivityTracker

private val TAB_TITLES = arrayOf(
    R.string.pedometer_tab,
    R.string.tab_text_2,
    R.string.tab_text_3,
    R.string.tab_text_4


)

private val TAB_FRAGMENTS = arrayOf(
    PedometerFragment.newInstance(),
    ActivityTracker.newInstance(),
    PlaceholderFragment.newInstance(3),
    PlaceholderFragment.newInstance(4)
)

class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return TAB_FRAGMENTS.get(position)

    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        // Show 2 total pages.
        return TAB_TITLES.size
    }
}