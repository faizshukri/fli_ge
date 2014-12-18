package flight;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

 /**
  * This class provides a number of example for control an 
  * airplane in the Flightgear Flightcontroller. 
  * Most of the given examples are done for the standard aircraft 
  * Cessna 172P.
  * 
  * @author P. H&ouml;fner
  * @author K. Bogdanov (University of Sheffield)
  * @version v0.9
  */ 
public class Examples {
	//Global Variables
		public final double WAY_1_LONG = -122.3947522; //longitude
		public final double WAY_1_LAT =   37.64583148;	//laltitude
		
		public final double WAY_2_LONG = -122.3796872; //longitude
		public final double WAY_2_LAT =   37.62240877;	//laltitude
		
		public final double WAY_3_LONG = -122.4379068; //longitude
		public final double WAY_3_LAT =   37.68217546;	//laltitude
		
		//an instance of an FgAircraft
		private final FgAircraft a;
	
		/** The integral component of the difference between the desired yaw and the current one */
		double yawDifferenceIntegral = 0;
		/** Used by the differential component of rudder position */
		double prevRudder = Double.NEGATIVE_INFINITY;

		/** Used by the integral component of the roll controller. */
		double rollDifferenceIntegral = 0;
		/** Used by the differential component of the roll controller. */
		double prevAilerons = Double.NEGATIVE_INFINITY;

		/** Stores the integral value reflecting our alt error. */
		double altDifferenceIntegral = 0;
		
		/** Stores the integral value reflecting our pitch error. */
		double pitchDifferenceIntegral = 0;
		
		/** The allowed maximal rudder position. */
		final double maxRudder = 0.3;
		
		/** The desired position of the elevator. */
		double prevElevator = Double.NEGATIVE_INFINITY;

		//Constructors

		//Constructor which refers to a specific aircraft
		public Examples(FgAircraft airplane)
		{
			a=airplane;
		}
	
	/** Control states, used in these two examples. */
	enum controlStates { TO_TAXI, TO_TAXI_2, TO_AIR, INIT, CLIMB, DESCEND, TO_WAY_1, TURN_TO_WAY_1, OVER_WAY_1, 
						 TO_WAY_2, TURN_TO_WAY_2, OVER_WAY_2, TO_WAY_3, TURN_TO_WAY_3, OVER_WAY_3, CLOSING }
	// In the takeoff example, the following states are used
	// TO_TAXI taxiing slow, using rudder to keep direction
    // TO_TAXI_2 taxiing fast
	// TO_AIR in the air
		
	/**	Example of a take off routine 
	 *		 
	 * @param takeOffSpeed the specific sppeed which a aircraft needs to take off
	 * 			(for example using Cessna 172 P takeOffSpeed = 60)
	 * @param toAltitude altitude in feet to which the aircrft should climb
	 * 			before leaving the method
	 * @param climbRate pitch degree during the climbing flight 
	 * @throws FgException
	 */
	  public void takeOff(double takeOffSpeed, double toAltitude, double climbRate)  throws FgException
	  {
		//get direction
		  //since flightgear starts at the runway, we can use the current heading to 
		  //set the runwayHeading
		  double runwayHdg= a.getHeadingDeg();
		  //in the beginning the currentHeading (the direction of the airplane) is
		  //the same as the runway direction
		  double aircraftHdg = runwayHdg;
		  
		  // the load command makes it easy to restart the simulation, but you cannot save afterwards.
		 // a.load("on_the_ground.sav");
		  
		  System.out.println("getting ready for take-off");
		  //Brakes on
		  a.parkingBrakes(true);
		  //Start the engine
		  a.engineOn(true);
		  // once the engine is on, we need to wait for instruments to stabilize
		  double difference = 0;
		  do
		  {
			//check the different between the current heading and the given 
			aircraftHdg = a.getHeadingDeg();
			difference = aircraftHdg - runwayHdg;  //get difference
			runwayHdg = aircraftHdg;
			sleep(500);
		  } while(Math.abs(difference) > 1.); // the instuments are stabilized

		  //full Flaps
		  a.setFlaps(0.4);
		  //full throttle
		  a.setThrottle(1);
		  //release brakes
		  a.parkingBrakes(false);
		  //set rudder to a neutral position
		  a.setRudder(0);
		  //boolean for leaving the cotrol loop
		  //the loop is left is the value is false
		  boolean takeOff = true;
		  
		  //set the current state
		  state = controlStates.TO_TAXI;
		  
		  
		  //initial all variables for moving slow on the ground
		  //(e.g. set the maximal turn of rudder)
		    controllerReset();
		    createAndShowGUI();
			frame.setTitle(state.toString());
		  //a.setView(5);// side view
			//the control loop
			while(takeOff) {
				//check position every few hundred milliseconds
				sleep(300);
				//the implemented different states
				switch(state)
				{
				case TO_TAXI: // taxiing slowly using rudder to keep direction 
					//get the current Heading
					aircraftHdg = a.getHeadingDeg();
					//calculate the difference between the runway heading and the current heading
					//and set the rudder w.r.t. to the deviation
					//(20 is the sensitivity how far the rudder should be moved, see the description of holdYaw)
					holdYaw(runwayHdg,20);
					//hold the roll stable
					roll(0,40);
					//if the speed is high enough we change the state to TO_TAXI_2
					if (a.getInstrumentAirspeed() > 0.60*takeOffSpeed)
					{
						state = controlStates.TO_TAXI_2;  //set new state
						a.setFlaps(0.4);   //reduce flaps
						a.setElevator(-0.15); //lift frontWheel
						frame.setTitle(state.toString());
					}
					break;
					
				case TO_TAXI_2: // taxiing fast
					//get the current Heading
					aircraftHdg = a.getHeadingDeg();
					//calculate the difference between the runway heading and the current heading
					//and set the rudder w.r.t. to the deviation
					//(8 is the sensitivity how far the rudder should be moved, see the description of holdYwGND)
					holdYaw(runwayHdg,8);
					//hold the roll stable
					roll(0,40);
					
					//if the aircraft has reached the takeOffSpeed
					//(and is perhaps already a little bit in the air)
					//we have to change the state again
					if (a.getInstrumentAirspeed() > takeOffSpeed)
					{
						state = controlStates.TO_AIR;  //set new state
						a.setFlaps(0);  //retract flaps
						frame.setTitle(state.toString());
					}
					break;

				case TO_AIR: // in the air
					//hold the direction of the runway
					//therefore calculate the difference between the runway heading and the current heading
					//and set the rudder w.r.t. to the deviation
					//(30 is the sensitivity how far the rudder should be moved, see the description of holdYaw)
					holdYaw(runwayHdg, 30);
					//hold the aircraft's roll stable
					roll(0,40);
					
					//check the actual climbing rate and change the elevator if there is a correction necessary
					pitch(climbRate);
					
					//if the aircraft has reached the given altitude, 
					//set the flying parameters and terminate the control loop
					if(a.getAltitudeFt()>toAltitude)
					{
						//set parameters
						a.setThrottle(0.8);
						a.setRudder(0);
						a.setFlaps(0);
						a.setGear(false); //lift wheels
						System.out.println("Now, it's your turn");
						//a.setView(0);// cockpit view
						frame.setTitle("in air");
						takeOff=false;
					}
					break;
				case CLOSING:
					a.pause(true); //pause the flight simulator
					takeOff=false; //leave the control loop
					System.out.println("Application closed on user request");
					break;
				}
			}
		  }
//end take-off

	synchronized public void closeGui()
	{
		buttonListener(Buttons.applicationExit);frame=null;
	}
	  
 /** Calculates a direction an observer has to follow in order to get to the goal,
  * 
  * @param x_pos the X coordinate of the position of the observer
  * @param y_pos the Y coordinate of the position of the observer
  * @param x_goal the X coordinate of the goal position
  * @param y_goal the Y coordinate of the goal position
  * @return the angle to follow to get from observer's position to the goal one.
  */
  public double calc_dir(double x_pos, double y_pos, double x_goal, double y_goal)  
  {
	  double angle = Double.NaN, diffX = Math.abs(x_goal-x_pos), diffY = Math.abs(y_goal-y_pos);
	  if (diffX > diffY)
	    angle = Math.atan(diffY/diffX)*180/Math.PI;
	  else
	    angle = 90-Math.atan(diffX/diffY)*180/Math.PI;

	  if(x_goal>=x_pos & y_goal >= y_pos)
	  	return 90-angle;
	  if(x_goal>=x_pos & y_goal < y_pos)
	  	return 90+angle;
	  if(x_goal<x_pos & y_goal < y_pos)
	  	return 270-angle;
	  if(x_goal<x_pos & y_goal >= y_pos)
	  	return 270+angle;
	  return(Double.NaN);		  
  }
//end calc_dir

  /** The <pre>holdYaw</pre> method maintains a yaw of the plane from the past coordinates of the plane. 
   * Initially (before the series of calls to <pre>holdYaw</pre>, the yaw is not known
   * and has to be calculated. Such an initialisation is performed by this method. It needs to be called after
   * one changes a direction of flight.
   */
  public void controllerReset()
  {
	  yawDifferenceIntegral=0;
	  prevRudder = Double.NEGATIVE_INFINITY;

	  rollDifferenceIntegral = 0;
	  prevAilerons = Double.NEGATIVE_INFINITY;
	  
	  altDifferenceIntegral = 0;
	  
	  prevElevator = Double.NEGATIVE_INFINITY;
	  
	  pitchDifferenceIntegral = 0;
  }
  
  /** Uses rudder to keep yaw stable during take-off. This method is for taxiing on the ground.
   * 
   * @param target target yaw
   * @param factor how sensitive rudder movements should be to yaw error. 20 for takeoff, 40 in the air.
   *         In general a small number yields greater rudder turn and great number yields a sensitive rudder movement.
   * @throws FgException if communication with Flightgear failed.
   */
  public void holdYaw(double target,double factor) throws FgException
  {

	// get current Heading degree
	double aircraftHdg = a.getHeadingDeg();
	// calculate the deviation in degree
	double degreeToTurn = (target-aircraftHdg);

	// normalise the degree to a value between -180 and 180 degrees
	if (degreeToTurn < -180) degreeToTurn +=360;
	else
		if (degreeToTurn > 180) degreeToTurn-=360;

	// Limit the potential for an integrator to accumulate ridicuously high values when
    // we cannot reach its target yaw for a period of time.
	yawDifferenceIntegral+=degreeToTurn;
	if (yawDifferenceIntegral > 100)
		yawDifferenceIntegral = 100;
	else
		if (yawDifferenceIntegral < -100)
			yawDifferenceIntegral = -100;
	double rudderPos = degreeToTurn/factor+yawDifferenceIntegral/(factor*40);

	// How much are we moving a rudder by? (NEGATIVE_INFINITY is used to mean that the previous rudder value is not known)
	double difference = prevRudder != Double.NEGATIVE_INFINITY? rudderPos-prevRudder:0;
	
	final double rudderMaxDelta = 0.05; 
	double newRudder = rudderPos;
	if (difference > rudderMaxDelta) newRudder = prevRudder+rudderMaxDelta;
	else
		if (difference < -rudderMaxDelta) newRudder = prevRudder-rudderMaxDelta;
	
	//if the absolute value of the calculated rudder position is too large, set it to 
	//the greatest/smallest possible value
	if (newRudder > maxRudder) newRudder = maxRudder;
	else
		if (newRudder < -maxRudder) newRudder = -maxRudder;
	//finally set the rudder
	a.setRudder(newRudder);prevRudder = newRudder;
  }
  
  
  
/**	Holds the roll of the aircraft around the target degree of roll
*  with a slight overshooting.
*  
*  @param target the target degree of roll
*  @param factor determines the translation of the difference between the desired roll and the actual one, into the movement of ailerons. 
*  40 is best shortly after takeoff when the air speed is low and 80 for air an speed of around 80 knots.
 * @throws FgException if communication with FlightGear breaks down.
*/  
  public void roll(double target,double factor)  throws FgException
  {
	  //maximal value when setting the aileron
	  final double maxAilerons = 0.3, maxDiffererenceIntegral=30;

	  //get the difference between the target and the current roll degree of the plane
	  double rollDegDifference = target - a.getRollDeg();
	  rollDifferenceIntegral+=rollDegDifference;
	  if (rollDifferenceIntegral > maxDiffererenceIntegral)
		  rollDegDifference = maxDiffererenceIntegral;
	  else
		  if (rollDifferenceIntegral < -maxDiffererenceIntegral)
			  rollDifferenceIntegral = -maxDiffererenceIntegral;
	  
	  //calculate the desired new value for the aileron
	  double ailSetting = (0.3*rollDifferenceIntegral + rollDegDifference)/factor;
	  
	  if (ailSetting > maxAilerons) 
		  ailSetting = maxAilerons;
	  else if (ailSetting < -maxAilerons) 
		  ailSetting = -maxAilerons;
	  
	  //set aileron
	  a.setAileron(ailSetting);
  }
//end roll
  
 public final double elevLimit = 0.1;  

  /** Implements climbing/descending, aiming to do this at specific pitch angle.
 * 
 * @param pitchAngle the rate at which to climb/descend
 * @param elev elevator deflection angle to use
 * @throws FgException if there is a problem in communication with FlightGear
 */
  public void pitch(double pitchAngle)  throws FgException
  {
	  // add to the integral
	  pitchDifferenceIntegral+=a.getPitchDeg()-pitchAngle;
	  final double coeff=100,integralLimit=elevLimit*coeff;
	  if (pitchDifferenceIntegral > integralLimit) pitchDifferenceIntegral=integralLimit;
	  if (pitchDifferenceIntegral < -integralLimit) pitchDifferenceIntegral=-integralLimit;
	  
	  double desiredElev = pitchDifferenceIntegral/coeff;
	  if (desiredElev < -elevLimit) desiredElev =-elevLimit;
	  if (desiredElev > elevLimit) desiredElev =elevLimit;
	  a.setElevator(desiredElev);
  }
//end climb

/** Climbs/descends to a given Altitude and holds it.
* fixed values for altitude ... may be changed
* at the moment only for Cessna 172P
* 
* @param altitude the altitude to fly at
* @throws FgException if the communication with FlightGear breaks down
*/
  public void reachAlt(double altitude ) throws FgException
  {
	//set the tolerance, an altitude between the given one minus tolerance 
	//and the given one plus tolerance is okay
	final double tolerance = 150;
	//get current altitude
	double currentAlt = a.getAltitudeFt();

	if(currentAlt>altitude+tolerance)//to high -> the aircraft has to descend
		pitch(-5);
	else if(currentAlt <altitude-tolerance)//to low -> the aircraft has to climb
		pitch(5);
	else
		holdAlt(altitude); // altitude okay -> the aircraft has to hold its altitude
  }
//end reachAlt
  
  /** Holds pitch at a zero rate. Therefore the aircraft holds more or less its
   * current altitude.
   *
   * @param desiredAlt desired altitude
   * @throws FgException if the communication with FlightGear breaks down
   */
  public void holdAlt(double desiredAlt) throws FgException
  {
  	//get current pitch (in degree) and current altitude (in feet)
  	double pitch = a.getPitchDeg();
  	double currAlt = a.getAltitudeFt();
  	
  	double desiredElev = 0;

	double altError = (currAlt-desiredAlt)/10; // 25 foot error is about as bad as 1 pitch off by 1 degree.
	altDifferenceIntegral+=altError;
	if (Math.abs(altDifferenceIntegral) > 20)
		altDifferenceIntegral = 20*Math.signum(altDifferenceIntegral);

	double pitchError = pitch/3;
	if (Math.abs(pitchError) > 4)
		pitchError = 4*Math.signum(pitchError);
	
  	desiredElev = (altDifferenceIntegral*0.15+pitchError)*0.05;
  	
  	if (prevElevator != Double.NEGATIVE_INFINITY)
  	{// limits the rate of elevator change
  	  	double difference = desiredElev-prevElevator;
  	  	if (Math.abs(difference) > 0.05)
  	  		difference = 0.05*Math.signum(difference);
  	  	
  	  	desiredElev = prevElevator+difference;
  	}

	// clamps the maximal setting of the elevator
  	if (Math.abs(desiredElev) > 0.5)
  		desiredElev = 0.5*Math.signum(desiredElev);
  	
  	//finally set the elevator to the calculated value
  	a.setElevator(desiredElev);prevElevator = desiredElev;
  }
//end holdAlt

  /** Maintains a specific air speed. */
  public void airSpeed(double airspeed) throws FgException
  {
      //get current airspeed
  	  double speed = a.getInstrumentAirspeed();
  	  double currentThrottle = a.getThrottle(); 
  	  double desiredThrottle = currentThrottle; //the initial value for the variable to calculate the value for the throttle
  	  if (speed > airspeed+5) //speed too high
  		  desiredThrottle = currentThrottle*0.95; // reduce throttle  to 95%
  	  if (speed < airspeed-5) //speed too low
  		  desiredThrottle = currentThrottle*1.05;// increase throttle  to 105%

  	  //check if the calculated value is too large (greater than 1)
  	  if (desiredThrottle > 1)
  		  desiredThrottle = 1;
  	  //check if the calculated value is too small 
  	  //(if the throttle value is smaller 0.4 the aircraft is difficult to control) 
  	  if (desiredThrottle < 0.4)
  		  desiredThrottle = 0.4;
  	  //finally set throttle value if needed
  	  if (desiredThrottle != currentThrottle)
  		  a.setThrottle(desiredThrottle);
  }
   /** This function keeps the air speed at around 85 knots. 
    * 
   * @throws FgException if there is a problem communicating with FlightGear.
   */
    public void airSpeed85() throws FgException
    {
    	airSpeed(85);
    }

  enum Buttons { buttonHigher, buttonLower, applicationExit }

  /** The main interface. */
  JFrame frame = null;

  /** Creates the user interfaces, modified from  http://java.sun.com/docs/books/tutorial/uiswing/examples/start/HelloWorldSwingProject/src/start//HelloWorldSwing.java */
  private void createAndShowGUI() {
	  if (frame == null)
	  {
		  final Object syncObject = new Object();
	      javax.swing.SwingUtilities.invokeLater(new Runnable() {
	          public void run() {
	 		     //Create and set up the window.
	 		     frame = new JFrame("FG control");
	 		     frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	 		    frame.addWindowListener(new WindowAdapter(  ) {// from http://www.oreilly.com/catalog/learnjava/chapter/ch14.html
	 		       @Override
	 		       public void windowClosing(@SuppressWarnings("unused") WindowEvent we) 
	 		       { 
		 		   	   buttonListener(Buttons.applicationExit);
				   }
	 		     });
	 		   final Dimension buttonDim = new Dimension(120,20);
	 		    frame.getContentPane().setLayout(new FlowLayout());
	 		    // Add the two buttons
	 		    JButton buttonHigher = new JButton("Higher");buttonHigher.setPreferredSize(buttonDim);
	 		     frame.getContentPane().add(buttonHigher);
	 		     buttonHigher.addActionListener(new ActionListener() {
	 				public void actionPerformed(@SuppressWarnings("unused") ActionEvent e) {
	 					buttonListener(Buttons.buttonHigher);
	 				}});
	
	 		     frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),"ESC_pressed");
	 		    frame.getRootPane().getActionMap().put("ESC_pressed", new AbstractAction() {
				    public void actionPerformed(@SuppressWarnings("unused") ActionEvent e) {
		 		    	   buttonListener(Buttons.applicationExit);
				    }
				});
	 		     JButton buttonLower = new JButton("Lower");buttonLower.setPreferredSize(buttonDim);
	 		     frame.getContentPane().add(buttonLower);
	 		     buttonLower.addActionListener(new ActionListener() {
	  				public void actionPerformed(@SuppressWarnings("unused") ActionEvent e) {
	  					buttonListener(Buttons.buttonLower);
	  				}});
	 		
	 		     Dimension d = Toolkit.getDefaultToolkit().getScreenSize();	// from http://forum.java.sun.com/thread.jspa?threadID=473285&messageID=2190854
	 		     int screenHeight = (int)d.getHeight();
	 		     int screenWidth = (int)d.getWidth();        
	 		     //Display the window.
	 		     frame.pack();
	 		     frame.setLocation(0,screenHeight-frame.getHeight()-30);//screenWidth-frame.getWidth(),0);
	 		     frame.setVisible(true);
	 		     synchronized(syncObject)
	 		     {
	 		    	 syncObject.notify();
	 		     }
	          }});
		  synchronized(syncObject)
		  {
			  try {
				syncObject.wait();
			} catch (InterruptedException e) {
				// guess a user is impatient
			}
		  }
	  }
  }

//A complete manoeuvre	
//(until now not finished)
// Uses a few instance variables and methods, followed by the manoeuvre which is the methond flying the plane.
  
// INIT is the initial state (reset all variables)
// CLIMB means climb up to alt
// DESCEND means descend to alt
// TO_ALCA means turn to fly to Alcatraz
// TURN_TO_ALCA means fly to Alcatraz
// OVER_ALCA means manoeuvre over Alcatraz
// TO_GOLD means sink and fly in direction of Golden Gate Bridge
// LOW means a low-altitude flight
// CLOSING means user requested the application to be closed

/** crusing altitude, used by the manoeuvre() method. */
 double alt = 1200;	   
 controlStates state = controlStates.INIT;
 
 /** This method is asynchronously called when you press a button; 
  * you are expected to change the appropriate instance variables
  * which will be noticed at the next tick.
  *  
  * @param buttonNo the button which was pressed.
  */
 synchronized void buttonListener(Buttons buttonNo)
 {
	 if (state == controlStates.TO_TAXI || state == controlStates.TO_TAXI_2 || state == controlStates.TO_AIR)
	 {// close the application
		 if (frame != null)
		 {
	        frame.setVisible(false);frame.dispose();
		 }
 		 state = controlStates.CLOSING;
		 return;
	 }
	 
	 switch(buttonNo)
	 {
		 case buttonHigher:
			 System.out.println("Higher pressed");
			 alt = 2000;
			 state=controlStates.CLIMB;
			 controllerReset();
			 break;
		 case buttonLower:
			 System.out.println("Lower pressed");
			 alt = 500;
			 state=controlStates.DESCEND;
			 controllerReset();
			 break;
		 case applicationExit:
			 if (frame != null)
			 {
				 frame.setVisible(false);frame.dispose();
			 }
			 state = controlStates.CLOSING;
			 break;
	 }

 }
 
 /** Sleeps for the given time (in ms).
  * 
  * @param duration how long to sleep
  */
synchronized void sleep(int duration)
{
	try {
		wait(duration);
	} catch (InterruptedException e) {
		// We've been interrupted-  terminate by setting state to CLOSING
		state = controlStates.CLOSING;
	}
	
}

/** Example manoeuvre III: 
* Fly to Alcatraz at 800ft
* make a manoeuvre to the direction of Golden Gate Bridge
* go down to 100 ft
* fly under the Golden Gate Bridge
* @throws FgException if the communication with FlightGear breaks down
*/
 synchronized public void manoeuvre() throws FgException
  {
	//variables
	boolean manoeuvre=true; // while true, we keep receiving ticks and responding; false exits the control loop
	  	
	createAndShowGUI();
	controllerReset();
	
	if (state == controlStates.CLOSING)
		return;
	
	state = controlStates.INIT;//a.load("turned_to_ALCA.sav");
	frame.setTitle(state.toString());
	
	
	//initialisation
	double target_dir=0, degreeToTurn=0;
	
	// prevState store the last state before a button pressed (TWO or THREE)
	controlStates prevState = null;
	
	//the control loop
	while(manoeuvre)
	{
		//check position every few hundred milliseconds
		sleep(300);
		switch(state)
		{
		case INIT:  //initial state (reset all variables)
			//"reset" throttle and flaps
			a.setThrottle(1);
			a.setFlaps(0);
			//get current heading and take this value  
			//for the current direction
			target_dir=a.getHeadingDeg();
			//change to the "first real" control mode
			state=controlStates.CLIMB;
			System.out.println("climbing to cruise altitude");
			frame.setTitle(state.toString());
			
			//a.setPosition(-122,37);// this is how you can warp a plane to a specific position
			break;
			
		case CLIMB:  //climb up to alt
			//hold roll degree stable
			roll(0,80);
			//hold speed at 85 kts
			airSpeed85();
			//hold/change yaw to the current direction
			holdYaw(target_dir,40);
			//climb with a rate of 10
			pitch(10);
	
			//if we are near the cruising altitude, change state
			if(a.getAltitudeFt()>alt-75)
			{
				if(prevState == null) state = controlStates.TURN_TO_WAY_1; //set new state
				else state = prevState;
				
				System.out.println("reached the cruising altitude, turning to "+state.toString());
				controllerReset();
				frame.setTitle(state.toString());
			}
			break;
			
		case TURN_TO_WAY_1:  //turn to fly to WAYPOINT_1
			//hold the altitude (modify the elevator)
			holdAlt(alt);
			//hold the speed at 85 (modify the throttle)
			airSpeed85();
			//get the current heading
			double currentHdg = a.getHeadingDeg();
			//calculate the direction to WAYPOINT_1 
			target_dir = calc_dir(a.getLongitudeDeg(),a.getLatiduteDeg(),WAY_1_LONG,WAY_1_LAT);
			// target_dir is the desired direction (0..360)
			
			//calculate the angle between the target direction 
			//and the current direction
			degreeToTurn = (target_dir-currentHdg);
			//normalise the degree to a value between -180 and 180
			if (degreeToTurn < -180) degreeToTurn +=360;
			else
				if (degreeToTurn > 180) degreeToTurn-=360;
			// at this point, degreeToTurn is -180 .. 180 degrees

			// tolerance is the tolerance for the roll degree.
			double tolerance = 4;
            // factor determines how sensitive we are to the difference between plane heading and
            // the one to the target, factor 3 makes for a rather steep turn. 
			double factor=3;
			
			holdYaw(target_dir,40);
			//doing a left turn
			if(degreeToTurn<-tolerance)
			{
                // keep the roll angle proportional to the difference between the current direction and the one to WAYPOINT_1;
                // the maximal roll angle is 25 degrees.
				roll(Math.max(-15,(degreeToTurn-20)/factor),80);
			}
			//doing a right turn
			else if (degreeToTurn>tolerance)
			{
				// keep the roll angle proportional to the difference between the current direction and the one to WAYPOINT_1;
                // the maximal roll angle is 25 degrees.
				roll(Math.min(15,(degreeToTurn+20)/factor),80);
			}
			
			if (Math.abs(degreeToTurn) < 2) {
				state = controlStates.TO_WAY_1;  //set new control state
				System.out.println("turn complete, flying to WAY_1");
				controllerReset(); //reset yaw integrator/differentiator values
				frame.setTitle(state.toString());
			}
			
			break;
		
		case TO_WAY_1:  // fly to WAYPOINT_1
			//hold the altitude still at the given cruising altitude (using the elevator)
			holdAlt(alt);
			//hold the target still at 85 kts (modify throttle)
			airSpeed85();
			//stabilise the roll (using ailerons)
			roll(0,80);
			
			//calculate the direction to WAY_POINT_1 
			target_dir = calc_dir(a.getLongitudeDeg(),a.getLatiduteDeg(),WAY_1_LONG,WAY_1_LAT);
			// dir is the desired direction (0..360), currentHdg is the current heading.
			
			//make small changes in direction to hold the direction to WAY_POINT_1
			holdYaw(target_dir,40);
			
			//If the airplane is "close enough" to WAY_POINT_1
			//change state
			if(Math.abs(a.getLongitudeDeg()-WAY_1_LONG)<0.02 && Math.abs(a.getLatiduteDeg()-WAY_1_LAT)<0.02)
			{
				state=controlStates.OVER_WAY_1;  //set new state
				frame.setTitle(state.toString());
			}
			break;
			
		case OVER_WAY_1:  //manoeuvre over WAYPOINT_1
			System.out.println("WAY_1 reached");

			// Set flag of toWaypoint1AfterBtnPress to false
			if(toWaypoint1AfterBtnPress) toWaypoint1AfterBtnPress = false;

			// Continue to waypoint 2
			state=controlStates.TURN_TO_WAY_2;
			frame.setTitle(state.toString());
			break;
			
		case TURN_TO_WAY_2:  //turn to fly to WAYPOINT_2
			//hold the altitude (modify the elevator)
			holdAlt(alt);
			//hold the speed at 85 (modify the throttle)
			airSpeed85();
			//get the current heading
			currentHdg = a.getHeadingDeg();
			//calculate the direction to WAYPOINT_1 
			target_dir = calc_dir(a.getLongitudeDeg(),a.getLatiduteDeg(),WAY_2_LONG,WAY_2_LAT);
			// target_dir is the desired direction (0..360)

			//calculate the angle between the target direction 
			//and the current direction
			degreeToTurn = (target_dir-currentHdg);
			//normalise the degree to a value between -180 and 180
			if (degreeToTurn < -180) degreeToTurn +=360;
			else
				if (degreeToTurn > 180) degreeToTurn-=360;
			// at this point, degreeToTurn is -180 .. 180 degrees

			// tolerance is the tolerance for the roll degree.
			tolerance = 4;
            // factor determines how sensitive we are to the difference between plane heading and
            // the one to the target, factor 3 makes for a rather steep turn. 
			factor=3;
			
			holdYaw(target_dir,40);
			//doing a left turn
			if(degreeToTurn<-tolerance)
			{
                // keep the roll angle proportional to the difference between the current direction and the one to WAYPOINT_2;
                // the maximal roll angle is 25 degrees.
				roll(Math.max(-15,(degreeToTurn-20)/factor),80);
			}
			//doing a right turn
			else if (degreeToTurn>tolerance)
			{
				// keep the roll angle proportional to the difference between the current direction and the one to WAYPOINT_2;
                // the maximal roll angle is 25 degrees.
				roll(Math.min(15,(degreeToTurn+20)/factor),80);
			}
			
			if (Math.abs(degreeToTurn) < 2)
			{
				state = controlStates.TO_WAY_2;  //set new control state
				System.out.println("turn complete, flying to WAY_2");
				controllerReset(); //reset yaw integrator/differentiator values
				frame.setTitle(state.toString());
			}
			break;
			
		case TO_WAY_2:  // fly to WAYPOINT_2
			//hold the altitude still at the given cruising altitude (using the elevator)
			holdAlt(alt);
			//hold the target still at 85 kts (modify throttle)
			airSpeed85();
			//stabilise the roll (using ailerons)
			roll(0,80);
			
			//calculate the direction to WAY_POINT_2 
			target_dir = calc_dir(a.getLongitudeDeg(),a.getLatiduteDeg(),WAY_2_LONG,WAY_2_LAT);
			// dir is the desired direction (0..360), currentHdg is the current heading.
			
			//make small changes in direction to hold the direction to WAY_POINT_2
			holdYaw(target_dir,40);
			
			//If the airplane is "close enough" to WAY_POINT_2
			//change state
			if(Math.abs(a.getLongitudeDeg()-WAY_2_LONG)<0.02 && Math.abs(a.getLatiduteDeg()-WAY_2_LAT)<0.02)
			{
				state=controlStates.OVER_WAY_2;  //set new state
				frame.setTitle(state.toString());
			}
			break;
			
		case OVER_WAY_2:  //manoeuvre over WAYPOINT_2
			System.out.println("WAY_2 reached");

			// Continue to waypoint 3
			state=controlStates.TURN_TO_WAY_3;
			frame.setTitle(state.toString());
			break;
			
		case TURN_TO_WAY_3:  //turn to fly to WAYPOINT_3
			//hold the altitude (modify the elevator)
			holdAlt(alt);
			//hold the speed at 85 (modify the throttle)
			airSpeed85();
			//get the current heading
			currentHdg = a.getHeadingDeg();
			//calculate the direction to WAYPOINT_3
			target_dir = calc_dir(a.getLongitudeDeg(),a.getLatiduteDeg(),WAY_3_LONG,WAY_3_LAT);
			// target_dir is the desired direction (0..360)

			//calculate the angle between the target direction 
			//and the current direction
			degreeToTurn = (target_dir-currentHdg);
			if (degreeToTurn < -180) degreeToTurn +=360;
			else
				if (degreeToTurn > 180) degreeToTurn-=360;
			// at this point, degreeToTurn is -180 .. 180 degrees

			// tolerance is the tolerance for the roll degree.
			tolerance = 4;
            // factor determines how sensitive we are to the difference between plane heading and
            // the one to the target, factor 3 makes for a rather steep turn. 
			factor=3;
			
			holdYaw(target_dir,40);
			//doing a left turn
			if(degreeToTurn<-tolerance)
			{
                // keep the roll angle proportional to the difference between the current direction and the one to WAYPOINT_3;
                // the maximal roll angle is 25 degrees.
				roll(Math.max(-15,(degreeToTurn-20)/factor),80);
			}
			//doing a right turn
			else if (degreeToTurn>tolerance)
			{
				// keep the roll angle proportional to the difference between the current direction and the one to WAYPOINT_3;
                // the maximal roll angle is 25 degrees.
				roll(Math.min(15,(degreeToTurn+20)/factor),80);
			}
			
			if (Math.abs(degreeToTurn) < 2)
			{
				state = controlStates.TO_WAY_3;  //set new control state
				System.out.println("turn complete, flying to WAY_3");
				controllerReset(); //reset yaw integrator/differentiator values
				frame.setTitle(state.toString());
			}
			break;
			
		case TO_WAY_3:  // fly to WAYPOINT_3
			//hold the altitude still at the given cruising altitude (using the elevator)
			holdAlt(alt);
			//hold the target still at 85 kts (modify throttle)
			airSpeed85();
			//stabilise the roll (using ailerons)
			roll(0,80);
			
			//calculate the direction to WAY_POINT_3 
			target_dir = calc_dir(a.getLongitudeDeg(),a.getLatiduteDeg(),WAY_3_LONG,WAY_3_LAT);
			// dir is the desired direction (0..360), currentHdg is the current heading.
			
			//make small changes in direction to hold the direction to WAY_POINT_3
			holdYaw(target_dir,40);
			
			//If the airplane is "close enough" to WAY_POINT_3
			//change state
			if(Math.abs(a.getLongitudeDeg()-WAY_3_LONG)<0.02 && Math.abs(a.getLatiduteDeg()-WAY_3_LAT)<0.02)
			{
				state=controlStates.OVER_WAY_3;  //set new state
				frame.setTitle(state.toString());
			}
			break;
			
		case OVER_WAY_3:  //manoeuvre over WAYPOINT_3
			System.out.println("WAY_3 reached");
			frame.setTitle("at WAY_3");
			System.out.println("END");
			state=controlStates.CLOSING;
			break;
		case DESCEND:  // descend to alt
			controllerReset();
			//hold roll degree stable
			roll(0,80);
			//hold speed at 85 kts
			airSpeed85();
			//hold/change yaw to the current direction
			holdYaw(target_dir,40);
			//climb with a rate of 10
			pitch(-10);
	
			//if we are near the cruising altitude, change state
			if(a.getAltitudeFt()<alt+75)
			{
				if(prevState == null) state = controlStates.TURN_TO_WAY_1; //set new state
				else state = prevState;
				
				System.out.println("reached the cruising altitude, turning to "+state.toString());
				controllerReset();
				frame.setTitle(state.toString());
			}
			break;
		case CLOSING:
			a.pause(true); //pause the flight simulator
			manoeuvre=false; //leave the control loop
			System.out.println("Application closed on user request");
			break;
		}
	}	
  }
  //end manoeuvre
}
//end class
