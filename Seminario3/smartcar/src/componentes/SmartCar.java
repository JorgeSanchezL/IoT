package componentes;

import utils.MySimpleLogger;


public class SmartCar {


	protected String brokerURL = null;

	protected String smartCarID = null;
	protected RoadPlace rp = null;	// simula la ubicación actual del vehículo
	protected SmartCar_RoadInfoSubscriber subscriber = null;
	protected SmartCar_InicidentNotifier notifier = null;
	
	public SmartCar(String id, String brokerURL) {
		
		this.setSmartCarID(id);
		this.brokerURL = brokerURL;

		this.notifier = new SmartCar_InicidentNotifier(id + ".incident-notifier", this, this.brokerURL);
		this.notifier.connect();
		
		this.subscriber = new SmartCar_RoadInfoSubscriber(id + ".road-info-subscriber", this, this.brokerURL);
		this.subscriber.connect();

		this.setCurrentRoadPlace(new RoadPlace("R5s1", 0));
	}
	
	
	public void setSmartCarID(String smartCarID) {
		this.smartCarID = smartCarID;
	}
	
	public String getSmartCarID() {
		return smartCarID;
	}

	public void setCurrentRoadPlace(RoadPlace rp) {
		if ( this.rp != null ) {
			String oldBase = "es/upv/pros/tatami/smartcities/traffic/PTPaterna/road/" + this.rp.getRoad();
			this.subscriber.unsubscribe(oldBase + "/info");
			this.subscriber.unsubscribe(oldBase + "/signals");
			this.subscriber.unsubscribe(oldBase + "/traffic");
			this.subscriber.unsubscribe(oldBase + "/alerts");
			this.infoLocation(this.getSmartCarID(), this.rp, false);
		}
		
		String base = "es/upv/pros/tatami/smartcities/traffic/PTPaterna/road/"+rp.getRoad();
		this.subscriber.subscribe(base + "/info");
		this.subscriber.subscribe(base + "/signals");
		this.subscriber.subscribe(base + "/traffic");
		this.subscriber.subscribe(base + "/alerts");
		this.infoLocation(this.getSmartCarID(), rp, true);

		this.rp = rp;
	}

	public RoadPlace getCurrentPlace() {
		return rp;
	}

	public void changeKm(int km) {
		this.getCurrentPlace().setKm(km);
	}
	
	public void getIntoRoad(String road, int km) {
		this.getCurrentPlace().setRoad(road);
		this.getCurrentPlace().setKm(km);
		setCurrentRoadPlace(this.getCurrentPlace());
	}
	
	public void notifyIncident(String incidentType) {
		if ( this.notifier == null )
			return;
		
		this.notifier.alert(this.getSmartCarID(), incidentType, this.getCurrentPlace());
		
	}

	public void infoLocation(String smartCarID, RoadPlace place, Boolean in) {
		if ( this.notifier == null )
			return;
		
		this.notifier.infoLocation(smartCarID, place, in);
		
	}

}
