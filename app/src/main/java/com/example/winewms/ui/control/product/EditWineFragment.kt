package com.example.winewms.ui.control.product

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
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
import com.example.winewms.data.model.WineViewModel
import com.example.winewms.databinding.FragmentEditWineBinding
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class EditWineFragment : Fragment() {

    lateinit var binding: FragmentEditWineBinding

    lateinit var wineModel: WineModel
    private val wineViewModel: WineViewModel by activityViewModels()

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
    var newPurchase: Boolean = false
    var newBottleImage: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentEditWineBinding.inflate(inflater)

        setupClickListeners()

        setupUIComponents()

        //Observe list of wines from Wine View Model
        observeWineList()

        return binding.root
    }

    private fun observeWineList() {
        wineViewModel.wineList.observe(viewLifecycleOwner, Observer { listOfWines ->
            loadWineDataOnForm(listOfWines[0])
        })
    }

    private fun loadWineDataOnForm(wine: WineModel) {
        binding.apply {

            //initiate variables
            image_path = wine.image_path
            wineId = wine.id

            //Load image from Google Firebase
            val storageRef = com.google.firebase.ktx.Firebase.storage.reference.child(wine.image_path)
            // Fetch the image URL from Firebase Storage
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                Picasso.get()
                    .load(uri.toString())
                    .error(R.drawable.wine_bottle_t)
                    .into(binding.imgWineBottle)
            }.addOnFailureListener {
                binding.imgWineBottle.setImageResource(R.drawable.wine_bottle_t)
            }

            // Basic info
            txtAddWineName.setText(wine.name)
            txtAddWineProducer.setText(wine.producer)
            txtAddWineCountry.setText(wine.country)
            txtAddWineHarvest.setText(wine.harvest_year.toString())
            txtAddWineRate.setText(wine.rate.toString())
            txtAddWineDescription.setText(wine.description)

            // Wine type
            val typePosition = (spinnerWineType.adapter as ArrayAdapter<String>)
                .getPosition(wine.type)
            spinnerWineType.setSelection(typePosition)

            // Taste characteristics
            wine.taste_characteristics.let { taste ->
                spinnerLightness.setSelection(taste.lightness - 1)
                spinnerTannin.setSelection(taste.tannin - 1)
                spinnerDryness.setSelection(taste.dryness - 1)
                spinnerAcidity.setSelection(taste.acidity - 1)
            }

            // Grapes
            checkBoxChardonnay.isChecked = wine.grapes.contains("Chardonnay")
            checkBoxSauvignonBlanc.isChecked = wine.grapes.contains("Sauvignon Blanc")
            checkBoxPinotGrigio.isChecked = wine.grapes.contains("Pinot Grigio")
            checkBoxCabernetSauvignon.isChecked = wine.grapes.contains("Cabernet Sauvignon")
            checkBoxMalbec.isChecked = wine.grapes.contains("Malbec")
            checkBoxZinfandel.isChecked = wine.grapes.contains("Zinfandel")
            checkBoxTempranillo.isChecked = wine.grapes.contains("Tempranillo")
            checkBoxSangiovese.isChecked = wine.grapes.contains("Sangiovese")
            checkBoxGrenache.isChecked = wine.grapes.contains("Grenache")
            checkBoxPinotNoir.isChecked = wine.grapes.contains("Pinot Noir")
            checkBoxSyrah.isChecked = wine.grapes.contains("Syrah")
            checkBoxMerlot.isChecked = wine.grapes.contains("Merlot")

            // Food pairings
            checkBoxStake.isChecked = wine.food_pair.contains("Stake")
            checkBoxLamb.isChecked = wine.food_pair.contains("Lamb")
            checkBoxChicken.isChecked = wine.food_pair.contains("Chicken")
            checkBoxPork.isChecked = wine.food_pair.contains("Pork")
            checkBoxPasta.isChecked = wine.food_pair.contains("Pasta")
            checkBoxAgedCheeses.isChecked = wine.food_pair.contains("Aged Cheeses")
            checkBoxSeafood.isChecked = wine.food_pair.contains("Seafood")
            checkBoxFish.isChecked = wine.food_pair.contains("Fish")
            checkBoxSalad.isChecked = wine.food_pair.contains("Salad")
            checkBoxCharcuterie.isChecked = wine.food_pair.contains("Charcuterie")
            checkBoxDarkChocolate.isChecked = wine.food_pair.contains("Dark Chocolate")
            checkBoxOtherFoods.isChecked = wine.food_pair.contains("Other Foods")

            // Price and stock
            txtSalePrice.setText(String.format("%.2f", wine.sale_price))
            txtSaleDiscont.setText((String.format("%.2f", wine.discount * 100)))
            txtStock.setText(wine.stock.toString())

            // Reviews
            txtAddWineReviews.setText(wine.reviews.joinToString("\n"))
        }
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

        val spinnerNewWarehouseLocation: Spinner = binding.spinnerNewWarehouseLocation
        context?.let {
            ArrayAdapter.createFromResource(
                it,
                R.array.spinnerWarehouseLocation,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerNewWarehouseLocation.adapter = adapter
            }
        }

        val spinnerNewWarehouseAisle: Spinner = binding.spinnerNewWarehouseAisle
        context?.let {
            ArrayAdapter.createFromResource(
                it,
                R.array.spinnerAisle,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerNewWarehouseAisle.adapter = adapter
            }
        }

        val spinnerNewWarehouseShelf: Spinner = binding.spinnerNewWarehouseShelf
        context?.let {
            ArrayAdapter.createFromResource(
                it,
                R.array.spinnerShelf,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerNewWarehouseShelf.adapter = adapter
            }
        }
    }

    private fun setupClickListeners() {

        // Set click listener to add wine image
        binding.linearLayoutBottle.setOnClickListener {
            // Call the function to open the gallery
            openImagePicker()
            newBottleImage = true
        }

        // Set click listener for Add Wine Button
        binding.btnUpdateWine.setOnClickListener {
            UpdateWine()
        }
    }

    private fun UpdateWine() {

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

        cost_price = binding.txtNewCostPrice.text.toString().toFloat()
        stock = binding.txtAdditionalStock.text.toString().trim().toInt()

        warehouse_location = binding.spinnerNewWarehouseLocation.selectedItem.toString()
        warehouse_aisle = binding.spinnerNewWarehouseAisle.selectedItem.toString()
        warehouse_shelf = binding.spinnerNewWarehouseShelf.selectedItem.toString()

        if (listOf(name, producer, country, type, description, lightness, tannin, dryness, acidity,
                warehouse_location, warehouse_aisle, warehouse_shelf).any { it.isEmpty() } ||
            harvest_year == null || rate == null || sale_price == null || discount == null ||
            grapes.isEmpty() || food_pair.isEmpty() ||
            !::image_path.isInitialized) {
            Toast.makeText(requireContext(), "Please fill out all required fields, including wine image.", Toast.LENGTH_SHORT).show()
        }
        else if ((cost_price != 0.0f && stock == 0) || (cost_price == 0.0f && stock != 0)) {
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
                id = wineId,
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
                stock = 0,
                stockLocation = mutableListOf()
            )

            if (cost_price != 0.0f) {
                newPurchase = true
            }

            //upload wine to backend
            updateWineOnBackendServer()
        }
    }

    //Function to update wine on backend server
    private fun updateWineOnBackendServer() {
        //Fetch data from api
        val apiCall = wineApi.updateWine(wineModel.id, wineModel)

        //Asynchronous call to create new wine
        apiCall.enqueue(object : Callback<ResponseModel> {
            override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("API Service Failure", t.message.toString())
            }
            override fun onResponse(call: Call<ResponseModel>, response: Response<ResponseModel>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        Toast.makeText(requireContext(), "Wine successfully updated.", Toast.LENGTH_SHORT).show()
                        //upload wine image to Firebase.
                        if (newBottleImage) {
                            uploadImageToFirebase()
                        }

                        if (newPurchase) {
                            placePurchaseOrder()
                        } else {
                            //Navigate back to Fragment Control
                            findNavController().navigate(R.id.navigation_control)
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Update wine failed: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                    Log.e("API Service Response", "Failed to fetch data. Error: ${response.errorBody()?.string()}")
                }
            }
        })
    }

    //function to place purchase order
    private fun placePurchaseOrder() {

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val purchaseDate = LocalDateTime.now().format(formatter)

        val purchaseModel = PurchaseModel(
            id = "0",
            wine_id = wineId,
            cost_price = cost_price,
            amount = stock,
            date = purchaseDate
        )

        //Fetch data from api
        val apiCall = wineApi.placePurchaseOrder(purchaseModel)

        //Asynchronous call to place new purchase order
        apiCall.enqueue(object : Callback<ResponseModel> {
            override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("API Service Failure", t.message.toString())
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
                    Log.e("API Service Response", "Failed to fetch data. Error: ${response.errorBody()?.string()}")
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
                Log.e("API Service Failure", t.message.toString())
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
                    Log.e("API Service Response", "Failed to fetch data. Error: ${response.errorBody()?.string()}")
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