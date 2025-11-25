package com.example.fixitnopanic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

public class CountryAdapter extends BaseAdapter {
    private final Context context;
    private final List<Country> countries;

    public CountryAdapter(Context context, List<Country> countries) {
        this.context = context;
        this.countries = countries;
    }

    @Override
    public int getCount() {
        return countries.size();
    }

    @Override
    public Object getItem(int position) {
        return countries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_country, parent, false);
        }
        bindView(view, countries.get(position));
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_country_dropdown, parent, false);
        }
        bindView(view, countries.get(position));
        return view;
    }

    private void bindView(View view, Country country) {
        ImageView flagImageView = view.findViewById(R.id.flagImageView);
        TextView countryNameTextView = view.findViewById(R.id.countryNameTextView);

        if (country.isDefault()) {
            flagImageView.setImageResource(R.drawable.question_mark_icon);
        } else {
            flagImageView.setImageResource(country.getFlag());
        }

        if (country.isDefault()) {
            countryNameTextView.setText(country.getName());
        } else {
            countryNameTextView.setText(country.getName() + " " + country.getCode());
        }
    }
}