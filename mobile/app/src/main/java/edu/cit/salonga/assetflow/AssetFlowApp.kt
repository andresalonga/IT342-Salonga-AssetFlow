package edu.cit.salonga.assetflow

import android.app.Application
import edu.cit.salonga.assetflow.features.auth.utils.TokenManager

class AssetFlowApp : Application() {
    override fun onCreate() {
        super.onCreate()
        TokenManager.init(this)
    }
}
