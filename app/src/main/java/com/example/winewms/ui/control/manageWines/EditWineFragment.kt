package com.example.winewms.ui.control.manageWines

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.winewms.R
import com.example.winewms.api.WineApi
import com.example.winewms.api.WineApiService
import com.example.winewms.data.model.ResponseModel
import com.example.winewms.data.model.TasteCharacteristics
import com.example.winewms.data.model.WineModel
import com.example.winewms.databinding.FragmentEditWineBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.gson.Gson


class EditWineFragment : Fragment() {
    private var _binding: FragmentEditWineBinding? = null
    private val binding get() = _binding!!

    private var imageUri: Uri? = null
    private var wineToEdit: WineModel? = null
    private val firebaseStorage = Firebase.storage.reference
    private val wineApi = WineApi.retrofit.create(WineApiService::class.java)
    private val gson = Gson()



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditWineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val wineId = arguments?.getString("wineId")
        if (wineId.isNullOrEmpty()) {
            Toast.makeText(context, "No wine ID provided", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }
        setupUI()
        loadWineData(wineId)

    }


    private fun setupUI() {
        setupSpinners()
        setupClickListeners()
    }
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            binding.imgWineBottle.setImageURI(it)
        }
    }

    private fun setupSpinners() {
        context?.let { ctx ->
            // Setup Wine Type Spinner
            ArrayAdapter.createFromResource(
                ctx,
                R.array.spinnerWineType,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerWineType.adapter = adapter
            }

            // Setup Rating Spinners (1-10)
            ArrayAdapter.createFromResource(
                ctx,
                R.array.spinner_1_10,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.apply {
                    spinnerLightness.adapter = adapter
                    spinnerTannin.adapter = adapter
                    spinnerDryness.adapter = adapter
                    spinnerAcidity.adapter = adapter
                }
            }

            // Setup Warehouse Location Spinners
            ArrayAdapter.createFromResource(
                ctx,
                R.array.spinnerWarehouseLocation,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerWarehouseLocation.adapter = adapter
            }

            ArrayAdapter.createFromResource(
                ctx,
                R.array.spinnerAisle,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerWarehouseAisle.adapter = adapter
            }

            ArrayAdapter.createFromResource(
                ctx,
                R.array.spinnerShelf,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerWarehouseShelf.adapter = adapter
            }
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            // Image picker
            linearLayoutBottle.setOnClickListener {
                pickImageLauncher.launch("image/*")
            }

            // Save button
            btnEditWine.setOnClickListener {
                if (validateInputs()) {
                    saveWine()
                }
            }
        }
    }

    private fun loadWineData(wineId: String) {
        wineApi.getWineById(wineId).enqueue(object : Callback<WineModel> {
            override fun onResponse(call: Call<WineModel>, response: Response<WineModel>) {
                if (response.isSuccessful) {
                    response.body()?.let { wine ->
                        wineToEdit = wine
                        populateForm(wine)
                    }
                } else {
                    Toast.makeText(context, "Failed to load wine", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            }

            override fun onFailure(call: Call<WineModel>, t: Throwable) {
                Toast.makeText(context, "Error loading wine: ${t.message}", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        })
    }

    private fun populateForm(wine: WineModel) {
        binding.apply {
            // Load image
            Picasso.get()
                .load(wine.image_path)
                .error(R.drawable.wine_bottle_t)
                .into(imgWineBottle)

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
            txtSalePrice.setText(wine.sale_price.toString())
            txtSaleDiscont.setText((wine.discount * 100).toString())
            txtStock.setText(wine.stock.toString())

            // Reviews
            txtAddWineReviews.setText(wine.reviews.joinToString("\n"))
        }
    }

    private fun validateInputs(): Boolean {
        binding.apply {
            if (txtAddWineName.text.isNullOrBlank() ||
                txtAddWineProducer.text.isNullOrBlank() ||
                txtAddWineCountry.text.isNullOrBlank() ||
                txtAddWineHarvest.text.isNullOrBlank() ||
                txtAddWineRate.text.isNullOrBlank() ||
                txtAddWineDescription.text.isNullOrBlank() ||
                txtSalePrice.text.isNullOrBlank() ||
                txtSaleDiscont.text.isNullOrBlank() ||
                txtStock.text.isNullOrBlank()
            ) {
                Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }

    private fun saveWine() {
        val updatedWine = createUpdatedWineModel() ?: return

        if (imageUri != null) {
            uploadImageAndUpdateWine(updatedWine)
        } else {
            updateWineInBackend(updatedWine)
        }
    }

    private fun createUpdatedWineModel(): WineModel? {
        binding.apply {
            try {
                // Validate required fields
                val name = txtAddWineName.text.toString().trim()
                val producer = txtAddWineProducer.text.toString().trim()
                val country = txtAddWineCountry.text.toString().trim()
                val harvestYear = txtAddWineHarvest.text.toString().toIntOrNull()
                val rate = txtAddWineRate.text.toString().toFloatOrNull()
                val salePrice = txtSalePrice.text.toString().toFloatOrNull()
                val discount = txtSaleDiscont.text.toString().toFloatOrNull()
                val stock = txtStock.text.toString().toIntOrNull()

                if (name.isBlank() || producer.isBlank() || country.isBlank()) {
                    Toast.makeText(context, "Name, Producer, and Country are required", Toast.LENGTH_SHORT).show()
                    return null
                }

                if (harvestYear == null || harvestYear <= 0) {
                    Toast.makeText(context, "Please enter a valid Harvest Year", Toast.LENGTH_SHORT).show()
                    return null
                }

                if (rate == null || rate < 0 || rate > 5) {
                    Toast.makeText(context, "Please enter a valid Rate (0-5)", Toast.LENGTH_SHORT).show()
                    return null
                }

                if (salePrice == null || salePrice <= 0) {
                    Toast.makeText(context, "Please enter a valid Sale Price", Toast.LENGTH_SHORT).show()
                    return null
                }

                if (discount == null || discount < 0 || discount > 100) {
                    Toast.makeText(context, "Please enter a valid Discount (0-100)", Toast.LENGTH_SHORT).show()
                    return null
                }

                if (stock == null || stock < 0) {
                    Toast.makeText(context, "Please enter a valid Stock value", Toast.LENGTH_SHORT).show()
                    return null
                }

                // Create and return the updated WineModel
                return WineModel(
                    id = wineToEdit?.id ?: throw IllegalArgumentException("Wine ID is missing"),
                    image_path = wineToEdit?.image_path ?: throw IllegalArgumentException("Image path is missing"),
                    name = name,
                    producer = producer,
                    country = country,
                    harvest_year = harvestYear,
                    type = spinnerWineType.selectedItem.toString(),
                    rate = rate,
                    description = txtAddWineDescription.text.toString().trim(),
                    reviews = txtAddWineReviews.text.toString().split("\n").map { it.trim() }.filter { it.isNotEmpty() },
                    grapes = getSelectedGrapes(),
                    taste_characteristics = TasteCharacteristics(
                        lightness = spinnerLightness.selectedItemPosition + 1,
                        tannin = spinnerTannin.selectedItemPosition + 1,
                        dryness = spinnerDryness.selectedItemPosition + 1,
                        acidity = spinnerAcidity.selectedItemPosition + 1
                    ),
                    food_pair = getSelectedFoodPairs(),
                    sale_price = salePrice,
                    discount = discount / 100, // Convert percentage to decimal
                    stock = stock
                )

            } catch (e: NumberFormatException) {
                Toast.makeText(context, "Please enter valid numeric values", Toast.LENGTH_SHORT).show()
                return null
            } catch (e: IllegalArgumentException) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                return null
            }
        }
    }


    private fun getSelectedGrapes(): List<String> = mutableListOf<String>().apply {
        binding.apply {
            if (checkBoxChardonnay.isChecked) add("Chardonnay")
            if (checkBoxSauvignonBlanc.isChecked) add("Sauvignon Blanc")
            if (checkBoxPinotGrigio.isChecked) add("Pinot Grigio")
            if (checkBoxCabernetSauvignon.isChecked) add("Cabernet Sauvignon")
            if (checkBoxMalbec.isChecked) add("Malbec")
            if (checkBoxZinfandel.isChecked) add("Zinfandel")
            if (checkBoxTempranillo.isChecked) add("Tempranillo")
            if (checkBoxSangiovese.isChecked) add("Sangiovese")
            if (checkBoxGrenache.isChecked) add("Grenache")
            if (checkBoxPinotNoir.isChecked) add("Pinot Noir")
            if (checkBoxSyrah.isChecked) add("Syrah")
            if (checkBoxMerlot.isChecked) add("Merlot")
        }
    }

    private fun getSelectedFoodPairs(): List<String> = mutableListOf<String>().apply {
        binding.apply {
            if (checkBoxStake.isChecked) add("Stake")
            if (checkBoxLamb.isChecked) add("Lamb")
            if (checkBoxChicken.isChecked) add("Chicken")
            if (checkBoxPork.isChecked) add("Pork")
            if (checkBoxPasta.isChecked) add("Pasta")
            if (checkBoxAgedCheeses.isChecked) add("Aged Cheeses")
            if (checkBoxSeafood.isChecked) add("Seafood")
            if (checkBoxFish.isChecked) add("Fish")
            if (checkBoxFish.isChecked) add("Fish")
            if (checkBoxSalad.isChecked) add("Salad")
            if (checkBoxCharcuterie.isChecked) add("Charcuterie")
            if (checkBoxDarkChocolate.isChecked) add("Dark Chocolate")
            if (checkBoxOtherFoods.isChecked) add("Other Foods")
        }
    }

    private fun uploadImageAndUpdateWine(wine: WineModel) {
        val imageName = "wine_${System.currentTimeMillis()}.jpg"
        val imageRef = firebaseStorage.child(imageName)

        imageUri?.let { uri ->
            imageRef.putFile(uri)
                .addOnSuccessListener {
                    val updatedWine = wine.copy(image_path = imageName)
                    updateWineInBackend(updatedWine)
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        context,
                        "Failed to upload image: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun updateWineInBackend(wine: WineModel) {
        val wineId = wine.id.toString()
        Log.d("UpdateWine", "Attempting to update wine with ID: $wineId")
        wineApi.updateWine(wineId, wine).enqueue(object : Callback<ResponseModel> {
            override fun onResponse(call: Call<ResponseModel>, response: Response<ResponseModel>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Wine updated successfully", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else {
                    val errorMsg = try {
                        response.errorBody()?.string() ?: "Unknown error"
                    } catch (e: Exception) {
                        "Error parsing response"
                    }
                    Log.e("UpdateWine", "Failed to update wine: $errorMsg")
                    Toast.makeText(
                        context,
                        "Failed to update wine: $errorMsg",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                Log.e("UpdateWine", "Network failure: ${t.message}")
                Toast.makeText(
                    context,
                    "Error updating wine: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}