package tw.com.mqtts;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.UUID;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.support.design.widget.TabLayout;
//import android.support.v4.view.ViewPager;

import android.speech.tts.TextToSpeech;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.util.Locale;
/**
 * A Mqtt topic subscriber
 *
 */
public class MainActivity extends AppCompatActivity {


    static boolean isShutdown = false;

//    TabLayout tabLayout;
//    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void run(String... args) throws IOException {
        System.out.println("TopicSubscriber initializing...");

        String host = "tcp://120.126.18.132:1883";
        String username = "android";
//        String TAG = "data";
//        String password = "";
//        if (args.length > 2) password = args[2];



        try {
            // Create an Mqtt client
            MqttClient mqttClient = new MqttClient(host, "HelloWorldSub_" + UUID.randomUUID().toString().substring(0,8));
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setUserName(username);
//            if (args.length > 2) connOpts.setPassword(password.toCharArray());

            // Connect the client
            System.out.println("Connecting to Solace messaging at "+host);
            mqttClient.connect(connOpts);
            System.out.println("Connected");

            // Topic filter the client will subscribe to
            final String subTopic = "dialogflow_output";

            // Callback - Anonymous inner-class for receiving messages
            mqttClient.setCallback(new MqttCallback() {

                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    // Called when a message arrives from the server that
                    // matches any subscription made by the client
//                    String msg= new String(message.getPayload());
                    String time = new Timestamp(System.currentTimeMillis()).toString();
                    String messageTxt = new String( message.getPayload(), "UTF-8" );

//                    String msg1 = new String(message.getPayload());

                    System.out.println("\nReceived a Message!" +
                            "\n\tTime:    " + time +
                            "\n\tTopic:   " + topic +
                            "\n\tMessage: " + new String(message.getPayload()) +
                            "\n\tQoS:     " + message.getQos() + "\n");

                }

                public void connectionLost(Throwable cause) {
                    System.out.println("Connection to Solace messaging lost!" + cause.getMessage());
                    isShutdown = true;
                }

                public void deliveryComplete(IMqttDeliveryToken token) {
                }

            });

            // Subscribe client to the topic filter and a QoS level of 0
            System.out.println("Subscribing client to topic: " + subTopic);
            mqttClient.subscribe(subTopic, 0);
            System.out.println("Subscribed. Press [ENTER] to quit.");

            // Wait for the message to be received

            try {
                while (System.in.available() == 0 && !isShutdown) {
                    Thread.sleep(1000);  // wait 1 second
                }
            } catch (InterruptedException e) {
                // Thread.sleep() interrupted... probably getting shut down
            }

            // Disconnect the client
            mqttClient.disconnect();
            System.out.println("Exiting");

            System.exit(0);
        } catch (MqttException me) {
            System.out.println("Exception:   " + me);
            System.out.println("Reason Code: " + me.getReasonCode());
            System.out.println("Message:     " + me.getMessage());
            if (me.getCause() != null) System.out.println("Cause:       " + me.getCause());
            me.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        // Check command line arguments
        if (args.length < 2) {
            System.out.println("Usage: topicSubscriber tcp://<host:port> <client-username> [client-password]");
            System.out.println();
            System.exit(-1);
        }
        new MainActivity().run(args);
    }

    /*
    以下為tts
     */

    public class pronounce extends Fragment  {

//        private String msg= new String(MqttMessage.getPayload());
        private TextToSpeech textToSpeech;
        private Button btn;
//        private EditText editText;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View view= inflater.inflate(R.layout.activity_main, container, false);
            btn = view.findViewById(R.id.btn);
            textToSpeech = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        int ttsLang = textToSpeech.setLanguage(Locale.ENGLISH);

                        if (ttsLang == TextToSpeech.LANG_MISSING_DATA || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Log.e("TTS", "The Language is not supported!");
                        } else {
                            Log.i("TTS", "Language Supported.");
                        }
                        Log.i("TTS", "Initialization success.");
                    } else {
                        Toast.makeText(getContext(), "TTS Initialization failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {

                    String msg = String(message.getPayload());
                    String data = msg;
                    Log.i("TTS", "button clicked: " + data);
                    int speechStatus = textToSpeech.speak(data, TextToSpeech.QUEUE_FLUSH, null);

                    if (speechStatus == TextToSpeech.ERROR) {
                        Log.e("TTS", "Error in converting Text to Speech!");
                    }
                }
            });
            return view;
        }


        @Override
        public void onDestroy() {
            super.onDestroy();
            if (textToSpeech != null) {
                textToSpeech.stop();
                textToSpeech.shutdown();
            }
        }
    }


}