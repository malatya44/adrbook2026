package tmgd.adrbook.com

import android.app.Application
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ADRBookApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Google Mobile Ads SDK on a background thread
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@ADRBookApp) {}
        }
    }
}
