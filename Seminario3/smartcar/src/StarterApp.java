import componentes.SmartCar.SmartCar;

public class StarterApp {
    public static void main(String[] args) throws Exception {

		if ( args.length < 2 )
		{
			System.out.println("Usage: SmartCarStarterApp <smartCarID> <brokerURL>");
			System.exit(1);
		}

		String smartCarID = args[0];
		String brokerURL = args[1];

        SmartCar sc1 = new SmartCar(smartCarID, brokerURL);
		sc1.getIntoRoad("R1s2a", 0);
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
		}

		// Configura el vehículo para que inicie en un punto específico del PTPaterna
		sc1.getIntoRoad("R1s2a", 300);
		//sc1.notifyIncident(...);
    }
}
