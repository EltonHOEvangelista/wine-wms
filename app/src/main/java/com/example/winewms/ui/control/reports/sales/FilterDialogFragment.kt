package com.example.winewms.ui.control.reports.sales

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.winewms.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class FilterDialogFragment : DialogFragment() {

    private var onApplyFiltersListener: ((String, String, List<String>, Boolean) -> Unit)? = null

    fun setOnApplyFiltersListener(listener: (String, String, List<String>, Boolean) -> Unit) {
        onApplyFiltersListener = listener
    }

//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        val dialog = super.onCreateDialog(savedInstanceState)
//        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent) // Remove o fundo padrão
//        dialog.window?.setLayout(
//            ViewGroup.LayoutParams.MATCH_PARENT, // Define a largura para ocupar todo o espaço disponível
//            ViewGroup.LayoutParams.WRAP_CONTENT // Altura se ajusta ao conteúdo
//        )
//        return dialog
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.filter_popup_sales, container, false)

        val startDateField = view.findViewById<EditText>(R.id.etStartDate)
        val endDateField = view.findViewById<EditText>(R.id.etEndDate)

        val allCheckBox = view.findViewById<CheckBox>(R.id.filter_option_all)
        val redWineCheckBox = view.findViewById<CheckBox>(R.id.filter_option_red)
        val whiteWineCheckBox = view.findViewById<CheckBox>(R.id.filter_option_white)
        val roseWineCheckBox = view.findViewById<CheckBox>(R.id.filter_option_rose)
        val sparklingWineCheckBox = view.findViewById<CheckBox>(R.id.filter_option_sparkling)
        val bestSellersCheckBox = view.findViewById<CheckBox>(R.id.filter_option_best_sellers)

        allCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                redWineCheckBox.isChecked = false
                whiteWineCheckBox.isChecked = false
                roseWineCheckBox.isChecked = false
                sparklingWineCheckBox.isChecked = false
                bestSellersCheckBox.isChecked = false
                startDateField.text.clear()
                endDateField.text.clear()
            }
        }

        view.findViewById<Button>(R.id.btnApplyFilters).setOnClickListener {
            val startDate = startDateField.text.toString()
            val endDate = endDateField.text.toString()

            // Validate dates
            if (startDate.isNotEmpty() && endDate.isNotEmpty()) {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val startDateParsed = sdf.parse(startDate)
                val endDateParsed = sdf.parse(endDate)

                if (startDateParsed != null && endDateParsed != null && startDateParsed.after(endDateParsed)) {
                    Toast.makeText(requireContext(), "Start date cannot be after end date.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            val selectedCategories = mutableListOf<String>()
            if (redWineCheckBox.isChecked) selectedCategories.add("Red Wine")
            if (whiteWineCheckBox.isChecked) selectedCategories.add("White Wine")
            if (roseWineCheckBox.isChecked) selectedCategories.add("Rose Wine")
            if (sparklingWineCheckBox.isChecked) selectedCategories.add("Sparkling Wine")

            onApplyFiltersListener?.invoke(
                startDate,
                endDate,
                if (allCheckBox.isChecked) emptyList() else selectedCategories,
                bestSellersCheckBox.isChecked
            )
            dismiss()
        }

        view.findViewById<Button>(R.id.btnClearFilters).setOnClickListener {
            allCheckBox.isChecked = false
            redWineCheckBox.isChecked = false
            whiteWineCheckBox.isChecked = false
            roseWineCheckBox.isChecked = false
            sparklingWineCheckBox.isChecked = false
            bestSellersCheckBox.isChecked = false
            startDateField.text.clear()
            endDateField.text.clear()
        }

        setupDatePicker(startDateField)
        setupDatePicker(endDateField)

        return view
    }

    private fun setupDatePicker(editText: EditText) {
        editText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                editText.setText(formattedDate)
            }, year, month, day)

            datePicker.show()
        }
    }
}

