/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2019. All rights reserved.
 */
package com.hdbmapsdemo.app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.UnicodeSet.EMPTY
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.huawei.hmf.tasks.OnSuccessListener
import com.huawei.hms.framework.common.StringUtils
import com.huawei.hms.location.*
import com.huawei.hms.maps.*
import com.huawei.hms.maps.model.*
import java.io.IOException
import java.util.*


/**
 * map activity entrance class
 */
class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    companion object {
        private const val TAG = "MapViewDemoActivity"
        private const val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
        private const val REQUEST_CODE = 100
        private var MY_LAT_LNG = LatLng(1.3840, 103.7470)
        private val RUNTIME_PERMISSIONS = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET
        )
    }

    private lateinit var hmap: HuaweiMap
    private lateinit var mMapView: MapView
    private var mMarker: Marker? = null
    private var mCircle: Circle? = null

    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "map onCreate:")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (!hasPermissions(this, *RUNTIME_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, RUNTIME_PERMISSIONS, REQUEST_CODE)
        }


        // get mapView by layout view
        mMapView = findViewById(R.id.mapView)
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        }
        // please replace "Your API key" with api_key field value in
        // agconnect-services.json if the field is null.
        MapsInitializer.setApiKey("CgB6e3x9gNB7Y9My5BhIr5cV94q0aExIkU6rqVapVVHr/A62/6YhjTc0t3VJS7XHBMNuRZ7b4vv7+i9G66bMhHgt")
        mMapView.onCreate(mapViewBundle)

        // get map by async method
        mMapView.getMapAsync(this)

        //Creating a Location Service Client
        mFusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this@MainActivity)

    }

    /**
     * Obtain the last known location
     */
    private fun getLastLocation() {
        try {
            val lastLocation =
                mFusedLocationProviderClient.lastLocation
            lastLocation.addOnSuccessListener(OnSuccessListener { location ->
                if (location == null) {
                    Log.i(TAG, "getLastLocation onSuccess location is null")
                    return@OnSuccessListener
                }
                Log.i(
                    TAG,
                    "getLastLocation onSuccess location[Longitude,Latitude]:${location.longitude},${location.latitude}"
                )
                MY_LAT_LNG = LatLng(location.latitude, location.longitude)

                return@OnSuccessListener
            }).addOnFailureListener { e ->
                Log.e(TAG, "getLastLocation onFailure:${e.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "getLastLocation exception:${e.message}")
        }
    }


    private fun getAddressFromLocation(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(this, Locale.ENGLISH)
        var final_address = ""
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses.size > 0) {
                val fetchedAddress = addresses[0]
                Log.v(TAG, "Reverse geocoding address number: ${addresses[0]}")
//                final_address += "${fetchedAddress.subAdminArea} ${fetchedAddress.postalCode}"
                final_address += fetchedAddress.postalCode
                return final_address

            } else {
                Log.e(TAG, "No address found")

            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }


    override fun onStart() {
        super.onStart()
        mMapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mMapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMapView.onDestroy()
    }


    override fun onMapReady(map: HuaweiMap) {
        Log.d(TAG, "onMapReady: ")
        getLastLocation()

        // after call getMapAsync method ,we can get HuaweiMap instance in this call back method
        hmap = map
        hmap.isMyLocationEnabled = true
        hmap.uiSettings.isMyLocationButtonEnabled = true

        // move camera by CameraPosition param ,latlag and zoom params can set here
        val build = CameraPosition.Builder().target(MY_LAT_LNG).zoom(15f).build()

        val cameraUpdate = CameraUpdateFactory.newCameraPosition(build)
        hmap.animateCamera(cameraUpdate)
        //onLongPress latlng
        hmap.setOnMapLongClickListener { latLng ->
            Toast.makeText(
                applicationContext,
                "onMapLongClick:$latLng",
                Toast.LENGTH_SHORT
            ).show()
//            val myCurrentAddress = getAddressFromLocation(MY_LAT_LNG.latitude, MY_LAT_LNG.longitude)
//            val destinationAddress = getAddressFromLocation(latLng.latitude, latLng.longitude)
//            Log.v(TAG, "my current address: ${myCurrentAddress}")
//            Log.v(TAG, "my current address: ${destinationAddress}")

//            Re-direct to Petal Maps
            val uriString =
                "mapapp://navigation?saddr=${MY_LAT_LNG.latitude},${MY_LAT_LNG.longitude}&daddr=${latLng.latitude},${latLng.longitude}&language=en&type=walk"

            val content_url: Uri = Uri.parse(uriString)
            val intent = Intent(Intent.ACTION_VIEW, content_url)
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
        }

//        hmap.setMaxZoomPreference(5f)
//        hmap.setMinZoomPreference(2f)

        // mark can be add by HuaweiMap
//        mMarker = hmap.addMarker(MarkerOptions().position(LAT_LNG)
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.badge_ph))
//                .clusterable(true))
//        mMarker?.showInfoWindow()

        // circle can be add by HuaweiMap
//        mCircle = hmap.addCircle(CircleOptions().center(LatLng(1.3840, 103.7470)).radius(50.0).fillColor(Color.GREEN))
//        mCircle?.fillColor = Color.TRANSPARENT
    }

    override fun onPause() {
        mMapView.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mMapView.onResume()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMapView.onLowMemory()
    }

    private fun hasPermissions(context: Context, vararg permissions: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissions != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }
}