package com.example.startxplanify;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.content.pm.PackageManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.Request;  // Utilisez OkHttp3.Request





public class Map extends AppCompatActivity implements OnMapReadyCallback {

    private TextView locationTextView;

    private GoogleMap map;
    private AutoCompleteTextView addressInput;

    private Button searchButton, locationButton,Okbutton,routeButton;

    private TextView addressTextView;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng currentLocation;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);

        // Initialiser le champ de texte et le bouton
        addressInput = findViewById(R.id.autoCompleteTaskLocation);
        searchButton = findViewById(R.id.searchButton);
        addressTextView = findViewById(R.id.addressTextView);
        locationButton = findViewById(R.id.locationButton); // Bouton pour revenir à la localisation actuelle
        Okbutton = findViewById(R.id.buttonOK);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        routeButton = findViewById(R.id.routeButton);



        // Configurer la carte
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            System.out.println("Erreur: MapFragment est null !");
        }

        // Ajouter un écouteur d'événements pour le bouton de recherche
        searchButton.setOnClickListener(v -> {
            String address = addressInput.getText().toString();
            if (!address.isEmpty()) {
                geocodeAddress(address);  // Appeler la méthode de géocodage
            } else {
                Toast.makeText(Map.this, "Please Enter an Adresse", Toast.LENGTH_SHORT).show();
            }
        });

        addressTextView.setOnClickListener(v -> {
            String address = addressTextView.getText().toString();
            if (!address.isEmpty()) {
                geocodeAddress(address);  // Rechercher l'adresse sur la carte
            }
        });

        getCurrentLocation();

        // Ajouter un écouteur pour le bouton de retour à la localisation actuelle
        locationButton.setOnClickListener(v -> {
            if (currentLocation != null) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12));
            } else {
                Toast.makeText(Map.this, "Impossible de récupérer la localisation actuelle", Toast.LENGTH_SHORT).show();
            }
        });

        routeButton.setOnClickListener(v -> {
            if (currentLocation != null) {
                // Récupérer la destination recherchée à partir de l'adresse géocodée
                String address = addressInput.getText().toString();  // L'adresse recherchée par l'utilisateur
                if (!address.isEmpty()) {
                    geocodeAddress(address);  // Appeler la méthode de géocodage
                } else {
                    Toast.makeText(Map.this, "Veuillez entrer une adresse", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(Map.this, "Impossible de récupérer la localisation actuelle", Toast.LENGTH_SHORT).show();
            }
        });

    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID); // Activer la vue hybride pour un meilleur rendu
        enableLocation();
        getCurrentLocation();
    }

    private void enableLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        map.setMyLocationEnabled(true); // Afficher la position actuelle sur la carte
    }

    // Cette méthode récupère la localisation actuelle de l'utilisateur
    private void getCurrentLocation() {
        // Vérifier si la permission est accordée
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Si la permission n'est pas accordée, demander la permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        // Récupérer la localisation actuelle
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        // Si la localisation est trouvée, mettre un marqueur sur la carte
                        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        if (map != null) {
                            map.clear(); // Supprimer les anciens marqueurs
                            map.addMarker(new MarkerOptions().position(currentLocation).title("Ma position"));
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12));
                        }
                    } else {
                        Toast.makeText(Map.this, "Impossible de récupérer la localisation actuelle", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void geocodeAddress(String address) {
        Geocoder geocoder = new Geocoder(this);
        try {
            // Geocoding de l'adresse
            List<Address> addresses = geocoder.getFromLocationName(address, 1); // Limiter à 1 résultat
            if (addresses != null && !addresses.isEmpty()) {
                Address location = addresses.get(0); // On prend le premier résultat trouvé

                // Afficher l'adresse complète dans le TextView
                String fullAddress = location.getAddressLine(0);  // Cela récupère l'adresse complète sous forme de texte
                addressTextView.setText(fullAddress);  // Affiche l'adresse dans TextView

                // Optionnel : Si tu veux afficher l'adresse sur la carte sans latitude/longitude
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                map.clear(); // Nettoie les anciens marqueurs
                map.addMarker(new MarkerOptions().position(latLng).title(fullAddress));  // Affiche le marqueur
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12)); // Déplace la caméra vers la position

                // Calculer l'itinéraire entre la position actuelle et la destination recherchée
                if (currentLocation != null) {
                    getDirections(currentLocation, latLng);  // Correct : Utilisez latLng comme destination
                }
               else {
                Toast.makeText(this, "Adresse non trouvée", Toast.LENGTH_SHORT).show();
                    }

                // Passer l'adresse complète à l'activité précédente (ok)
                Okbutton.setOnClickListener(v -> {
                    // Créer un Intent pour renvoyer l'adresse complète à ok
                    Intent intent = new Intent();
                    intent.putExtra("selectedLocation", fullAddress);  // Passer l'adresse complète à ok

                    // Définir le résultat et revenir à l'activité appelante (ok)
                    setResult(RESULT_OK, intent);
                    finish();  // Fermer l'activité Map et revenir à ok
                });
            } else {
                Toast.makeText(this, "Adresse non trouvée", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur lors de la géocodification", Toast.LENGTH_SHORT).show();
        }
    }

    private void getDirections(LatLng origin, LatLng destination) {
        // URL pour appeler l'API Directions
        String originStr = origin.latitude + "," + origin.longitude;
        String destinationStr = destination.latitude + "," + destination.longitude;

        // Remplacer "YOUR_API_KEY" par votre propre clé API Google Maps Directions
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + originStr + "&destination=" + destinationStr + "&key=AIzaSyC0WM5v4QRV70_6HaW86hN1kwLLf2XtBhw";

        // Utiliser OkHttp pour envoyer la requête
        OkHttpClient client = new OkHttpClient();

        // Utiliser OkHttp3.Request au lieu de Picasso.Request
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .build();

        // Envoi de la requête de manière asynchrone
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Gérer les erreurs de réseau ici
                Log.e("MapActivity", "Erreur de réseau: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);

                        // Vérifier si 'routes' existe
                        if (jsonObject.has("routes")) {
                            JSONArray routes = jsonObject.getJSONArray("routes");

                            if (routes.length() > 0) {
                                JSONObject route = routes.getJSONObject(0);
                                JSONArray legs = route.getJSONArray("legs");

                                if (legs.length() > 0) {
                                    JSONObject leg = legs.getJSONObject(0);
                                    JSONArray steps = leg.getJSONArray("steps");

                                    List<LatLng> routePoints = new ArrayList<>();
                                    for (int i = 0; i < steps.length(); i++) {
                                        JSONObject step = steps.getJSONObject(i);

                                        // Vérifier la présence de "end_location"
                                        if (step.has("end_location")) {
                                            JSONObject endLocation = step.getJSONObject("end_location");
                                            double lat = endLocation.getDouble("lat");
                                            double lng = endLocation.getDouble("lng");
                                            routePoints.add(new LatLng(lat, lng));
                                        }
                                    }

                                    runOnUiThread(() -> addRouteToMap(routePoints));
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(Map.this, "Erreur lors du parsing", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    Log.e("MapActivity", "Response not successful");
                }
            }


        });
    }

    // Méthode pour ajouter l'itinéraire sur la carte
    private void addRouteToMap(List<LatLng> routePoints) {
        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(routePoints)
                .width(10)
                .color(Color.BLUE);  // Assurez-vous que la couleur est bien définie
        map.addPolyline(polylineOptions);
    }
}



