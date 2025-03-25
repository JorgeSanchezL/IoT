package dispositivo.api.rest;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Options;
import org.restlet.resource.Put;

import dispositivo.interfaces.IDispositivo;
import dispositivo.interfaces.IFuncion;
import dispositivo.interfaces.FuncionStatus;
import dispositivo.utils.MySimpleLogger;

// 5.10 - Clase que implementa la funcionalidad de copiar una función a todos los dispositivos. Contiene una ruta que permite  
//       indicar a un dispositivo que una función se debe copiar al resto de dispositivos.
//       Al recibir una petición POST, se publica un mensaje en el broker MQTT con el estado de la función para que el resto lo copien.
public class Funcion_Copiar_Recurso extends Recurso {
	
	
	public static final String ID = "FUNCION-ID";
	public static final String RUTA = Dispositivo_Recurso.RUTA + "/funcion/{" + Funcion_Recurso.ID + "}/copiar";

	public static JSONObject serialize(IFuncion f) {
		JSONObject jsonResult = new JSONObject();
		try {
			jsonResult.put("estado", f.getStatus());
		} catch (JSONException e) {
		}
		return jsonResult;
		
	}
	
	protected IFuncion getFuncion() {
		IDispositivo dispositivo = this.getDispositivo_RESTApplication().getDispositivo();
		String funcionId = getAttribute(Funcion_Recurso.ID);
		return dispositivo.getFuncion(funcionId);
	}

	protected void publishCopyStatus(String funcion, JSONObject json) {
		MqttTopic topic = myClient.getTopic(Configuracion.TOPIC_BASE + "dispositivo/" + dispositivo.getId() + "/funcion/" + funcion + "/copiar");
		MqttMessage message = new MqttMessage(json.toString().getBytes());
		message.setQos(0);
		message.setRetained(true);

		// Publish the message
    	MqttDeliveryToken token = null;
    	try {
    		// publish message to broker
			token = topic.publish(message);
			MySimpleLogger.debug(this.loggerId, message.toString());
	    	// Wait until the message has been delivered to the broker
			token.waitForCompletion();
			Thread.sleep(100);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    @Post
    public Representation post() {
    	// Obtenemos la función indicada como parámetro en la Ruta
		IFuncion f = this.getFuncion();
		if ( f == null ) {
			return this.generateResponseWithErrorCode(Status.CLIENT_ERROR_NOT_FOUND);
		}
		
		// Construimos el mensaje de respuesta
    	JSONObject resultJSON = Funcion_Recurso.serialize(f);
		
		// Si todo va bien, devolvemos el resultado y publicamos el mensaje en el broker
		publishCopyStatus(f.getId(), resultJSON);
    	this.setStatus(Status.SUCCESS_OK);
        return new StringRepresentation(resultJSON.toString(), MediaType.APPLICATION_JSON);
    }
    
    
    
	@Options
	public void describe() {
		Set<Method> meths = new HashSet<Method>();
		meths.add(Method.POST);
		this.getResponse().setAllowedMethods(meths);
	}
}
