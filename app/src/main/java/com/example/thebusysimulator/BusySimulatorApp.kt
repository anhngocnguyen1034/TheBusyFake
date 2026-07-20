package com.example.thebusysimulator

import android.app.Application
import com.anhnn.ads.AdFormat
import com.anhnn.ads.Ads
import com.anhnn.ads.AdsConfig
import com.example.thebusysimulator.ads.AdNames
import com.example.thebusysimulator.ads.RemoteConfigManager

/**
 * Application: khởi tạo Remote Config + module quảng cáo sớm nhất có thể.
 *
 * Module ads KHÔNG phụ thuộc Firebase — [Ads.init] chỉ nhận các lambda đọc dữ liệu app
 * (Remote Config) bơm vào. Ad unit fallback theo định dạng (test unit) nằm sẵn trong
 * [RemoteConfigManager].
 */
class BusySimulatorApp : Application() {

    override fun onCreate() {
        super.onCreate()

        RemoteConfigManager.init(this)

        Ads.init(
            AdsConfig(
                adsEnabled = { RemoteConfigManager.adsEnabled() },
                adUnitId = { name ->
                    when (AdNames.formatOf(name)) {
                        AdFormat.INTERSTITIAL -> RemoteConfigManager.interAdUnitId(name)
                        AdFormat.NATIVE -> RemoteConfigManager.nativeAdUnitId(name)
                        AdFormat.BANNER -> RemoteConfigManager.bannerAdUnitId(name)
                        AdFormat.APP_OPEN -> RemoteConfigManager.appOpenAdUnitId(name)
                        null -> ""
                    }
                },
                adFormat = { name -> AdNames.formatOf(name) },
                interCooldownMs = { RemoteConfigManager.interMinIntervalMs() },
            )
        )

        // App Open: tự hiện khi user quay lại app từ background.
        Ads.setupAppOpen(this, AdNames.APP_OPEN_RESUME)
    }
}
