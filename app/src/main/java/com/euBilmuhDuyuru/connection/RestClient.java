package com.euBilmuhDuyuru.connection;

import com.euBilmuhDuyuru.BuildConfig;
import com.euBilmuhDuyuru.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.squareup.okhttp.OkHttpClient;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

/**
 * Created by Yunus Emre Şeker on 03.03.2015.
 * -
 */

public class RestClient {

    private static API api;
    private static final String ROOT_URL = "https://bilmuh-y3seker.rhcloud.com";

    private RestClient() {
    }

    static {
        setup();
    }

    public static synchronized API get() {
        return api;
    }

    private static void setup() {
        // 2015-05-29T11:21:54.000Z
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();
        //Gson gson = new GsonBuilder().registerTypeAdapter(Date.class,new DateDeserializer()).create();
        OkHttpClient client = new OkHttpClient();
        client.setRetryOnConnectionFailure(true);
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(ROOT_URL)
                .setLogLevel(RestAdapter.LogLevel.BASIC)
                .setClient(new OkClient(client))
                .setConverter(new GsonConverter(gson))
                .build();
        api = restAdapter.create(API.class);
    }

    private static class DateDeserializer implements JsonDeserializer<Date> {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Utils.trLoc);
        private final String[] DATE_FORMATS = new String[]{
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "MMM dd, yyyy HH:mm:ss",
                "MMM dd, yyyy"
        };

        @Override
        public Date deserialize(JsonElement jsonElement, Type typeOF,
                                JsonDeserializationContext context) throws JsonParseException {
            dateFormat.setTimeZone(Utils.tzTurkey);
            // for (String format : DATE_FORMATS) {
            try {
                return dateFormat.parse(jsonElement.getAsString());
            } catch (ParseException e) {
                if (BuildConfig.DEBUG)
                    e.printStackTrace();
            }
            //}
            throw new JsonParseException("Unparseable date: \"" + jsonElement.getAsString()
                    + "\". Supported formats: " + Arrays.toString(DATE_FORMATS));
        }
    }
}
