package dispositivo.componentes;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import dispositivo.api.iot.infraestructure.Dispositivo_RegistradorMQTT;
import dispositivo.api.mqtt.Dispositivo_APIMQTT;
import dispositivo.api.mqtt.Funcion_APIMQTT;
import dispositivo.api.rest.Dispositivo_APIREST;
import dispositivo.interfaces.IDispositivo;
import dispositivo.interfaces.IFuncion;

public class Dispositivo implements IDispositivo {
	
	protected String deviceId = null;
	protected Boolean habilitado = false;

	protected Map<String, IFuncion> functions = null;
	protected Dispositivo_RegistradorMQTT registrador = null;
	protected Funcion_APIMQTT apiFuncionesMQTT = null;
	protected Dispositivo_APIMQTT apiDispositivosMQTT = null;
	protected Dispositivo_APIREST apiFuncionesREST = null;
	
	
	public static Dispositivo build(String deviceId, String ip, String mqttBrokerURL) {
		Dispositivo dispositivo = new Dispositivo(deviceId);
		dispositivo.registrador = Dispositivo_RegistradorMQTT.build(deviceId, ip, mqttBrokerURL);
		dispositivo.apiDispositivosMQTT = Dispositivo_APIMQTT.build(dispositivo, mqttBrokerURL);
		dispositivo.apiFuncionesMQTT = Funcion_APIMQTT.build(dispositivo, mqttBrokerURL);
		dispositivo.apiFuncionesREST = Dispositivo_APIREST.build(dispositivo);
		return dispositivo;
	}

	public static Dispositivo build(String deviceId, String ip, int port, String mqttBrokerURL) {
		Dispositivo dispositivo = new Dispositivo(deviceId);
		dispositivo.registrador = Dispositivo_RegistradorMQTT.build(deviceId, ip, mqttBrokerURL);
		dispositivo.apiDispositivosMQTT = Dispositivo_APIMQTT.build(dispositivo, mqttBrokerURL);
		dispositivo.apiFuncionesMQTT = Funcion_APIMQTT.build(dispositivo, mqttBrokerURL);
		dispositivo.apiFuncionesREST = Dispositivo_APIREST.build(dispositivo, port);
		return dispositivo;
	}

	protected Dispositivo(String deviceId) {
		this.deviceId = deviceId;
	}
	
	@Override
	public String getId() {
		return this.deviceId;
	}

	protected Map<String, IFuncion> getFunctions() {
		return this.functions;
	}
	
	protected void setFunctions(Map<String, IFuncion> fs) {
		this.functions = fs;
	}
	
	@Override
	public Collection<IFuncion> getFunciones() {
		if ( this.getFunctions() == null )
			return null;
		return this.getFunctions().values();
	}
	
	
	@Override
	public IDispositivo addFuncion(IFuncion f) {
		if ( this.getFunctions() == null )
			this.setFunctions(new HashMap<String, IFuncion>());
		this.getFunctions().put(f.getId(), f);
		return this;
	}
	
	
	@Override
	public IFuncion getFuncion(String funcionId) {
		if ( this.getFunctions() == null )
			return null;
		return this.getFunctions().get(funcionId);
	}
	
		
	@Override
	public IDispositivo iniciar() {
		for(IFuncion f : this.getFunciones()) {
			f.iniciar();
		}

		this.registrador.registrar();
		this.apiFuncionesMQTT.iniciar();
		this.apiDispositivosMQTT.iniciar();
		this.apiFuncionesREST.iniciar();
		return this;
	}

	@Override
	public IDispositivo detener() {
		this.registrador.desregistrar();
		this.apiFuncionesMQTT.detener();
		this.apiDispositivosMQTT.detener();
		this.apiFuncionesREST.detener();
		for(IFuncion f : this.getFunciones()) {
			f.detener();
		}
		return this;
	}
	
	// 5.4 - Método para consultar si el dispositivo está habilitado o no
	@Override
	public Boolean getHabilitado() {
		return this.habilitado;
	}
	
	// 5.4 - Método para habilitar el dispositivo
	@Override
	public void habilitar() {
		this.habilitado = true;
	}

	// 5.4 - Método para deshabilitar el dispositivo
	@Override
	public void deshabilitar() {
		this.habilitado = false;
	}

	// 5.10 - Llama al metodo que publica el estado de la funcion en el topic correspondiente
	public void copiarF1() {
		this.apiDispositivosMQTT.copiarF1();
	}
}
