package com.example.winewms.ui.control.addwine

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.example.winewms.R
import com.example.winewms.api.WineApi
import com.example.winewms.api.WineApiService
import com.example.winewms.data.model.Aisle
import com.example.winewms.data.model.PurchaseModel
import com.example.winewms.data.model.ResponseModel
import com.example.winewms.data.model.Shelf
import com.example.winewms.data.model.TasteCharacteristics
import com.example.winewms.data.model.WarehouseModel
import com.example.winewms.data.model.WineBox
import com.example.winewms.data.model.WineModel
import com.example.winewms.databinding.FragmentAddWineBinding
import com.google.firebase.storage.FirebaseStorage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddWineFragment : Fragment() {

    lateinit var binding: FragmentAddWineBinding

    lateinit var wineModel: WineModel

    private var imageUri: Uri? = null
    private val firebaseStorage = FirebaseStorage.getInstance().reference

    //Instantiate Wine Api
    var wineApi = WineApi.retrofit.create(WineApiService::class.java)

    //variable to store image path
    private lateinit var image_path: String

    //global variable Wine Stock
    var stock: Int = 0
    lateinit var wineId: String
    var cost_price: Float = 0.0f
    lateinit var warehouse_location: String
    lateinit var warehouse_aisle: String
    lateinit var warehouse_shelf: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentAddWineBinding.inflate(inflater)

        setupClickListeners()

        setupUIComponents()

        return binding.root
    }

    //Function to setup UI components (spinners)
    private fun setupUIComponents() {

        val spinnerWineType: Spinner = binding.spinnerWineType
        context?.let {
            ArrayAdapter.createFromResource(
                it,
                R.array.spinnerWineType,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerWineType.adapter = adapter
            }
        }

        val spinnerLightness: Spinner = binding.spinnerLightness
        context?.let {
            ArrayAdapter.createFromResource(
                it,
                R.array.spinner_1_10,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerLightness.adapter = adapter
            }
        }

        val spinnerTannin: Spinner = binding.spinnerTannin
        context?.let {
            ArrayAdapter.createFromResource(
                it,
                R.array.spinner_1_10,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerTannin.adapter = adapter
            }
        }

        val spinnerDryness: Spinner = binding.spinnerDryness
        context?.let {
            ArrayAdapter.createFromResource(
                it,
                R.array.spinner_1_10,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerDryness.adapter = adapter
            }
        }

        val spinnerAcidity: Spinner = binding.spinnerAcidity
        context?.let {
            ArrayAdapter.createFromResource(
                it,
                R.array.spinner_1_10,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerAcidity.adapter = adapter
            }
        }

        val spinnerWarehouseLocation: Spinner = binding.spinnerWarehouseLocation
        context?.let {
            ArrayAdapter.createFromResource(
                it,
                R.array.spinnerWarehouseLocation,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerWarehouseLocation.adapter = adapter
            }
        }

        val spinnerWarehouseAisle: Spinner = binding.spinnerWarehouseAisle
        context?.let {
            ArrayAdapter.createFromResource(
                it,
                R.array.spinnerAisle,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerWarehouseAisle.adapter = adapter
            }
        }

        val spinnerWarehouseShelf: Spinner = binding.spinnerWarehouseShelf
        context?.let {
            ArrayAdapter.createFromResource(
                it,
                R.array.spinnerShelf,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerWarehouseShelf.adapter = adapter
            }
        }
    }

    private fun setupClickListeners() {

        // Set click listener to add wine image
        binding.linearLayoutBottle.setOnClickListener {
            // Call the function to open the gallery
            openImagePicker()
        }

        // Set click listener for Add Wine Button
        binding.btnSaveWine.setOnClickListener {
            addNewWine()
        }
    }

    private fun addNewWine() {

        //load data from UI
        val name = binding.txtAddWineName.text.toString().trim()
        val producer = binding.txtAddWineProducer.text.toString().trim()
        val country = binding.txtAddWineCountry.text.toString().trim()
        val harvest_year = binding.txtAddWineHarvest.text.toString().toInt()
        val type = binding.spinnerWineType.selectedItem.toString()
        val rate = binding.txtAddWineRate.text.toString().toFloat()
        val description = binding.txtAddWineDescription.text.toString().trim()
        val reviews: List<String> = binding.txtAddWineReviews.text.toString().trim().split(";")

        val grapes = mutableListOf<String>()
        val grapesCheckBoxes = listOf(
            binding.checkBoxChardonnay,
            binding.checkBoxSauvignonBlanc,
            binding.checkBoxPinotGrigio,
            binding.checkBoxCabernetSauvignon,
            binding.checkBoxMalbec,
            binding.checkBoxZinfandel,
            binding.checkBoxTempranillo,
            binding.checkBoxSangiovese,
            binding.checkBoxGrenache,
            binding.checkBoxPinotNoir,
            binding.checkBoxSyrah,
            binding.checkBoxMerlot,
        )
        grapesCheckBoxes.forEach { checkBox ->
            if (checkBox.isChecked) {
                grapes.add(checkBox.text.toString().trim())
            }
        }

        val lightness = binding.spinnerLightness.selectedItem.toString()
        val tannin = binding.spinnerTannin.selectedItem.toString()
        val dryness = binding.spinnerDryness.selectedItem.toString()
        val acidity = binding.spinnerAcidity.selectedItem.toString()

        val food_pair = mutableListOf<String>()
        val food_pairCheckBoxes = listOf(
            binding.checkBoxStake,
            binding.checkBoxLamb,
            binding.checkBoxChicken,
            binding.checkBoxPork,
            binding.checkBoxPasta,
            binding.checkBoxAgedCheeses,
            binding.checkBoxSeafood,
            binding.checkBoxFish,
            binding.checkBoxSalad,
            binding.checkBoxCharcuterie,
            binding.checkBoxDarkChocolate,
            binding.checkBoxOtherFoods,
        )
        food_pairCheckBoxes.forEach { checkBox ->
            if (checkBox.isChecked) {
                food_pair.add(checkBox.text.toString().trim())
            }
        }

        val sale_price = binding.txtSalePrice.text.toString().toFloat()
        val discount = binding.txtSaleDiscont.text.toString().toFloat() / 100.00
        cost_price = binding.txtCostPrice.text.toString().toFloat()
        stock = binding.txtStock.text.toString().trim().toInt()

        warehouse_location = binding.spinnerWarehouseLocation.selectedItem.toString()
        warehouse_aisle = binding.spinnerWarehouseAisle.selectedItem.toString()
        warehouse_shelf = binding.spinnerWarehouseShelf.selectedItem.toString()

        if (listOf(name, producer, country, type, description, lightness, tannin, dryness, acidity,
                warehouse_location, warehouse_aisle, warehouse_shelf).any { it.isEmpty() } ||
            harvest_year == null || rate == null || sale_price == null || discount == null || cost_price == 0.0f ||
            grapes.isEmpty() || food_pair.isEmpty() ||
            !::image_path.isInitialized) {
            Toast.makeText(requireContext(), "Please fill out all required fields, including wine image.", Toast.LENGTH_SHORT).show()
        }
        else if ((cost_price == 0.0f || stock == 0) ||
            warehouse_location.isEmpty() ||
            warehouse_aisle.isEmpty() ||
            warehouse_shelf.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill out all purchase and storage fields.", Toast.LENGTH_SHORT).show()
        }
        else {
            //load data to taste characteristics model
            val tasteCharacteristics = TasteCharacteristics(
                lightness = lightness.toInt(),
                tannin = tannin.toInt(),
                dryness = dryness.toInt(),
                acidity = acidity.toInt()
            )
            //load data to wine model
            wineModel = WineModel(
                id = "0", //to be replaced by backend
                image_path = image_path,
                name = name,
                producer = producer,
                country = country,
                harvest_year = harvest_year,
                type = type,
                rate = rate,
                description = description,
                reviews = reviews,
                grapes = grapes,
                taste_characteristics = tasteCharacteristics,
                food_pair = food_pair,
                sale_price = sale_price,
                discount = discount.toFloat(),
                stock = 0
            )

            //upload wine to backend
            createWineOnBackendServer()
        }
    }

    //Function to create new wine on backend server
    private fun createWineOnBackendServer() {
        //Fetch data from api
        val apiCall = wineApi.createWine(wineModel)

        //Asynchronous call to create new wine
        apiCall.enqueue(object : Callback<ResponseModel> {
            override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
            override fun onResponse(call: Call<ResponseModel>, response: Response<ResponseModel>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        //get wine id from backend
                        wineId = data.response_id
                        Toast.makeText(requireContext(), "Wine successfully created with id ${wineId}", Toast.LENGTH_SHORT).show()

                        //upload wine image to Firebase.
                        uploadImageToFirebase()

                        //if initial stock is not null, place purchase order and update stock
                        if (stock != 0) {
                            placePurchaseOrder()
                        } else {
                            setFragmentResult("wine_added", bundleOf("refresh" to true))
                            findNavController().navigateUp()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Create wine failed: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    //function to place purchase order
    private fun placePurchaseOrder() {

        val dateFormat = SimpleDateFormat("HH:mm:ss MM/dd/yyyy", Locale.US)

        val purchaseModel = PurchaseModel(
            id = "0",
            wine_id = wineId,
            cost_price = cost_price,
            amount = stock,
            date = dateFormat.format(Date())
        )

        //Fetch data from api
        val apiCall = wineApi.placePurchaseOrder(purchaseModel)

        //Asynchronous call to place new purchase order
        apiCall.enqueue(object : Callback<ResponseModel> {
            override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
            override fun onResponse(call: Call<ResponseModel>, response: Response<ResponseModel>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        //update wine stock
                        updateWineStock()
                    }
                } else {
                    Toast.makeText(requireContext(), "Purchase order failed: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                    //Navigate back to Fragment Control
                    findNavController().navigate(R.id.navigation_control)
                }
            }
        })
    }

    //function to update wine stock
    private fun updateWineStock() {

        val wineBox = mutableListOf(
            WineBox(
                wine_id = wineId,
                stock = stock
            )
        )

        val shelf = mutableListOf(
            Shelf(
                shelf = warehouse_shelf,
                wines = wineBox
            )
        )

        val aisle = mutableListOf(
            Aisle(
                aisle = warehouse_aisle,
                shelves = shelf
            )
        )

        val warehouseModel = WarehouseModel(
            id = "0",
            location = warehouse_location,
            aisles = aisle
        )

        //Fetch data from api
        val apiCall = wineApi.updateStock(warehouseModel)

        //Asynchronous call to place new purchase order
        apiCall.enqueue(object : Callback<ResponseModel> {
            override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
            override fun onResponse(call: Call<ResponseModel>, response: Response<ResponseModel>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        Toast.makeText(requireContext(), "Stock updated successfully", Toast.LENGTH_SHORT).show()
                        //Navigate back to Fragment Control
                        findNavController().navigate(R.id.navigation_control)
                    }
                } else {
                    Toast.makeText(requireContext(), "Update stock failed: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                    //Navigate back to Fragment Control
                    findNavController().navigate(R.id.navigation_control)
                }
            }
        })
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
        if (imageUri != null) {
            binding.imgWineBottle.setImageURI(imageUri)
            image_path = getFileNameFromUri(imageUri!!)
        } else {
            Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFileNameFromUri(uri: Uri): String {
        var fileName = "unknown"
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    fileName = it.getString(nameIndex)
                }
            }
        }
        return fileName
    }

    private fun openImagePicker() {
        pickImageLauncher.launch("image/*")
    }

    private fun uploadImageToFirebase() {
        imageUri?.let {
            val fileReference = firebaseStorage.child("${image_path}") //UUID.randomUUID()
            val uploadTask = fileReference.putFile(it)

            uploadTask.addOnSuccessListener {
                // Image uploaded successfully
                fileReference.downloadUrl.addOnSuccessListener { uri ->
                    // Get the download URL
                    val downloadUrl = uri.toString()
                    println("Image uploaded successfully: $downloadUrl")
                }
            }.addOnFailureListener { exception ->
                // Handle unsuccessful uploads
                println("Failed to upload image: ${exception.message}")
            }
        }
    }

}