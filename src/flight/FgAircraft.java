package flight;
import java.util.Map;
import java.util.TreeMap;


 /**
  * FgAircraft provides functions which allow one to manipulate 
  * the variable of the FlightGear FlightSimulator. The purpose of these functions is to make it possible, 
  * to navigate an aircraft.
  * 
  * @author P. H&ouml;fner
  * @author K. Bogdanov (University of Sheffield)
  * @version v0.9
  */ 
public class FgAircraft {
	
	/** Obtains flight parameters, all at once due to oddities of Fg 3. 
	 * @throws FgException */
	public void extractFlightParameters() throws FgException
	{
	}
	
	/** Communicator threads, used to call Fg at the same time to beat the >50ms delay between its evaluation of tcp/ip inputs. */ 
	private FgConnect flight;
	
	/** The thread to use for next call to FG, round-robin. */
	private int currentCommunicator = 0;

	/** The number of worker threads to use. */
	private int totalCommmunicators = 1;
	
	/** Constructor which creates an instance of a communication manager class. 
	 */
	public FgAircraft()
	{
			totalCommmunicators = 1;
			flight= new FgConnect();
	}
	
	/** Initialises communications with FlightGear by establishing a connection to a standard host of flightgear
	 * (such as http://localhost:5500/). If the simulator does not seem to be running, it is started.
	 * 
	 * @param cmd the path to the FlightGear executable
	 * @param saveDir the directory where FlightGear should store its saves.
	 * @throws FgException if something fails.
	*/
	public void Init(String cmd,String saveDir) throws FgException {
		flight.FgInit(cmd, saveDir);// if totalCommmunicators > 1, we do FgInit it multiple times in sequence: the first one will start Fg if needed. The rest will connect to the running instance since until Flightgear starts, the call to FgInit does not return and this will block the rest of the connections.
		flight.setValue("/environment/clouds/layer/coverage", "clear");
		for (int i=1;i<=4;++i)
			flight.setValue("/environment/clouds/layer["+i+"]/coverage", "clear");
		flight.setValue("/environment/metar/valid", false);

	}
	
	/** Closes the connection to FG */
	public void close()
	{
		flight.FgClose();
	}
	
/* ***************************************************************** */	
	//functions
	
	/**
	 * the controlled aircraft waits for a number of milliseconds.
	 * (does nothing)
	 * 
	 * @param milliseconds the number of milliseconds to wait
	 */
	  public void sleep(int milliseconds) {
		  try{
			  Thread.sleep(milliseconds); 
		  }
		  catch(InterruptedException e){
			  // it was decided to ignore this since the reason we can get 
			  // interrupted here is upon shutdown, hence the main control 
			  // loop should immediately get control and terminate the application.
		  }
	  }
	  
	/* ***************************************************************** */	      
	  
	  /**
	   * turn on/off the brakes
	   * 
	   * @param bool true = breakes on, false = breakes off
	   * @throws FgException if there is a communication problem with FlightGear flight simulator.
	   */
	  public void parkingBrakes(boolean bool) throws FgException {
		  flight.setValue("/controls/gear/brake-parking", bool);
	  }

	  /** starts an engine 
	   * @param bool whether to turn ignition on or not
       * @throws FgException if there is a communication problem with FlightGear flight simulator.
	   */
	  public void engineOn(boolean bool) throws FgException {
		  if (bool)
		  {
			  if (FgConnect.isFg3())
				  flight.setValue("controls/switches/starter", true);
			  else
			  {
				  flight.setValue("/controls/engines/engine/starter", 1);
				  flight.setValue("/controls/engines/engine/magnetos", 2);
			  }
		  }
		  else
		  {
			  flight.setValue("/controls/engines/engine/starter", 0);
			  flight.setValue("/controls/engines/engine/magnetos", 0);
		  }			  
	  }
	  
	//left brake
	  
	  /**
	   * Sets wheel brakes
	   * @param wheelRight whether to set the right-wheel breakes
	   * @throws FgException if there is a communication problem with FlightGear flight simulator.
	   */
	  public void setBrake(boolean wheelRight) throws FgException
	  {
		  if (wheelRight)
		  {
			  flight.setValue("controls/gear/brake-right", 1);
			  flight.setValue("controls/gear/brake-left", 0);
		  }
		  else
		  {
			  flight.setValue("controls/gear/brake-right", 0);
			  flight.setValue("controls/gear/brake-left", 1);
		  }
	  }
	  
	  /**
	   * Releases wheel brakes
	   * @throws FgException if there is a communication problem with FlightGear flight simulator.
	   */
	  public void setBrakes() throws FgException
	  {
		  flight.setValue("controls/gear/brake-right", 1);
		  flight.setValue("controls/gear/brake-left", 1);
	  }

	  /**
	   * Releases wheel brakes
	   * @throws FgException if there is a communication problem with FlightGear flight simulator.
	   */
	  public void releaseBrake() throws FgException
	  {
		  flight.setValue("controls/gear/brake-right", 0);
		  flight.setValue("controls/gear/brake-left", 0);
	  }

	  /**
	 * set the throttle of the controlled aircraft.
	 * (0 &le; t &le; 1)
	 * 
	 * @param t value to be set
	 * @throws FgException if there is a communication problem with FlightGear flight simulator.
	 */
	public void setThrottle(double t)  throws FgException{
		//guarantee that t is between 0 and 1
		if(t>=0 && t<=1){
			flight.setValue("controls/engines/engine/throttle", t);
			flight.setValue("/controls/engines/engine[0]/throttle", t);
		}
		else 
			throw new IllegalArgumentException("Error: Plane.setThrottle: InputValue ("+t+") not between 0 and 1.");
	}
	/**
	 * get the value of throttle of the controlled aircraft.
	 * 
	 * @return value of throttle (between 0 and 1)
	 * @throws FgException if there is a communication problem with FlightGear flight simulator.
	 */	
	public double getThrottle() throws FgException {
			return flight.getDoubleValue("controls/engines/engine/throttle");
	}
	  
    /* ***************************************************************** */	    

	//rudders
	//Elevator corresponds to keys up/down
	/**
	 * set the elevator of the controlled aircraft.
	 * (-1 &le; t &le; 1)
	 * 
	 * @param t value to be set
	 * @throws FgException if there is a communication problem with FlightGear flight simulator.
	 */
	public void setElevator(double t) throws FgException {
		// ensure that t is between -1 and 1
		if(t>=-1 && t<=1)
			flight.setValue("controls/flight/elevator", t);
		else 
			throw new IllegalArgumentException("Error: Plane.setElevator: InputValue ("+t+") not between -1 and 1.");	
	}
	
	/**
	 * Retrieve the value of the elevator of the controlled aircraft.
	 * 
	 * @return value of elevator (between -1 and 1)
	 * @throws FgException if there is a communication problem with FlightGear flight simulator or the returned value cannot be converted into an integer.
	 */		
	public double getElevator() throws FgException {
		return flight.getDoubleValue("controls/flight/elevator");
	}	
	
   //Aileron corresponds to keys left/right
	/**
	 * Set the aileron of the controlled aircraft.
	 * (-1(left) &le; t &le; 1(right))
	 * 
	 * @param t value to be set
	 * @throws FgException if there is a communication problem with FlightGear flight simulator.
	 */
	public void setAileron(double t) throws FgException{
		//guarantee that t is between -1 and 1

		if(t>=-1 && t<=1)
			flight.setValue("controls/flight/aileron", t);
		else 
			throw new IllegalArgumentException("Error: Plane.setAileron: InputValue ("+t+") not between -1 and 1.");	
	}

	/**
	 * Get the value of aileron of the controlled aircraft.
	 * 
	 * @return value of aileron (between -1 and 1)
	 * @throws FgException if there is a communication problem with FlightGear flight simulator or the returned value cannot be converted into an integer.
	 */		
	public double getAileron() throws FgException{
		return flight.getDoubleValue("controls/flight/aileron");
	}	
	
	//Rudder corresponds to keys Return/0
	/**
	 * set the rudder of the controlled aircraft.
	 * (-1(left) &le; t &le; 1(right))
	 * 
	 * @param t value to be set
	 * @throws FgException if there is a communication problem with FlightGear flight simulator.
	 */
	public void setRudder(double t)throws FgException {
		//guarantee that t is between -1 and 1
		if(t>=-1 && t<=1)
			flight.setValue("controls/flight/rudder", t);
		else 
			throw new IllegalArgumentException("Error: Plane.setRudder: InputValue ("+t+") not between -1 and 1.");	
	}

	/**
	 * get the value of rudder of the controlled aircraft.
	 * 
	 * @return value of rudder (between -1 and 1)
	 * @throws FgException if there is a communication problem with FlightGear flight simulator or the returned value cannot be converted into an integer.
	 */		
	public double getRudder() throws FgException{
		return flight.getDoubleValue("controls/flight/rudder");
	}	
	
    /* ***************************************************************** */	    

	//Gear (keys g/G)
	/**
	 * lift/lower the wheels.
	 * 
	 * @param down boolean value: true = lower wheels, false = lift wheels
	 * @throws FgException if there is a communication problem with FlightGear flight simulator.
	 */
	public void setGear(boolean down) throws FgException{
		flight.setValue("controls/gear/gear-down", down);
	}

	/* ***************************************************************** */	    

	//Flaps
	/**
	 * set the flaps of the controlled aircraft.
	 * (0 (no flaps) &le; t &le; 1 (full flaps))
	 * 
	 * @param t value to be set
	 * @throws FgException if there is a communication problem with FlightGear flight simulator.
	 */
	public void setFlaps(double t) throws FgException{
		//check that t is between 0 and 1
		if(0<=t && t<=1)
			flight.setValue("controls/flight/flaps", t);
		else 
			throw new IllegalArgumentException("Error: Plane.setRudder: InputValue ("+t+") not between 0 and 1.");	
	}

	/**
	 * get the value of flaps' position.
	 * 
	 * @return value of rudder (between 0 and 1)
	 * @throws FgException if there is a communication problem with FlightGear flight simulator or the returned value cannot be converted into an integer.
	 */		
	public double getFlaps() throws FgException{
		return flight.getDoubleValue("controls/flight/flaps");
	}	
    /* ***************************************************************** */	    

	//Position
	/**
	 * get the longitude degree of airplane's position
	 * 
	 * @return longitude
	 * @throws FgException if there is a communication problem with FlightGear flight simulator or the returned value cannot be converted into an integer.
	 */	
	public double getLongitudeDeg() throws FgException{
		return flight.getDoubleValue("position/longitude-deg");
	}	
	/**
	 * get the latitude degree of airplane's position
	 * 
	 * @return latitude
	 * @throws FgException if there is a communication problem with FlightGear flight simulator or the returned value cannot be converted into an integer.
	 */	
	public double getLatiduteDeg() throws FgException{
		return flight.getDoubleValue("position/latitude-deg");
	}
	
	/** Warps the plane to the specified place.
	 * 
	 * @param deg_l longtitude
	 * @param deg_lat latitude
	 * @throws FgException if there is a communication problem with FlightGear flight simulator or the returned value cannot be converted into an integer.
	 */
	public void setPosition(double deg_l,double deg_lat) throws FgException{
		flight.setValue("position/longitude-deg",deg_l);
		flight.setValue("position/latitude-deg",deg_lat);
	}	
	
	/**
	 * get the attitude above sea level (in feet)
	 * 
	 * @return attitude
	 * @throws FgException if there is a communication problem with FlightGear flight simulator or the returned value cannot be converted into an integer.
	 */	
	public double getAltitudeFt() throws FgException{
		return flight.getDoubleValue("position/altitude-ft");
	}	
	/**
	 * get the attitude above ground level (in feet)
	 * 
	 * @return attitude above ground level
	 * @throws FgException if there is a communication problem with FlightGear flight simulator or the returned value cannot be converted into an integer.
	 */	
	public double getAltitudeAglFt() throws FgException{
		return flight.getDoubleValue("position/altitude-agl-ft");
	}	
	/**
	 * get the ground elevation (in feet)
	 * 
	 * @return ground elevation
	 * @throws FgException if there is a communication problem with FlightGear flight simulator or the returned value cannot be converted into an integer.
	 */
	public double getGroundElevFT() throws FgException{
		return flight.getDoubleValue("position/ground-elev-ft");
	}	
	/**
	 * get the ground elevation (in meter)
	 * 
	 * @return ground elevation
	 * @throws FgException if there is a communication problem with FlightGear flight simulator or the returned value cannot be converted into an integer.
	 */
	public double getGroundElevM() throws FgException{
		return flight.getDoubleValue("position/ground-elev-m");
	}	
	/**
	 * get the sea level radius
	 * 
	 * @return sea level radius
	 * @throws FgException if there is a communication problem with FlightGear flight simulator or the returned value cannot be converted into an integer.
	 */
	public double getSeaLevelRadiusFt() throws FgException{
		return flight.getDoubleValue("position/sea-level-radius-ft");
	}
	
	//orientation of the plane
	/**
	 * get heading degree of airplane's orientation
	 * 
	 * @return heading degree
	 * @throws FgException if there is a communication problem with FlightGear flight simulator or the returned value cannot be converted into an integer.
	 */
	public double getHeadingDeg() throws FgException{
		return flight.getDoubleValue("orientation/heading-deg");
	}
	/**
	 * get roll degree of airplane's orientation
	 * 
	 * @return roll degree
	 * @throws FgException if there is a communication problem with FlightGear flight simulator or the returned value cannot be converted into an integer.
	 */
	public double getRollDeg() throws FgException{
		return flight.getDoubleValue("orientation/roll-deg");
	}
	/**
	 * get pitch degree of airplane's orientation
	 * 
	 * @return roll degree
	 * @throws FgException if there is a communication problem with FlightGear flight simulator or the returned value cannot be converted into an integer.
	 */
	public double getPitchDeg() throws FgException{
		return flight.getDoubleValue("orientation/pitch-deg");
	}
	
    /* ***************************************************************** */	    

	//Position (according to the instruments)
	/**
	 * get the attitude above sea level (in feet) according to the cockpit's instruments
	 * (it can differ from the real value)
	 * 
	 * @return attitude according to the instruments
	 * @throws FgException if there is a communication problem with FlightGear flight simulator or the returned value cannot be converted into an integer.
	 */	
	public double getInstrumentAltitudeFt() throws FgException{
		return flight.getDoubleValue("instrumentation/altimeter/indicated-altitude-ft");
	}
	/**
	 * get the airspeed according to the cockpit's instruments
	 * 
	 * @return attitude according to the instruments
	 * @throws FgException if there is a communication problem with FlightGear flight simulator or the returned value cannot be converted into an integer.
	 */	
	public double getInstrumentAirspeed() throws FgException{
		return flight.getDoubleValue("instrumentation/airspeed-indicator/indicated-speed-kt");
	}
	/**
	 * get the turn according to the cockpit's instruments
	 * (a standard turn of 360 degrees takes exactly two minutes.)
	 * 
	 * @return turn value according to the instruments
	 * @throws FgException if there is a communication problem with FlightGear flight simulator or the returned value cannot be converted into an integer.
	 */	
	public double getInstrumentTurn() throws FgException{
		return flight.getDoubleValue("instrumentation/turn-indicator/indicated-turn-rate");
	}
	/**
	 * get the value of the magnitic compass (direction of the flight) according to the cockpit's instruments
	 * 
	 * @return compass' value
	 * @throws FgException if there is a communication problem with FlightGear flight simulator or the returned value cannot be converted into an integer.
	 */		
	public double getInstrumentCompass() throws FgException{
		return flight.getDoubleValue("instrumentation/magnetic-compass/indicated-heading-deg");
	}
	/**
	 * get the vertical speed according to the cockpit's instruments
	 * 
	 * @return vertical speed according to the instruments
	 * @throws FgException if there is a communication problem with FlightGear flight simulator or the returned value cannot be converted into an integer.
	 */	
	public double getInstrumentVerticalSpeed() throws FgException{
		return flight.getDoubleValue("instrumentation/vertical-speed-indicator/indicated-speed-fpm");
	}

	/** Returns the rate of pitch change - necessary to implement the differential component for altitude hold.
	 * 
	 * @return the rate of pitch change
	 * @throws FgException if communication with FlightGear breaks down or an invalid number is retrieved.
	 */
	public double getPitchRateDegps() throws FgException {
		return flight.getDoubleValue("orientation/pitch-rate-degps");
	}
	
	/** Loads a given situation into the flightgear
	 * 
	 * @param name the name of the situation to load; null simply resets the simulator
	 * @throws FgException if FlightGear failed to complete this command.
	 */
	public void load(String name) throws FgException {
		flight.load(name);flight.setValue("/sim/time/warp", (12+7-java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY))*3600);
		engineOn(true);
	}

	/** Saves the current situation.
	 * 
	 * @param name the name to give to the saved situation
	 * @throws FgException if FlightGear failed to complete this command.
	 */
	public void save(String name) throws FgException {
		flight.save(name);
	}
	
	/** Pauses/resumes simulation.
	 * @param pause true to pause, false to resume
	 * @throws FgException if the communication with FlightGear breaks down
	 */
	public void pause(boolean pause) throws FgException {
		flight.pause(pause);
	}
	
	/** Sets the view number, 0 is cockpit (a number between 0 and 5).
	 * 
	 * @param viewNum view number
	 * @throws FgException if the communication with FlightGear breaks down
	 */
	public void setView(int viewNum) throws FgException {
		flight.setValue("sim/current-view/view-number",viewNum);
	}
}
