import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BHIM_UPI
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.CRED_UPI
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.GPAY
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.KIWI_UPI
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.MOBIKWIK_UPI
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PAYTM
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PHONEPE
import com.plural_pinelabs.expresscheckoutsdk.common.ItemClickListener

class UpiAppsAdapter(
    private val upiApps: List<String>,
    private val itemClickListener: ItemClickListener<String>
) :
    RecyclerView.Adapter<UpiAppsAdapter.UpiAppViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UpiAppViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.upi_app_item, parent, false)
        return UpiAppViewHolder(view)
    }

    override fun onBindViewHolder(holder: UpiAppViewHolder, position: Int) {
        val upiApp = upiApps[position]
        holder.bind(upiApp, itemClickListener, position)
    }

    override fun getItemCount(): Int = upiApps.size

    class UpiAppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val appImage: ImageView = itemView.findViewById(R.id.upi_app_image)
        private val appName: TextView = itemView.findViewById(R.id.upi_app_name)
        private val parentLayout: View = itemView.findViewById(R.id.upi_item_parent)

        fun bind(upiApp: String, itemClickListener: ItemClickListener<String>, position: Int) {
            appImage.setImageResource(getImageResIdFromPackage(upiApp))
            appName.text = itemView.context.getString(getUpiNameFromPackage(upiApp))
            parentLayout.setOnClickListener {
                // Handle click event if needed
                itemClickListener.onItemClick(position, upiApp)
            }
        }

        private fun getImageResIdFromPackage(upiApp: String): Int {
            return when (upiApp) {
                GPAY -> R.drawable.ic_gpay_upi
                PHONEPE -> R.drawable.ic_phone_pe
                PAYTM -> R.drawable.wallet_paytm
                BHIM_UPI -> R.drawable.ic_bhim_upi
                CRED_UPI -> R.drawable.ic_cred_upi
                KIWI_UPI -> R.drawable.ic_kiwi_upi
                MOBIKWIK_UPI -> R.drawable.wallet_mobikwik
                else -> R.drawable.ic_upi // Default icon for unknown apps
            }
        }

        private fun getUpiNameFromPackage(upiApp: String): Int {
            return when (upiApp) {
                GPAY -> R.string.upi_app_google_pay
                PHONEPE -> R.string.upi_app_phonepe
                PAYTM -> R.string.upi_app_paytm
                BHIM_UPI -> R.string.upi_app_bhim_upi
                CRED_UPI -> R.string.upi_app_cred
                KIWI_UPI -> R.string.kiwi
                MOBIKWIK_UPI ->R.string.mokiwik
                else -> -1 // Default icon for unknown apps
            }
        }


    }
}
