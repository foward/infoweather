package cl.techmotion.infoweather;

import net.rim.device.api.ui.UiApplication;

public class LocalWeather extends UiApplication{

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LocalWeather theApp = new LocalWeather();
		theApp.enterEventDispatcher();
	}
	
	public LocalWeather (){
		pushScreen(new localWeatherScreen());
	}

}
