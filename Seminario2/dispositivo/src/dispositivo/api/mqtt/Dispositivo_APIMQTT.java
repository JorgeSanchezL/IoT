package dispositivo.api.mqtt;

import java.util.Collection;
import java.util.UUID;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.json.JSONException;
import org.json.JSONObject;

import dispositivo.interfaces.Configuracion;
import dispositivo.interfaces.IDispositivo;
import dispositivo.interfaces.IFuncion;
import dispositivo.utils.MySimpleLogger;

public class Dispositivo_APIMQTT implements MqttCallback {

	protected MqttClient myClient;
	protected MqttConnectOptions connOpt;
	protected String clientId = null;
	
	protected IDispositivo dispositivo;
	protected String mqttBroker = null;
	
	private String loggerId = null;
	
	public static Dispositivo_APIMQTT build(IDispositivo dispositivo, String brokerURL) {
		Dispositivo_APIMQTT api = new Dispositivo_APIMQTT(dispositivo);
		api.setBroker(brokerURL);
		return api;
	}
	
	protected Dispositivo_APIMQTT(IDispositivo dev) {
		this.dispositivo = dev;
		this.loggerId = dev.getId() + "-apiMQTT";
	}
	
	protected void setBroker(String mqttBrokerURL) {
		this.mqttBroker = mqttBrokerURL;
	}
	
	
	@Override
	public void connectionLost(Throwable t) {
		MySimpleLogger.debug(this.loggerId, "Connection lost!");
		// code to reconnect to the broker would go here if desired
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		//System.out.println("Pub complete" + new String(token.getMessage().getPayload()));
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		
		String payload = new String(message.getPayload());
		
		System.out.println("-------------------------------------------------");
		System.out.println("| Topic:" + topic);
		System.out.println("| Message: " + payload);
		System.out.println("-------------------------------------------------");
		
		// DO SOME MAGIC HERE!
		
		//
		// Obtenemos el id de la función
		//   Los topics están organizados de la siguiente manera:
		//         $topic_base/dispositivo/funcion/$ID-FUNCION/comandos
		//         o
		//         $topic_base/dispositivo/copiarf1
		//   Donde el $topic_base es parametrizable al arrancar el dispositivo
		//   y la $ID-FUNCION es el identificador de la dunción
		
		//
		// Definimos una API con mensajes de acciones básicos
		//

		String[] topicNiveles = topic.split("/");

		// 5.10 - En caso de recibir un mensaje en el topic de copiar estados, obtenemos el estado y lo copiamos.
		if (topicNiveles[topicNiveles.length-1].equalsIgnoreCase("copiarf1")) {
			JSONObject json = null;
			try {
				json = new JSONObject(payload);
			} catch (Exception e) {
				MySimpleLogger.warn(this.loggerId, "Error al parsear JSON: " + e.getMessage());
				return;
			}
		
			Collection<IFuncion> funciones = this.dispositivo.getFunciones();
			IFuncion f1 = funciones.iterator().next();
			if ( f1 == null ) {
				MySimpleLogger.warn(this.loggerId, "F1 no encontrada");
				return;
			}

			String estado = json.getString("estado");
			if (estado.equalsIgnoreCase("ON")) {
				f1.encender();
			} else if (estado.equalsIgnoreCase("OFF")) {
				f1.apagar();
			} else if (estado.equalsIgnoreCase("BLINK")) {
				f1.parpadear();
			} else {
				MySimpleLogger.warn(this.loggerId, "Estado '" + estado + "' no reconocido. Sólo admitidos: ON, OFF o BLINK");
			}
			return;
		}

		// 5.7 - Extensión de la API para habilitar/deshabilitar el dispositivo
		// Si no es un mensaje para copiar f1, ejecutamos acción indicada en campo 'accion' del JSON recibido
		String action = "";
		try {
			JSONObject json = null;
			json = new JSONObject(payload);
			action = json.getString("accion");
		} catch (Exception e) {
			MySimpleLogger.warn(this.loggerId, "Error al parsear JSON: " + e.getMessage());
			return;
		}
		
		if ( action.equalsIgnoreCase("habilitar") )
			this.dispositivo.habilitar();
		else if ( action.equalsIgnoreCase("deshabilitar") )
			this.dispositivo.deshabilitar();
		else
			MySimpleLogger.warn(this.loggerId, "Acción '" + payload + "' no reconocida. Sólo admitidas: habilitar o deshabilitar");

		
		
	}

	/**
	 * 
	 * runClient
	 * The main functionality of this simple example.
	 * Create a MQTT client, connect to broker, pub/sub, disconnect.
	 * 
	 */
	public void connect() {
		// setup MQTT Client
		String clientID = this.dispositivo.getId() + UUID.randomUUID().toString() + ".subscriber";
		connOpt = new MqttConnectOptions();
		
		connOpt.setCleanSession(true);
		connOpt.setKeepAliveInterval(30);
//			connOpt.setUserName(M2MIO_USERNAME);
//			connOpt.setPassword(M2MIO_PASSWORD_MD5.toCharArray());
		
		// Connect to Broker
		try {
			MqttDefaultFilePersistence persistence = null;
			try {
				persistence = new MqttDefaultFilePersistence("/tmp");
			} catch (Exception e) {
			}
			if ( persistence != null )
				myClient = new MqttClient(this.mqttBroker, clientID, persistence);
			else
				myClient = new MqttClient(this.mqttBroker, clientID);

			myClient.setCallback(this);
			myClient.connect(connOpt);
		} catch (MqttException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		MySimpleLogger.info(this.loggerId, "Conectado al broker " + this.mqttBroker);

	}
	
	
	public void disconnect() {
		
		// disconnect
		try {
			// wait to ensure subscribed messages are delivered
			Thread.sleep(10000);

			myClient.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}

		
	}

	
	protected void subscribe(String myTopic) {
		
		// subscribe to topic
		try {
			int subQoS = 0;
			myClient.subscribe(myTopic, subQoS);
			MySimpleLogger.info(this.loggerId, "Suscrito al topic " + myTopic);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	

	protected void unsubscribe(String myTopic) {
		
		// unsubscribe to topic
		try {
			myClient.unsubscribe(myTopic);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void iniciar() {

		if ( this.myClient == null || !this.myClient.isConnected() )
			this.connect();
		
		if ( this.dispositivo == null )
			return;
		
		this.subscribe(this.calculateCommandTopic());
		// 5.10 - Suscribimos al topic de copiar estados
		this.subscribe(this.calculateCopiarFuncionTopic());
	}
	
	
	
	public void detener() {
		
		
		// To-Do
		
	}

	// 5.10 - Publica el estado de la función en el topic correspondiente
	public void copiarF1() {
		JSONObject pubMsg = new JSONObject();
		try {
			IFuncion f1 = this.dispositivo.getFunciones().iterator().next();
			pubMsg.put("estado", f1.getStatus().getNombre());
	   		} catch (JSONException e1) {
			e1.printStackTrace();
		}

		
		MqttTopic topic = myClient.getTopic(Configuracion.TOPIC_BASE + "funcion/" + "copiarf1");
		MqttMessage message = new MqttMessage(pubMsg.toString().getBytes());
		message.setQos(0);
		message.setRetained(true);

		// Publish the message
    	MqttDeliveryToken token = null;
    	try {
    		// publish message to broker
			token = topic.publish(message);
	    	// Wait until the message has been delivered to the broker
			token.waitForCompletion();
			Thread.sleep(100);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	protected String calculateCommandTopic() {
		return Configuracion.TOPIC_BASE + "dispositivo/" + dispositivo.getId() + "/comandos";
	}

	protected String calculateCopiarFuncionTopic() {
		return Configuracion.TOPIC_BASE + "funcion/" + "copiarf1";
	}

}
