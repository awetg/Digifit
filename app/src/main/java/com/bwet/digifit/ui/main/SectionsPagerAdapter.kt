package com.bwet.digifit.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.bwet.digifit.PedometerFragment
import com.bwet.digifit.R

private val TAB_TITLES = arrayOf(
    R.string.pedometer,
    R.string.tab_text_2,
    R.string.tab_text_3,
    R.string.tab_text_4
)

private val TAB_FRAGMENTS = arrayOf(
    PedometerFragment.newInstance(),
    PlaceholderFragment.newInstance(2),
    PlaceholderFragment.newInstance(3),
    PlaceholderFragment.newInstance(4)
)

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return TAB_FRAGMENTS.get(position)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        // Show 2 total pages.
        return 4
    }
}