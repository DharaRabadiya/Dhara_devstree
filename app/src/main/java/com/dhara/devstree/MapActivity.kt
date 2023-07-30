package com.dhara.devstree

import DirectionsService
import android.content.Intent
import android.opengl.Visibility
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dhara.devstree.adapter.SearchAutoCompleteAdapter
import com.dhara.devstree.datamodel.DirectionsResponse
import com.dhara.devstree.datamodel.Item
import com.dhara.devstree.db.AppDatabase
import com.dhara.devstree.repo.ItemRepository
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.android.PolyUtil
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    private lateinit var map: GoogleMap
    private lateinit var placesClient: PlacesClient
    private lateinit var itemRepository: ItemRepository
    private lateinit var searchAutoComplete: AutoCompleteTextView
    private lateinit var searchResultListView: ListView
    lateinit var txtBtnSave: Button

    var current_place_id: String = ""
    var current_place_description: String = ""
    var current_lat: Double = 0.0
    var current_lng: Double = 0.0

    var my_lat: Double = 0.0
    var my_lng: Double = 0.0
    var marker_title: String = ""
    var place_id: String = ""
    var show_path: String = ""
    var first_lat: Double = 0.0
    var first_lng: Double = 0.0
    var id: Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val itemDao = AppDatabase.getDatabase(applicationContext).itemDao()
        itemRepository = ItemRepository(itemDao)

        findById()
        getDataFromIntent()
        initMap()
        setupSearchBar()
        setListener()
    }

    fun initMap(){
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            map = googleMap
            setupMap(map)
        }
        Places.initialize(applicationContext, "AIzaSyBSNyp6GQnnKlrMr7hD2HGiyF365tFlK5U")

        placesClient = Places.createClient(this)
    }

    fun findById(){
        txtBtnSave = findViewById(R.id.txtBtnSave)
        searchAutoComplete = findViewById(R.id.searchAutoComplete)
        searchResultListView = findViewById(R.id.searchResultListView)
    }

    fun getDataFromIntent(){
        my_lat = intent.getDoubleExtra("lat", 0.0)
        my_lng = intent.getDoubleExtra("lng", 0.0)
        id = intent.getLongExtra("id", 0)
        marker_title = intent.getStringExtra("marker_title").toString()
        show_path = intent.getStringExtra("show_path").toString()
        place_id = intent.getStringExtra("place_id").toString()
        first_lat = intent.getDoubleExtra("first_lat",0.0)
        first_lng = intent.getDoubleExtra("first_lng",0.0)
    }
    val myCoroutineScope = CoroutineScope(Dispatchers.Main)

    fun setListener() {

        txtBtnSave.setOnClickListener(View.OnClickListener {

            if (my_lng != 0.0) {
                updateData()
            } else {
               insertData()
            }

        })
    }

    fun updateData(){
        lateinit var items: List<Item>
        itemRepository.allItems.observe(this, { items2 ->
            items = items2
        })

        val newItem : Item
        if(first_lng == 0.0 && first_lat == 0.0){
            newItem = Item(
                id = id,
                place_id = current_place_id,
                place_description = current_place_description,
                place_lat = current_lat,
                place_lng = current_lng,
                distance = 0.0
            )
        }
        else{

            val point1 = LatLng(first_lat, first_lng) // San Francisco
            val point2 = LatLng(current_lat, current_lng)
            val distanceInKilometers = calculateDistanceInKilometers(point1, point2)

            newItem = Item(
                id = id,
                place_id = current_place_id,
                place_description = current_place_description,
                place_lat = current_lat,
                place_lng = current_lng,
                distance = distanceInKilometers
            )
        }

        myCoroutineScope.launch {
            itemRepository.update(newItem)

            for(item in items){
                val point1 = LatLng(current_lat, current_lng)
                val point2 = LatLng(item.place_lat, item.place_lng)
                var distanceInKilometers = calculateDistanceInKilometers(point1, point2)
                item.distance = distanceInKilometers.toDouble()
                itemRepository.update(item)
            }
        }
        val intent = Intent(this, ListActivity::class.java)
        startActivity(intent)
    }

    fun insertData(){
        lateinit var newItem :Item
        if(first_lng == 0.0 && first_lat == 0.0){
            newItem = Item(
                place_id = current_place_id, place_description = current_place_description,
                place_lat = current_lat, place_lng = current_lng,
                distance = 0.0
            )
        }
        else{
            val point1 = LatLng(first_lat, first_lng) // San Francisco
            val point2 = LatLng(current_lat, current_lng)
            val distanceInKilometers = calculateDistanceInKilometers(point1, point2)
            newItem = Item(
                place_id = current_place_id, place_description = current_place_description,
                place_lat = current_lat, place_lng = current_lng,
                distance = distanceInKilometers
            )
        }

        myCoroutineScope.launch {
            itemRepository.insert(newItem)
        }
        val intent = Intent(this, ListActivity::class.java)
        startActivity(intent)
    }


    fun calculateDistanceInKilometers(point1: LatLng, point2: LatLng): Double {
        val earthRadius = 6371.0 // Earth's radius in kilometers
        val lat1 = Math.toRadians(point1.latitude)
        val lon1 = Math.toRadians(point1.longitude)
        val lat2 = Math.toRadians(point2.latitude)
        val lon2 = Math.toRadians(point2.longitude)

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        val distanceInKilometers = earthRadius * c

        return BigDecimal(distanceInKilometers).setScale(2, RoundingMode.HALF_EVEN).toDouble()
    }

    private fun setupMap(map: GoogleMap) {

        val initialLocation = LatLng(my_lat, my_lng)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 12f))

        val markerLocation = LatLng(my_lat, my_lng)
        map.addMarker(MarkerOptions().position(markerLocation).title(marker_title))

        if (show_path.equals("true")) {
            //showPathBetweenPoints(map)
            txtBtnSave.visibility = View.GONE
            drawRoute(map)
        }
    }

    private fun setupSearchBar() {
        val searchAdapter = SearchAutoCompleteAdapter(this, android.R.layout.simple_list_item_1)
        searchAutoComplete.setAdapter(searchAdapter)


        searchAutoComplete.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    hideSearchResults()
                } else {
                    showSearchResults(s.toString())
                }
            }
        })

    }


    private fun showSearchResults(query: String) {
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                val predictions = response.autocompletePredictions

                val resultList = mutableListOf<PlaceItem>()
                for (prediction in predictions) {
                    resultList.add(
                        PlaceItem(
                            prediction.getPrimaryText(null).toString(),
                            prediction.placeId
                        )
                    )
                }

                val searchAdapter =
                    SearchAutoCompleteAdapter(this, android.R.layout.simple_list_item_1, resultList)
                searchResultListView.adapter = searchAdapter
                searchResultListView.visibility = View.VISIBLE


                searchResultListView.setOnItemClickListener(OnItemClickListener { parent, view, position, id ->
                    searchResultListView.visibility = View.GONE

                    val placeItem = searchAdapter.getItem(position)
                    placeItem?.let {
                        searchAutoComplete.setText(it.primaryText)
                        hideSearchResults()
                        moveCameraToPlace(it)
                    }

                })

            }
            .addOnFailureListener { exception ->

            }
    }

    private fun hideSearchResults() {
        searchResultListView.visibility = View.GONE
    }

    private fun moveCameraToPlace(placeItem: PlaceItem) {
        val initialLocation = LatLng(37.7749, -122.4194)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 12f))

        val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)


        val request = FetchPlaceRequest.builder(placeItem.placeId, placeFields).build()

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                val place = response.place
                val latLng = place.latLng

                current_place_id = placeItem.placeId
                current_place_description = placeItem.primaryText
                current_lat = latLng.latitude
                current_lng = latLng.longitude
                // Move the camera to the selected place and add a marker
                latLng?.let {
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 12f))
                    map.clear()
                    map.addMarker(MarkerOptions().position(it).title(placeItem.primaryText))
                }
            }
            .addOnFailureListener { exception ->
            }
    }





    private fun drawRoute(googleMap:GoogleMap) {


        lateinit var items: List<Item>
        val latLangList: MutableList<LatLng> = mutableListOf()


        itemRepository.allItems.observe(this, { items2 ->
            items = items2

            if (items.size > 1) {
                for (latLng in items) {
                    val latitude = latLng.place_lat
                    val longitude = latLng.place_lng
                    val latLng = LatLng(latitude, longitude)
                    latLangList.add(latLng)
                    println("Latitude: $latitude, Longitude: $longitude")
                }


                val origin = LatLng(latLangList.get(0).latitude, latLangList.get(0).longitude)
                val destination = LatLng(latLangList.get(latLangList.size - 1).latitude, latLangList.get(latLangList.size - 1).longitude)


               // googleMap.addMarker(MarkerOptions().position(origin).title(items.get(0).place_description))
                //googleMap.addMarker(MarkerOptions().position(destination).title(items.get(items.size - 1).place_description))
                for (item in items) {
                    googleMap.addMarker(MarkerOptions().position(LatLng(item.place_lat,item.place_lng)).title(item.place_description))
                }
                val waypointStrings = latLangList.joinToString("|") { "${it.latitude},${it.longitude}" }

                val apiKey = "AIzaSyBSNyp6GQnnKlrMr7hD2HGiyF365tFlK5U"
                val retrofit = createRetrofit()
                val directionsService = retrofit.create(DirectionsService::class.java)
                val call = directionsService.getDirections(
                    origin = "${origin.latitude},${origin.longitude}",
                    destination = "${destination.latitude},${destination.longitude}",
                    waypoints = waypointStrings,
                    apiKey = apiKey
                )

                call.enqueue(object : Callback<DirectionsResponse> {
                    override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
                        if (response.isSuccessful) {
                            val directionsResponse = response.body()
                            if (directionsResponse?.status == "OK") {
                                val path = directionsResponse.routes.firstOrNull()?.overviewPolyline?.points

                                path?.let {
                                    val decodedPath = PolyUtil.decode(path)

                                    val polylineOptions = PolylineOptions().apply {
                                        addAll(decodedPath)
                                        width(8f)
                                        color(ContextCompat.getColor(this@MainActivity, R.color.black)) // Color of the line
                                    }

                                    googleMap.addPolyline(polylineOptions)

                                    val bounds = LatLngBounds.builder()
                                        .include(origin)
                                        .include(destination)
                                        .also { builder ->
                                            for (waypoint in latLangList) {
                                                builder.include(waypoint)
                                            }
                                        }
                                        .build()

                                    val padding = 100
                                    val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
                                    googleMap.animateCamera(cameraUpdate)
                                }
                            } else {
                                Toast.makeText(this@MainActivity, "Directions API Error: ${directionsResponse?.status}", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@MainActivity, "Failed to get directions.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                        Toast.makeText(this@MainActivity, "Error fetching directions.", Toast.LENGTH_SHORT).show()
                    }
                })

            }

        })

    }

    private fun createRetrofit(): Retrofit {
        val gson: Gson = GsonBuilder().setLenient().create()
        val client: OkHttpClient = OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }


}

data class PlaceItem(val primaryText: String, val placeId: String = "")