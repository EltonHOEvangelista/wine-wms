package com.example.winewms.ui.control.financial

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.winewms.api.WineApi
import com.example.winewms.api.WineApiService
import com.example.winewms.data.model.CumulativeCostRevenue
import com.example.winewms.data.model.FinancialViewModel
import com.example.winewms.databinding.FragmentFinancialBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.*

class FinancialFragment : Fragment() {

    lateinit var binding: FragmentFinancialBinding

    //Instantiate Wine Api
    var wineApi = WineApi.retrofit.create(WineApiService::class.java)

    private var filters = mutableMapOf<String, String>()
    private lateinit var startDateTime: LocalDateTime
    private lateinit var endDateTime: LocalDateTime
    private var totalCost: Float = 0.0f
    private var totalRevenue: Float = 0.0f
    private var totalCostPrevious: Float = 0.0f
    private var totalRevenuePrevious: Float = 0.0f

    lateinit var listOfCostRevenue: List<CumulativeCostRevenue>
    private val financialViewModel: FinancialViewModel by activityViewModels()

    var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    var pstZone = ZoneId.of("America/Los_Angeles")
    var utcZone = ZoneId.of("UTC")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentFinancialBinding.inflate(inflater)

        setupClickListeners()

        //Get cumulative cost revenue by date
        getLastThirtyDaysCumulativeCostRevenue()

        //Observe financial report from View Model
        observeFinancialReport()

        // Inflate the layout for this fragment
        return binding.root
    }

    // Function to set filter for the last 30 days and request cumulative financial data
    private fun getLastThirtyDaysCumulativeCostRevenue() {
        // Get the current date and time
        endDateTime = LocalDateTime.now()
        // Get the date 30 days ago
        startDateTime = endDateTime.minusDays(30)
        startDateTime = startDateTime.withSecond(1)
        startDateTime = startDateTime.withMinute(0)
        startDateTime = startDateTime.withHour(0)

        //get cumulative cost and revenue by date
        getCumulativeCostRevenueByDate()
    }

    private fun setupClickListeners() {

        //get currently month cumulative cost and revenue
        binding.btnMonth.setOnClickListener{
            // Get the current date and time
            endDateTime = LocalDateTime.now()
            // Get the first day of the current month
            startDateTime = endDateTime.withDayOfMonth(1)
            startDateTime = startDateTime.withHour(0)
            startDateTime = startDateTime.withMinute(0)
            startDateTime = startDateTime.withSecond(1)

            //call function to request cost and revenue by date
            getCumulativeCostRevenueByDate()
        }

        //get currently year cumulative cost and revenue
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
            getCumulativeCostRevenueByDate()
        }

        //get cumulative cost and revenue by custom date
        binding.btnGoDate.setOnClickListener{
            //Get and format date and time
            endDateTime = LocalDateTime.parse(binding.txtEndDate.text.toString(), formatter)
            startDateTime = LocalDateTime.parse(binding.txtStartDate.text.toString(), formatter)

            //call function to request cost and revenue by date
            getCumulativeCostRevenueByDate()
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
            binding.calendarViewFinancialManagement.setOnDateChangeListener { _, year, month, dayOfMonth ->
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

            binding.calendarViewFinancialManagement.setOnDateChangeListener { _, year, month, dayOfMonth ->
                // Month is 0-based, so add 1 to display correctly
                val localDate = LocalDate.of(year, month + 1, dayOfMonth)
                val formattedDateTime = localDate.toString() + " 23:59:59"

                // Set the formatted date in txtStartDate
                binding.txtEndDate.setText(formattedDateTime)
                binding.linearLayoutCalendar.visibility = View.GONE
            }
        }
    }

    //Function to observe financial report from View Model
    private fun observeFinancialReport() {
        financialViewModel.listOfCostRevenue.observe(viewLifecycleOwner, Observer { listOfCostRevenue ->
            setAndDisplayReport()
        })
    }

    //Set UI parameters and display report
    private fun setAndDisplayReport() {

        totalCost = 0.0f
        totalRevenue = 0.0f

        //calculate total cost and revenue.
        financialViewModel.listOfCostRevenue.value?.let { listOfCashFlow ->
            for (dailyCashFlow in listOfCashFlow) {
                // Parse the string date into LocalDate
                val cashFlowDate = LocalDateTime.parse(dailyCashFlow.date, formatter)
                // Compare dates to split requested and extended periods.
                if (cashFlowDate >= startDateTime) {
                    totalCost += dailyCashFlow.cumulativeCost.toFloat()
                    totalRevenue += dailyCashFlow.cumulativeSales.toFloat()
                    Log.e("Cash Flow Date", "Date: ${cashFlowDate} Cost:${dailyCashFlow.cumulativeCost} Revenue: ${dailyCashFlow.cumulativeSales}")
                } else {
                    totalCostPrevious += dailyCashFlow.cumulativeCost.toFloat()
                    totalRevenuePrevious += dailyCashFlow.cumulativeSales.toFloat()
                    Log.e("Cash Flow Date", "Previous Date: ${cashFlowDate} Cost:${dailyCashFlow.cumulativeCost} Revenue: ${dailyCashFlow.cumulativeSales}")
                }
            }
            Log.e("Cash Flow Date", "---------------")
        } ?: run {
            Log.e("List of Cost and Revenue", "The list of cost and revenue is null.")
        }

        //set product cash flow
        setProductCashFlow()

        //set Gross Profit Margin
        val gpm: Float = (totalRevenue - totalCost) / totalRevenue * 100
        binding.txtGrossProfitMargin.text = String.format("%.2f", gpm) + " %"

        //set Revenue Growth Rate
        var rgr: Float = 0.0f
        if (totalRevenuePrevious <= 0) {
            rgr = 100.00f
        }
        else {
            rgr = (totalRevenue - totalRevenuePrevious) / totalRevenuePrevious * 100
        }
        binding.txtRevenueGrowthRate.text = String.format("%.2f", rgr) + " %"
    }

    private fun setProductCashFlow() {

        //call function to setup graph layout
        setProductCashFlowUIParameters(totalCost, totalRevenue)

        binding.txtTotalCost.text = String.format("$ %.2f", totalCost)
        binding.txtTotalRevenue.text = String.format("$ %.2f", totalRevenue)
        binding.txtStartDate.setText(startDateTime.toString())
        binding.txtEndDate.setText(endDateTime.toString())
        binding.txtStartPeriod.setText(startDateTime.toString().take(10))
        binding.txtEndPeriod.setText(endDateTime.toString().take(10))
    }

    //Function to setup product cash flow graph scale
    private fun setProductCashFlowUIParameters(totalCost: Float, totalRevenue: Float) {

        var value: Double = 1.00
        value = if (totalCost > totalRevenue) {
            totalCost.toDouble()
        } else {
            totalRevenue.toDouble()
        }

        val magnitude = 10.0.pow(floor(log10(value))).toInt() // Base magnitude
        val scale = when {
            value <= magnitude * 2 -> magnitude / 2  // Use half-step scale for smaller ranges
            else -> magnitude  // Full step for larger ranges
        }
        val maxValue = ceil(value / scale.toDouble()).toInt() * scale  // Round up to nearest

        // Function to set LayoutParams based on cost and revenue values
        fun setGraphBar(layout: LinearLayout, weight: Float) {
            val params = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT
            ).apply {
                this.weight = weight
            }
            layout.layoutParams = params
        }

        //set graph bar width using its weight
        val costGraphWeight: Float = (totalCost / maxValue)
        val costGraphEmptyWeight: Float = (1.00f - costGraphWeight)
        setGraphBar(binding.linearLayoutCostGraph, costGraphWeight)
        setGraphBar(binding.linearLayoutCostGraphEmpty, costGraphEmptyWeight)

        //set graph bar width using its weight
        val revenueGraphWeight: Float = (totalRevenue / maxValue)
        val revenueGraphEmptyWeight: Float = (1.00f - revenueGraphWeight)
        setGraphBar(binding.linearLayoutRevenueGraph, revenueGraphWeight)
        setGraphBar(binding.linearLayoutRevenueGraphEmpty, revenueGraphEmptyWeight)
    }

    //Function to fetch data from EndPoint API.
    private fun getCumulativeCostRevenueByDate() {

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

        //filters setup
        filters["start_date"] = formattedExtendedStartDate
        filters["end_date"] = formattedEndDate

        //fetch data from backend server
        val apiCall = wineApi.getCumulativeCostRevenueByDate(filters = filters)
        apiCall.enqueue(object : Callback<List<CumulativeCostRevenue>> {
            override fun onFailure(call: Call<List<CumulativeCostRevenue>>, t: Throwable) {
                Toast.makeText(requireContext(), "Failed to fetch data.", Toast.LENGTH_SHORT).show()
                Log.e("API Service Failure", t.message.toString())
            }

            override fun onResponse(call: Call<List<CumulativeCostRevenue>>, response: Response<List<CumulativeCostRevenue>>) {
                if (response.isSuccessful) {
                    listOfCostRevenue = response.body()!!
                    // Update the ViewModel with the list
                    response.body()?.let { financialViewModel.setListOfCostRevenue(it) }
                    //financialViewModel.setListOfCostRevenue(response.body()!!)
                } else {
                    Log.e("API Service Response", "Failed to fetch data. Error: ${response.errorBody()?.string()}")
                }
            }
        })
    }
}