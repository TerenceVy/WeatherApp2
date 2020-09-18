package com.example.basicweatherapp;
import com.example.basicweatherapp.Retrofit.ApiClient;
import com.example.basicweatherapp.Retrofit.WeatherApi;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.text.DecimalFormat;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import static android.content.ContentValues.TAG;


public class CurrentWeather extends Fragment {
    private SharedViewModel viewModel;

    private static DecimalFormat decimal = new DecimalFormat("0.00");
    private TextView text;
    public String cityName = "Paris";
    public CharSequence city;

    private static WeatherApi WeatherApi;

    public CurrentWeather() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * Connexion to the API
         */
        this.configureRetrofit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(getActivity()).get(SharedViewModel.class);
        city = viewModel.getText().getValue();
        if (!(city == null)) {
            cityName = city.toString();
        }
        this.getCurrentWeather(container, cityName);
        this.getForecastWeather(container, cityName);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_current_weather, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * Retrofit's configuration
     */
    private void configureRetrofit(){
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://community-open-weather-map.p.rapidapi.com/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        WeatherApi = retrofit.create(WeatherApi.class);

    }

    /**
     * Sends GET request, Get Current Weather
     * Also set views with the data
     */
    private void  getCurrentWeather(final ViewGroup container, String city){
        //WeatherApi weatherApi= ApiClient.getClient().create(WeatherApi.class);
        WeatherApi.getCurrentWeather("metric",city).enqueue(new Callback<JsonObject>() {
            public void onResponse(Call<JsonObject> call, Response <JsonObject> response) {
                if (response.code() == 200) {
                    JsonObject Main = response.body().get("main").getAsJsonObject();
                    JsonObject Wind = response.body().get("wind").getAsJsonObject();
                    JsonObject Body = response.body().getAsJsonObject();
                    String json_text;
                    double fahrenheit;

                    text = container.findViewById(R.id.text_temp); //Temperature
                    fahrenheit = ((Main.get("temp").getAsDouble()) * 1.8 + 32);
                    json_text = "Actuelle : " + Main.get("temp").getAsString() + " °C / " + String.valueOf(decimal.format(fahrenheit)) + " °F";
                    text.setText(json_text);

                    text = container.findViewById(R.id.text_ville); //Ville
                    json_text = Body.get("name").getAsString();
                    text.setText(json_text);

                    text = container.findViewById(R.id.text_temp_min); //Temperature min
                    fahrenheit = ((Main.get("temp_min").getAsDouble()) * 1.8 + 32);
                    json_text = "Minimale : " + Main.get("temp_min").getAsString() + " °C / " + String.valueOf(decimal.format(fahrenheit)) + " °F";
                    text.setText(json_text);

                    text = container.findViewById(R.id.text_temp_max); //Temperature max
                    fahrenheit = ((Main.get("temp_max").getAsDouble()) * 1.8 + 32);
                    json_text = "Maximale : " + Main.get("temp_max").getAsString() + " °C / " + String.valueOf(decimal.format(fahrenheit)) + " °F";
                    text.setText(json_text);

                    text = container.findViewById(R.id.text_humidite); //Humidite
                    json_text = "Humidité : " + Main.get("humidity").getAsString() + " %";
                    text.setText(json_text);

                    text = container.findViewById(R.id.text_pression); //Pression
                    json_text = "Pression : " + Main.get("pressure").getAsString() + " hPa";
                    text.setText(json_text);

                    text = container.findViewById(R.id.text_vitesse_vent); //Vitesse du vent
                    json_text = "Vitesse : " + Wind.get("speed").getAsString() + " Km/h";
                    text.setText(json_text);

                    text = container.findViewById(R.id.text_direction_vent); //Direction du vent
                    json_text = "Direction : " + Wind.get("deg").getAsString() + "°";
                    text.setText(json_text);
                }
            }

            public void onFailure(Call<JsonObject> call, Throwable t){
               Log.e(TAG, "onFailure: " + t.getMessage());
            }
        });
    }


    private void  getForecastWeather(final ViewGroup container, String city) {

        WeatherApi.getForecastWeather("metric", city).enqueue(new Callback<JsonObject>() {
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.code() == 200) {
                    JsonArray Body = response.body().get("list").getAsJsonArray();
                    JsonObject Main;
                    JsonObject listElements;
                    String listDateTime;
                    String json_text;
                    String Hour;
                    double fahrenheit;
                    int compteur = 1;

                    for (int i = 0; i < 40; i++) { //Boucle pour faire le tour des 40 elements de la liste retournee par la requete
                        listElements = Body.get(i).getAsJsonObject();
                        listDateTime = listElements.get("dt_txt").getAsString();
                        Main = listElements.get("main").getAsJsonObject();
                        Hour = listDateTime.substring(11,13);

                        if (Hour.equals("12")){
                            text = container.findViewById(R.id.text_temp+compteur);
                            fahrenheit = ((Main.get("temp").getAsDouble()) * 1.8 + 32);
                            json_text = Main.get("temp").toString() + " °C / " + String.valueOf(decimal.format(fahrenheit)) + " °F";
                            text.setText(json_text);
                            compteur++;
                        }
                    }
                }
            }

            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage());
            }
        });
    }
}