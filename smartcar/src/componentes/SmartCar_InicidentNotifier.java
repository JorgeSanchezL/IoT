package componentes;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.json.JSONException;
import org.json.JSONObject;
import utils.MySimpleLogger;

public class SmartCar_InicidentNotifier extends MyMqttClient {
	
	public SmartCar_InicidentNotifier(String clientId, SmartCar smartcar, String brokerURL) {
		super(clientId, smartcar, brokerURL);
	}
	
	
	public void alert(String smartCarID, String notificationType, RoadPlace place) {

		String myTopic =  "es/upv/pros/tatami/smartcities/traffic/PTPaterna/road/" + place.getRoad() + "/alerts";

		MqttTopic topic = myClient.getTopic(myTopic);


		// publish incident 'basic'
		// TIP: habrá que adaptar este mensaje si queremos conectarlo al servicio de tráfico SmartTraffic PTPaterna,
		//      para que siga la estructura allí propuesta (ver documento Seminario 3)
		JSONObject pubMsg = new JSONObject();
		try {
			pubMsg.put("vehicle", smartCarID);
			pubMsg.put("event", notificationType);
			pubMsg.put("road", place.getRoad());
			pubMsg.put("kp", place.getKm());
	   		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
   		int pubQoS = 0;
		MqttMessage message = new MqttMessage(pubMsg.toString().getBytes());
    	message.setQos(pubQoS);
    	message.setRetained(false);

    	// Publish the message
    	MySimpleLogger.trace(this.clientId, "Publishing to topic " + topic + " qos " + pubQoS);
    	MqttDeliveryToken token = null;
    	try {
    		// publish message to broker
			token = topic.publish(message);
			MySimpleLogger.trace(this.clientId, pubMsg.toString());
	    	// Wait until the message has been delivered to the broker
			token.waitForCompletion();
			Thread.sleep(100);
		} catch (Exception e) {
			e.printStackTrace();
		}
	    		    	

	}

	public void infoLocation(String smartCarID, RoadPlace place, Boolean in) {
		String action = in ? "VEHICLE_IN" : "VEHICLE_OUT";

		String myTopic =  "es/upv/pros/tatami/smartcities/traffic/PTPaterna/road/" + place.getRoad() + "/traffic";

		MqttTopic topic = myClient.getTopic(myTopic);
		
		
		JSONObject msgData = new JSONObject();
		try {
			msgData.put("action", action);
			msgData.put("road", place.getRoad().substring(0, 2));
			msgData.put("road-segment", place.getRoad());
			msgData.put("vehicle-id", smartCarID);
			msgData.put("position", place.getKm());
			msgData.put("vehicle-role", "Bus");
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		
		JSONObject msg = new JSONObject();
		try {
			msg.put("type", "TRAFFIC");
			msg.put("id", "zzz");
			msg.put("timestamp", System.currentTimeMillis());
			msg.put("msg", msgData);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}

   		int pubQoS = 0;
		MqttMessage message = new MqttMessage(msg.toString().getBytes());
    	message.setQos(pubQoS);
    	message.setRetained(false);

    	// Publish the message
    	MySimpleLogger.trace(this.clientId, "Publishing to topic " + topic + " qos " + pubQoS);
    	MqttDeliveryToken token = null;
    	try {
    		// publish message to broker
			token = topic.publish(message);
			MySimpleLogger.trace(this.clientId, msg.toString());
	    	// Wait until the message has been delivered to the broker
			token.waitForCompletion();
			Thread.sleep(100);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}