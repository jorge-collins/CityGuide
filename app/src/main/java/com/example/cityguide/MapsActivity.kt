package com.example.cityguide

import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.cityguide.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import java.util.logging.Logger

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener {

    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var lastLocation: Location

    private var listaMarcadores: ArrayList<Marker>? = null

    private var currentLatLng: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)

        map.setOnMarkerDragListener(this)

        setUpMap()

        prepararMarcadores()
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        return false
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        map.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                lastLocation = location
                currentLatLng = LatLng(location.latitude, location.longitude)
                placeMarkerOnMap(currentLatLng!!)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14f))
            } else {
                Toast.makeText(this, "location igual a null", Toast.LENGTH_LONG).show()
            }
        }

    }


    private fun placeMarkerOnMap(location: LatLng) {
        val markerOptions =
            MarkerOptions()
                .position(location)
                .title("My current location")
                .snippet("Ubicación " + location.toString() )

        markerOptions.icon( BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE) )

        map.addMarker(markerOptions)
    }

    private fun prepararMarcadores() {
        listaMarcadores = ArrayList()
        map.setOnMapLongClickListener {
            location: LatLng? ->
            listaMarcadores?.add(
                map.addMarker( MarkerOptions()
                    .position(location!!)
                    .icon( BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN) )
                )
            )
            listaMarcadores?.last()!!.isDraggable = true

            val coordenadas = LatLng( listaMarcadores?.last()!!.position.latitude, listaMarcadores?.last()!!.position.longitude )

            val origen = "origin=" + currentLatLng?.latitude + "," + currentLatLng?.longitude + "&"

            val destino = "destination=" + coordenadas.latitude + "," + coordenadas.longitude + "&"

            val parametros = origen + destino + "sensor=false&mode=driving"

            /*
                Dejamos esta funcion comentada porque para usar los servicios de maps
                se requiere tener dado de alta un medio de pago para Google Cloud Platform.
                Habilitar la funcion para obtener el mensaje de error y lineamientos a seguir.

                Curse "Android y Kotlin Desde Cero a Profesional Completo +45 horas". Lecture 185.
             */
            // cargarURL("http://maps.googleapis.com/maps/api/directions/json?" + parametros)
        }
    }

    private fun cargarURL(url:String) {
        val queue = Volley.newRequestQueue(this)

        Log.d("HTTP", "cargar URL")

        val solicitud = StringRequest(Request.Method.GET, url, Response.Listener<String> {
                response ->
                Log.d("HTTP", response)
            }, Response.ErrorListener {
                error ->
                Log.d("HTTP", error.toString())
        })

        queue.add(solicitud)
    }

    override fun onMarkerDragStart(marcador: Marker?) {
        Log.d("DRAG", "Iniciando movimiento...")
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(marcador?.position, 15f))

    }

    override fun onMarkerDrag(marcador: Marker?) {
        title = marcador?.position?.latitude.toString()
    }

    override fun onMarkerDragEnd(marcador: Marker?) {
        Log.d("DRAG", "Drag end.")
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(marcador?.position, 14f))

    }

}