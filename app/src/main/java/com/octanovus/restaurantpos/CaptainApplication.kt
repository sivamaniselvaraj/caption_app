package com.octanovus.restaurantpos

class CaptainApplication : android.app.Application() {
    override fun onCreate() {
        super.onCreate()
        _root_ide_package_.com.octanovus.restaurantpos.print.PrinterSettings.init(this)
    }
}
