package com.bwet.digifit.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.bwet.digifit.R
import com.bwet.digifit.utils.BoardingItem
import kotlinx.android.synthetic.main.boarding_layout.view.*

class BoardingPagerAdapter(private val context: Context, private val boardingItems: List<BoardingItem>) : PagerAdapter() {

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

    override fun getCount(): Int = boardingItems.size

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeViewAt(position)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.boarding_layout, null)
        view.boarding_title_txt.text = boardingItems[position].title
        view.boarding_description_txt.text = boardingItems[position].description
        view.boarding_img.setImageResource(boardingItems[position].imgResource)
        container.addView(view)
        return view
    }
}