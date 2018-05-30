package com.example.priyanka.maps;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Priyanka
 */

public class DataParser {
    private static final String TAG = "hello";
    Context c;

    public DataParser(Context c) {
        this.c = c;
    }

    public HashMap<String, String> getPlace(JSONObject googlePlaceJson) {
        HashMap<String, String> googlePlaceMap = new HashMap<>();
        String placeName = "--NA--";
        String vicinity = "--NA--";
        String latitude = "";
        String longitude = "";
        String reference = "";

        Log.v("DataParser", "jsonobject =" + googlePlaceJson.toString());


        try {
            if (!googlePlaceJson.isNull("name")) {
                placeName = googlePlaceJson.getString("name");
            }
            if (!googlePlaceJson.isNull("vicinity")) {
                vicinity = googlePlaceJson.getString("vicinity");
            }

            latitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lng");

            reference = googlePlaceJson.getString("reference");

            googlePlaceMap.put("place_name", placeName);
            googlePlaceMap.put("vicinity", vicinity);
            googlePlaceMap.put("lat", latitude);
            googlePlaceMap.put("lng", longitude);
            googlePlaceMap.put("reference", reference);


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return googlePlaceMap;

    }

    private List<HashMap<String, String>> getPlaces(JSONArray jsonArray) {
        int count = jsonArray.length();
        List<HashMap<String, String>> placelist = new ArrayList<>();
        HashMap<String, String> placeMap = null;

        for (int i = 0; i < count; i++) {
            try {
                placeMap = getPlace((JSONObject) jsonArray.get(i));
                placelist.add(placeMap);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        JSONObject test = new JSONObject();
        try {
            test.put("places", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("json-data-test ", test.toString());
        //postJSONObject("http://197.26.55.141:5901/savePlaces.php",test);

        Log.d(TAG, "" + test.toString());
        Toast.makeText(c, "" + test.toString(), Toast.LENGTH_SHORT).show();
        sendJsonMQTT(test);
        //receiveJSONObject();
        return placelist;
    }

    public List<HashMap<String, String>> parse(String jsonData) {
        JSONArray jsonArray = null;
        JSONObject jsonObject;

        Log.d("json data", jsonData);
        Toast.makeText(c, ""+jsonData, Toast.LENGTH_SHORT).show();
        Log.v("json data", jsonData);
        try {
            jsonObject = new JSONObject(jsonData);
            jsonArray = jsonObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return getPlaces(jsonArray);
    }


    /*public void receiveJSONObject (){
        String topic = "foo/sub";
        String clientId = MqttClient.generateClientId();
        final MqttAndroidClient client =
                new MqttAndroidClient(c, "tcp://34.243.152.115:5555",
                        clientId);
        MqttConnectOptions options = new MqttConnectOptions();
        Toast.makeText(c, "hello sub", Toast.LENGTH_SHORT).show();
        int qos = 1;
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String msg = new String(message.getPayload());
                Toast.makeText(c, ""+msg, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });


    }*/
    public void receiveJSONObject() {

        String clientId = MqttClient.generateClientId();
        final MqttAndroidClient client =
                new MqttAndroidClient(c, "tcp://34.243.152.115:5555",
                        clientId);
        MqttConnectOptions options = new MqttConnectOptions();


        try {
            options.setUserName("Ahmed");
            options.setPassword("123456".toCharArray());

            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                public static final String TAG = "shi";

                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d(TAG, "onSuccess");
                    Toast.makeText(c, "subscribing ...", Toast.LENGTH_SHORT).show();
                    setSubscription(client);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "onFailure");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String msg = new String(message.getPayload());
                Toast.makeText(c, "" + msg, Toast.LENGTH_SHORT).show();
                JSONObject jsonObject = new JSONObject(msg);
                Toast.makeText(c, ""+jsonObject.toString(), Toast.LENGTH_SHORT).show();
                Log.d("Hello",jsonObject.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

    }

    private void setSubscription(MqttAndroidClient client) {
        try {
            client.subscribe("foo/sub",0);
            Toast.makeText(c, "Subscribed to foo/sub", Toast.LENGTH_SHORT).show();
        } catch (MqttSecurityException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    private void sendJsonMQTT(final JSONObject jsonArray) {

        String clientId = MqttClient.generateClientId();
        final MqttAndroidClient client =
                new MqttAndroidClient(c, "tcp://34.243.152.115:5555",
                        clientId);
        MqttConnectOptions options = new MqttConnectOptions();


        try {
            options.setUserName("Ahmed");
            options.setPassword("123456".toCharArray());

            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                public static final String TAG = "shi";

                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d(TAG, "onSuccess");
                    String topic = "foo/bar";
                    String payload = "the payload";
                    byte[] encodedPayload = new byte[0];
                    try {
                        encodedPayload = payload.getBytes("UTF-8");
                        MqttMessage message = new MqttMessage(encodedPayload);
                        message.setPayload(jsonArray.toString().getBytes());
                        client.publish("foo", message);

                    } catch (UnsupportedEncodingException | MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "onFailure");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }




}