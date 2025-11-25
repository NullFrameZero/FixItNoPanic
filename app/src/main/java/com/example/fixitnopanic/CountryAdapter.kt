package com.example.fixitnopanic

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class CountryAdapter(private val context: Context, private val countries: List<Country>) : BaseAdapter() {
    override fun getCount(): Int = countries.size
    override fun getItem(position: Int): Any = countries[position]
    override fun getItemId(position: Int): Long = position.toLong()
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_country, parent, false)
        bindView(view, countries[position])
        return view
    }
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_country_dropdown, parent, false)
        bindView(view, countries[position])
        return view
    }
    private fun bindView(view: View, country: Country) {
        val flagImageView = view.findViewById<ImageView>(R.id.flagImageView)
        val countryNameTextView = view.findViewById<TextView>(R.id.countryNameTextView)
        if (country.isDefault) {
            flagImageView.setImageResource(R.drawable.question_mark_icon)
        } else {
            flagImageView.setImageResource(country.flag)
        }
        countryNameTextView.text = if (country.isDefault) {
            country.name
        } else {
            "${country.name} ${country.code}"
        }
    }
}