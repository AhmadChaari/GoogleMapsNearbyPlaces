package com.example.priyanka.maps;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage;

public class Subscriber extends AsyncTask {
    Context    c;

    public Subscriber(Context context) {
        this.c = context;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        String topic = "foo/sub";
        String clientId = MqttClient.generateClientId();
        final MqttAndroidClient client =
                new MqttAndroidClient(c, "tcp://34.243.152.115:5555",
                        clientId);
        MqttConnectOptions options = new MqttConnectOptions();

        int qos = 1;
        try {
            final IMqttToken subToken = client.subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    asyncActionToken.getActionCallback();
                    MqttWireMessage msg= asyncActionToken.getResponse();
                    msg.toString();
                    Toast.makeText(c, "msg"+msg, Toast.LENGTH_SHORT).show();
                }


                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

        return null;
    }
}
