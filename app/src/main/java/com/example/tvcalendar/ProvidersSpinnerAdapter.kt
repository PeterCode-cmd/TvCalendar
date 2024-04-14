package com.example.tvcalendar

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView

class ProvidersSpinnerAdapter(context: Context, private val providers: List<String>) : ArrayAdapter<String>(context, R.layout.spinner_item_with_checkbox, providers) {

    private val selectedProviders = BooleanArray(providers.size)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.spinner_item_with_checkbox, parent, false)
        val providerCheckBox = view.findViewById<CheckBox>(R.id.providerCheckBox)
        val providerNameTextView = view.findViewById<TextView>(R.id.providerNameTextView)

        providerNameTextView.text = providers[position]
        providerCheckBox.isChecked = selectedProviders[position]
        providerCheckBox.setOnCheckedChangeListener { _, isChecked ->
            selectedProviders[position] = isChecked
        }
        return view
    }

    private fun getSelectedProviders(): List<String> {
        val selectedList = mutableListOf<String>()
        for (i in selectedProviders.indices) {
            if (selectedProviders[i]) {
                selectedList.add(providers[i])
            }
        }
        return selectedList
    }
}
