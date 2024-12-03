package com.example.winewms.ui.control.sales

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.PopupWindow
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.winewms.R
import com.example.winewms.api.WineApi
import com.example.winewms.api.WineApiService
import com.example.winewms.data.model.CumulativeSales
import com.example.winewms.data.model.FinancialViewModel
import com.example.winewms.data.model.SalesResponseViewModel
import com.example.winewms.data.model.SearchWineViewModel
import com.example.winewms.data.model.WineViewModel
import com.example.winewms.databinding.FragmentSalesBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


class SalesFragment : Fragment() {

    lateinit var binding: FragmentSalesBinding

    private var wineApi = WineApi.retrofit.create(WineApiService::class.java)

    //lateinit var listOfSales: List<CumulativeSales>
    private val salesViewModel: SalesResponseViewModel by activityViewModels()

    // Filters
//    private var filters = mutableMapOf<String, String>()
    private val selectedWineTypes = mutableListOf<String>()
    private lateinit var startDateTime: LocalDateTime
    private lateinit var endDateTime: LocalDateTime

    private var totalSalesUnit: Int = 0
    private var totalSalesAmount: Float = 0.0f
    private var totalSalesUnitPrevious: Int = 0
    private var totalSalesAmountPrevious: Float = 0.0f

    var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    var pstZone = ZoneId.of("America/Los_Angeles")
    var utcZone = ZoneId.of("UTC")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSalesBinding.inflate(inflater, container, false)

        setupClickListeners()

        //Get cumulative cost revenue by date
        getLastThirtyDaysCumulativeSales()

        //Observe sales report from View Model
        observeSalesReport()

        return binding.root
    }

    private fun setupClickListeners() {

        // get sales by wine filters
        binding.imgSearch.setOnClickListener {
            hideKeyboard()
            getCumulativeSales()
        }
        // Filter button click listener
        binding.imgFilter.setOnClickListener {
            showFilterPopup(it)
        }

        //get currently month cumulative sales
        binding.btnMonth.setOnClickListener{
            // Get the current date and time
            endDateTime = LocalDateTime.now()
            // Get the first day of the current month
            startDateTime = endDateTime.withDayOfMonth(1)
            startDateTime = startDateTime.withHour(0)
            startDateTime = startDateTime.withMinute(0)
            startDateTime = startDateTime.withSecond(1)

            //call function to request sales by date
            getCumulativeSales()
        }

        //get currently year cumulative sales
        binding.btnYear.setOnClickListener{
            // Get the current date
            endDateTime = LocalDateTime.now()
            // Get the first day of the current month
            startDateTime = endDateTime.withSecond(1)
            startDateTime = startDateTime.withMinute(0)
            startDateTime = startDateTime.withHour(0)
            startDateTime = startDateTime.withDayOfMonth(1)
            startDateTime = startDateTime.withDayOfYear(1)

            //call function to request cost and revenue by date
            getCumulativeSales()
        }

        //get cumulative sales by custom date
        binding.btnGoDate.setOnClickListener{
            //Get and format date and time
            endDateTime = LocalDateTime.parse(binding.txtEndDate.text.toString(), formatter)
            startDateTime = LocalDateTime.parse(binding.txtStartDate.text.toString(), formatter)

            //call function to request cost and revenue by date
            getCumulativeSales()
        }

        // Set calendar image start date button
        binding.btnCalendarStartDate.setOnClickListener {
            // Toggle calendar visibility
            if (binding.linearLayoutCalendar.visibility == View.VISIBLE) {
                binding.linearLayoutCalendar.visibility = View.GONE
            } else {
                binding.linearLayoutCalendar.visibility = View.VISIBLE
            }

            // Set a listener to capture date changes
            binding.calendarViewSalesManagement.setOnDateChangeListener { _, year, month, dayOfMonth ->
                // Month is 0-based, so add 1 to display correctly
                val localDate = LocalDate.of(year, month + 1, dayOfMonth)
                val formattedDateTime = localDate.toString() + " 00:00:01"

                // Set the formatted date in txtStartDate
                binding.txtStartDate.setText(formattedDateTime)
                binding.linearLayoutCalendar.visibility = View.GONE
            }
        }

        // Set calendar image end date button
        binding.btnCalendarEndDate.setOnClickListener {
            // Toggle calendar visibility
            if (binding.linearLayoutCalendar.visibility == View.VISIBLE) {
                binding.linearLayoutCalendar.visibility = View.GONE
            } else {
                binding.linearLayoutCalendar.visibility = View.VISIBLE
            }

            binding.calendarViewSalesManagement.setOnDateChangeListener { _, year, month, dayOfMonth ->
                // Month is 0-based, so add 1 to display correctly
                val localDate = LocalDate.of(year, month + 1, dayOfMonth)
                val formattedDateTime = localDate.toString() + " 23:59:59"

                // Set the formatted date in txtStartDate
                binding.txtEndDate.setText(formattedDateTime)
                binding.linearLayoutCalendar.visibility = View.GONE
            }
        }
    }

    //Function to observe sales report from View Model
    private fun observeSalesReport() {
        salesViewModel.listOfSales.observe(viewLifecycleOwner, Observer { listOfSales ->
            setAndDisplaySalesReport()
        })
    }

    //Set UI parameters and display report
    private fun setAndDisplaySalesReport() {

        totalSalesUnit = 0
        totalSalesAmount = 0.0f

        val unitEntries = mutableListOf<Entry>()
        val amountEntries = mutableListOf<Entry>()

        //calculate total sales.
        salesViewModel.listOfSales.value?.let { salesList ->
            salesList.forEachIndexed { index, sale ->
                // Parse the string date into LocalDate
                val saleDate = LocalDateTime.parse(sale.date, formatter)

                // Compare dates to split requested and extended periods.
                if (saleDate >= startDateTime) {
                    totalSalesUnit += sale.cumulativeSalesUnit.toInt()
                    totalSalesAmount += sale.cumulativeSalesAmount.toFloat()

                    val salesUnit = sale.cumulativeSalesUnit.toFloat()
                    val salesAmount = sale.cumulativeSalesAmount.toFloat()
                    unitEntries.add(Entry(index.toFloat(), salesUnit))
                    amountEntries.add(Entry(index.toFloat(), salesAmount))
                } else {
                    totalSalesUnitPrevious += sale.cumulativeSalesUnit.toInt()
                    totalSalesAmountPrevious += sale.cumulativeSalesAmount.toFloat()
                }
            }
        } ?: run {
            Log.e("List of Cost and Revenue", "The list of cost and revenue is null.")
        }

        // Configure the Sales Unit Chart
        configureChart(binding.lineChartUnitSales, unitEntries, "Sales Units")

        // Configure the Sales Amount Chart
        configureChart(binding.lineChartAmountSales, amountEntries, "Sales Amount")

        //set Total sales (units and amount)
        binding.txtTotalSalesUnit.text = totalSalesUnit.toString()
        binding.txtTotalSalesAmount.text = totalSalesAmount.toString()
        binding.txtStartPeriod.setText(startDateTime.toString().take(10))
        binding.txtEndPeriod.setText(endDateTime.toString().take(10))
    }

    private fun configureChart(chart: LineChart, entries: List<Entry>, label: String) {
        val dataSet = LineDataSet(entries, label).apply {
            color = Color.BLUE
            valueTextColor = Color.BLACK
            lineWidth = 2f
            setDrawCircles(true)
            circleRadius = 4f
            circleHoleColor = Color.WHITE
            setDrawValues(true)
        }

        val lineData = LineData(dataSet)
        chart.apply {
            data = lineData
            description.isEnabled = false // Disable the description label
            legend.isEnabled = true // Enable legend
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f // Set the interval between entries
                setDrawGridLines(false)
                textColor = Color.BLACK
            }
            axisLeft.textColor = Color.BLACK // Configure Y-axis
            axisRight.isEnabled = false // Disable the right Y-axis
            animateY(1000) // Add animation
            invalidate() // Refresh the chart
        }
    }

    // Function to set filter for the last 30 days and request cumulative sales
    private fun getLastThirtyDaysCumulativeSales() {
        // Get the current date and time
        endDateTime = LocalDateTime.now()
        // Get the date 30 days ago
        startDateTime = endDateTime.minusDays(30)
        startDateTime = startDateTime.withSecond(1)
        startDateTime = startDateTime.withMinute(0)
        startDateTime = startDateTime.withHour(0)

        //get cumulative sales by date
        getCumulativeSales()
    }

    //Function to fetch data from EndPoint API.
    private fun getCumulativeSales() {

        val filters = mutableMapOf<String, String>()

        salesViewModel.clearSalesList()

        val pstEndDateTime = endDateTime.atZone(pstZone)
        val pstStartDateTime = startDateTime.atZone(pstZone)

        // Calculate the range in days between the dates
        val daysRange = ChronoUnit.DAYS.between(pstStartDateTime, pstEndDateTime)

        // Extend the start date to include the previous period
        val extendedStartDateTime = pstStartDateTime.minusDays(daysRange)

        // Convert the ZonedDateTime in PST to UTC
        val utcEndDateTime = pstEndDateTime.withZoneSameInstant(utcZone)
        val utcExtendedStartDateTime = extendedStartDateTime.withZoneSameInstant(utcZone)

        // Format the dates to strings in the UTC time zone
        val formattedEndDate = utcEndDateTime.format(formatter)
        val formattedExtendedStartDate = utcExtendedStartDateTime.format(formatter)

        //Date filters setup
        filters["start_date"] = formattedExtendedStartDate
        filters["end_date"] = formattedEndDate

        //Wine name filter
        val query = binding.txtSalesSearch.text.toString().trim()  // Wine name query
        if (query.isNotEmpty()) {
            filters["name"] = query
        }

        //Wine type filter
        if (selectedWineTypes.isNotEmpty()) {
            filters["type"] = selectedWineTypes.joinToString(",")
        }

        //fetch data from backend server
        val apiCall = wineApi.getCumulativeSales(filters = filters)
        apiCall.enqueue(object : Callback<List<CumulativeSales>> {
            override fun onFailure(call: Call<List<CumulativeSales>>, t: Throwable) {
                Toast.makeText(requireContext(), "Failed to fetch data.", Toast.LENGTH_SHORT).show()
                Log.e("API Service Failure", t.message.toString())
            }

            override fun onResponse(call: Call<List<CumulativeSales>>, response: Response<List<CumulativeSales>>) {
                if (response.isSuccessful) {
                    // Update the ViewModel with the list
                    response.body()?.let { salesViewModel.setListOfSales(it) }
                } else {
                    Log.e("API Service Response", "Failed to fetch data. Error: ${response.errorBody()?.string()}")
                }
            }
        })
    }

    private fun showFilterPopup(anchorView: View) {
        val inflater = LayoutInflater.from(context)
        val popupView = inflater.inflate(R.layout.filter_popup_sales, null)
        val popupWindow = PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)

        // Retrieve checkboxes for wine types
        val redCheckBox = popupView.findViewById<CheckBox>(R.id.filter_option_red)
        val whiteCheckBox = popupView.findViewById<CheckBox>(R.id.filter_option_white)
        val roseCheckBox = popupView.findViewById<CheckBox>(R.id.filter_option_rose)
        val sparklingCheckBox = popupView.findViewById<CheckBox>(R.id.filter_option_sparkling)
        val dessertCheckBox = popupView.findViewById<CheckBox>(R.id.filter_option_dessert_wine)
        val orangeCheckBox = popupView.findViewById<CheckBox>(R.id.filter_option_orange_wine)

        // Initialize the CheckBox states based on previously selected wine types
        redCheckBox.isChecked = selectedWineTypes.contains("Red")
        whiteCheckBox.isChecked = selectedWineTypes.contains("White")
        roseCheckBox.isChecked = selectedWineTypes.contains("Rose")
        sparklingCheckBox.isChecked = selectedWineTypes.contains("Sparkling")
        dessertCheckBox.isChecked = selectedWineTypes.contains("Dessert")
        orangeCheckBox.isChecked = selectedWineTypes.contains("Orange")

        // Apply button to save the current filter configuration
        popupView.findViewById<Button>(R.id.btn_apply_filters).setOnClickListener {
            // Save the selected wine types
            selectedWineTypes.clear()
            if (redCheckBox.isChecked) selectedWineTypes.add("Red")
            if (whiteCheckBox.isChecked) selectedWineTypes.add("White")
            if (roseCheckBox.isChecked) selectedWineTypes.add("Rose")
            if (sparklingCheckBox.isChecked) selectedWineTypes.add("Sparkling")
            if (dessertCheckBox.isChecked) selectedWineTypes.add("Dessert")
            if (orangeCheckBox.isChecked) selectedWineTypes.add("Orange")

            popupWindow.dismiss()

            // Apply filters with updated values
            getCumulativeSales()
        }

        // Clear button click listener to reset controls
        popupView.findViewById<Button>(R.id.btn_clear_filters).setOnClickListener {
            // Reset checkboxes
            redCheckBox.isChecked = false
            whiteCheckBox.isChecked = false
            roseCheckBox.isChecked = false
            sparklingCheckBox.isChecked = false
            dessertCheckBox.isChecked = false
            orangeCheckBox.isChecked = false

            // Clear the stored filter variables
            selectedWineTypes.clear()
        }

        popupWindow.showAsDropDown(anchorView, 0, 0)
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.txtSalesSearch.windowToken, 0)
    }
}