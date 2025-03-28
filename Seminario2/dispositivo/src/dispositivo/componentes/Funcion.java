package dispositivo.componentes;

import org.json.JSONException;
import org.json.JSONObject;

import dispositivo.api.mqtt.Funcion_APIMQTT;
import dispositivo.interfaces.FuncionStatus;
import dispositivo.interfaces.IFuncion;
import dispositivo.utils.MySimpleLogger;

public class Funcion implements IFuncion {
	
	protected String id = null;
	protected Dispositivo dispositivo = null;

	protected FuncionStatus initialStatus = null;
	protected FuncionStatus status = null;
	
	protected Funcion_APIMQTT funcionMQTT = null;

	private String loggerId = null;

	
	public static Funcion build(String id, Dispositivo dispositivo, String mqttBrokerURL) {
		return new Funcion(id, FuncionStatus.OFF, dispositivo, mqttBrokerURL);
	}
	
	public static Funcion build(String id, FuncionStatus initialStatus, Dispositivo dispositivo, String mqttBrokerURL) {
		return new Funcion(id, initialStatus, dispositivo, mqttBrokerURL);
	}

	protected Funcion(String id, FuncionStatus initialStatus, Dispositivo dispositivo, String mqttBrokerURL) {
		this.id = id;
		this.initialStatus = initialStatus;
		this.loggerId = "Funcion " + id;
		this.funcionMQTT = Funcion_APIMQTT.build(dispositivo, mqttBrokerURL);
	}
		
	@Override
	public String getId() {
		return this.id;
	}
		
	@Override
	public IFuncion encender() {

		MySimpleLogger.info(this.loggerId, "==> Encender");
		this.setStatus(FuncionStatus.ON);
		this.publishStatus();
		return this;
	}

	@Override
	public IFuncion apagar() {

		MySimpleLogger.info(this.loggerId, "==> Apagar");
		this.setStatus(FuncionStatus.OFF);
		this.publishStatus();
		return this;
	}

	@Override
	public IFuncion parpadear() {

		MySimpleLogger.info(this.loggerId, "==> Parpadear");
		this.setStatus(FuncionStatus.BLINK);
		this.publishStatus();
		return this;
	}
	
	protected IFuncion _putIntoInitialStatus() {
		switch (this.initialStatus) {
		case ON:
			this.encender();
			break;
		case OFF:
			this.apagar();
			break;
		case BLINK:
			this.parpadear();
			break;

		default:
			break;
		}
		
		return this;

	}

	@Override
	public FuncionStatus getStatus() {
		return this.status;
	}
	
	protected IFuncion setStatus(FuncionStatus status) {
		this.status = status;
		return this;
	}
	
	@Override
	public IFuncion iniciar() {
		this.funcionMQTT.iniciar();
		this._putIntoInitialStatus();
		return this;
	}
	
	@Override
	public IFuncion detener() {
		return this;
	}

	// 5.9 - Método para publicar un mensaje con el estado de la función. Se llama cada vez que cambiamos el estado de la función
	private void publishStatus() {
		JSONObject pubMsg = new JSONObject();
		try {
			pubMsg.put("accion", this.getStatus().name());
	   		} catch (JSONException e1) {
			e1.printStackTrace();
		}

		funcionMQTT.publishStatus(this.getId(), pubMsg);
	}
}
