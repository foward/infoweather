package cl.techmotion.infoweather;

import javax.microedition.location.Coordinates;
import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationProvider;

import net.rim.blackberry.api.browser.Browser;
import net.rim.blackberry.api.browser.BrowserSession;
import net.rim.device.api.gps.GPSInfo;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.component.Status;
import net.rim.device.api.ui.container.MainScreen;

final class localWeatherScreen extends MainScreen{

	private static String appTitle = "Local Weather";
	private RichTextField rtField;
	public LocationProvider lp;
	public LocationProvider getLp() {
		return lp;
	}

	public void setLp(LocationProvider lp) {
		this.lp = lp;
	}

	public final String gpsError = "Error conectando GPS";


	public localWeatherScreen(){
		// Make sure the default menu is displayed on the scroll wheel
		super(DEFAULT_MENU | DEFAULT_CLOSE);
		// Set the title for the application
		setTitle(new LabelField(appTitle, LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH));
		// Create the rich text field, we'll populate it later
		rtField = new RichTextField("Retrieving GPS information.");
		// Add the rich text field to the screen
		add(rtField);

		//Now lets see if we have GPS capabilities
		Criteria cr = new Criteria();
		cr.setAddressInfoRequired(false);
		cr.setAltitudeRequired(false);
		cr.setPreferredResponseTime(Criteria.NO_REQUIREMENT);
		cr.setSpeedAndCourseRequired(false);
		cr.setCostAllowed(true);
		cr.setHorizontalAccuracy(Criteria.NO_REQUIREMENT);
		cr.setPreferredPowerConsumption(Criteria.NO_REQUIREMENT);
		cr.setVerticalAccuracy(Criteria.NO_REQUIREMENT);

		try {
			// set up a location provider instance
			lp = LocationProvider.getInstance(cr);
			if (lp == null) {
				// No location provider, so we can't do much.
				rtField.setText(gpsError);
			} else {                                                            
				rtField.setText("Please wait while the local weather is launched.");
				// fire off a thread to get the GPS information and launch
				// the weather URL
				Runnable initialGPSLoad = new Runnable() {
					public void run() {
						updateGPSInfo();
					}
				};


				//UiApplication.getUiApplication().invokeLater(initialGPSLoad);
				UiApplication.getUiApplication().invokeLater(initialGPSLoad);     
			}
		}catch (LocationException le) {
			rtField.setText(gpsError);                       
		}

	}

	public void  makeMenu(Menu menu, int  instance) {
		// Add a simple About item to the menu
		menu.add(mnuAbout);
	};

	public  MenuItem mnuAbout = new MenuItem("About", 150, 10) {
		// toss up a little screen with my name and the copyright
		public void run() {
			Status.show(" www.techmotion.cl");
		}
	};

	public  void  updateGPSInfo() {
		Runnable gpsJob = new GPSInfo();
		Thread gpsThread = new Thread(gpsJob);
		gpsThread.start();
	}


	public class GPSInfo implements Runnable {
		public void run() {
			try {
				// Get location, one minute timeout
				// leave up to 180 seconds for autonomous mode
				Location loc = lp.getLocation(60);
				if (loc.isValid()) {
					Coordinates c = loc.getQualifiedCoordinates();
					if (c != null) {
						// use coordinate information to update the screen
						double latitude = c.getLatitude();
						double longitude = c.getLongitude();
						launchWeatherURL(latitude, longitude);
					}
				} else {
					locationError("The program returned an Invalid Location");
				}
			} catch (InterruptedException ie) {
				System.err.println(ie);
				locationError("The program raised an InterruptedException.\nException: " + ie);
			} catch (LocationException le) {
				System.err.println(le);
				locationError("The program raised a LocationException.\nException: " + le);
			} catch (Exception e) {
				System.err.println(e);
				locationError(e.getMessage());
			}
		}

		private void launchWeatherURL(final double latVal, final double longVal) {
			UiApplication.getUiApplication().invokeLater(new Runnable() {                        
				public void run() {                                     
					// http://forecast.weather.gov/MapClick.php?textField1=41.06&textField2=-81.50                                  
					String appURL = ("http://forecast.weather.gov/MapClick.php?textField1="
							+ Double.toString(latVal) + "&textField2=" + Double.toString(longVal));
					// Get the default browser session
					BrowserSession browserSession = Browser.getDefaultSession();
					// Then display the page using the browser session
					browserSession.displayPage(appURL);
					// The following line is a work around to the issue found in
					// version 4.2.0
					browserSession.showBrowser();
					// Once the URL is launched, close this application
					System.exit(0);
				}
			});
		}

		private void locationError(final String errMsg) {
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					rtField.setText(gpsError + "\n\n" + errMsg);
				}
			});
		}
	}

}

