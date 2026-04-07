package edu.cit.salonga.assetflow

import android.app.Application
import edu.cit.salonga.assetflow.utils.TokenManager

class AssetFlowApp : Application() {
    override fun onCreate() {
        super.onCreate()
        TokenManager.init(this)
    }
}
