import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.winewms.R

class ReplenishStockDialogFragment(private val onReplenish: (Double, Int) -> Unit) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_replenish_stock, container, false)

        val costPriceInput = view.findViewById<EditText>(R.id.txtCostPrice)
        val stockInput = view.findViewById<EditText>(R.id.txtStock)
        val confirmButton = view.findViewById<Button>(R.id.btnConfirmReplenish)

        confirmButton.setOnClickListener {
            val costPrice = costPriceInput.text.toString().toDoubleOrNull()
            val stock = stockInput.text.toString().toIntOrNull()

            if (costPrice != null && stock != null && costPrice > 0 && stock > 0) {
                onReplenish(costPrice, stock)
                dismiss()
            } else {
                Toast.makeText(context, "Please enter valid values", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}
