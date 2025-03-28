package dispositivo.interfaces;

import java.util.Collection;

public interface IDispositivo {
	public String getId();
	
	public IDispositivo iniciar();
	public IDispositivo detener();
		
	public IDispositivo addFuncion(IFuncion f);
	public IFuncion getFuncion(String funcionId);
	public Collection<IFuncion> getFunciones();

	public Boolean getHabilitado();

	// 5.4 - Métodos para habilitar y deshabilitar el dispositivo
	public void habilitar();
	public void deshabilitar();

	public void copiarF1();		
}
