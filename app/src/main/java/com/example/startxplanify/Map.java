package com.example.startxplanify;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
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
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

import android.Manifest;
import android.content.pm.PackageManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONObject;


public class Map extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private AutoCompleteTextView addressInput;

    private Button searchButton, locationButton;

    private TextView addressTextView;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng currentLocation;
    private LatLng selectedLocation; // Pour stocker la position de l'adresse sélectionnée

    private ImageView addressImageView;  // ImageView pour afficher l'image de l'adresse

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);

        // Initialiser le champ de texte et le bouton
        addressInput = findViewById(R.id.autoCompleteTaskLocation);
        searchButton = findViewById(R.id.searchButton);
        addressTextView = findViewById(R.id.addressTextView);
        locationButton = findViewById(R.id.locationButton); // Bouton pour revenir à la localisation actuelle
        addressImageView = findViewById(R.id.addressImageView);  // ImageView pour la photo

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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
                Toast.makeText(Map.this, "Veuillez entrer une adresse", Toast.LENGTH_SHORT).show();
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
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap; // Assigner la carte
        getCurrentLocation();
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
            // Obtenir la liste des adresses
            List<Address> addresses = geocoder.getFromLocationName(address, 1); // Limiter à 1 résultat
            if (addresses != null && !addresses.isEmpty()) {
                Address location = addresses.get(0); // Prendre la première adresse trouvée
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                // Ajouter un marqueur à l'adresse trouvée
                map.clear(); // Nettoyer les anciens marqueurs
                map.addMarker(new MarkerOptions().position(latLng).title(address));
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));

                // Afficher l'adresse dans le TextView
                addressTextView.setText(address);

l
            } else {
                Toast.makeText(this, "Adresse non trouvée", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur lors de la géocodification", Toast.LENGTH_SHORT).show();
        }
    }


}
