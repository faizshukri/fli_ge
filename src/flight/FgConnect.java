package flight;
import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.List;


/**
  * FgConnect provides a bridge between java and the http-output
  * of the Flight Gear Flight Simulator.
  * Therefore it opens a connection to a given URL (standard  http://localhost:5500/)
  * 
  * @author P. H&ouml;fner
  * @author K. Bogdanov (University of Sheffield)
  * @version v0.9
  */ 
public class FgConnect {

	/** Determines the API for communication with FlightGear. If true, expects to communicate with Flightgear3, otherwise FlightGear 1. */
	private static final boolean fg3=true;
	
	public static boolean isFg3() 
	{
		return fg3;
	}

	/** Any communications with FlightGear is one of three kinds, get property, set property and load/save
	 * a situation.
	 *
	 */ 
	interface FgComm {
		/**
		 * Assigns a value to a FlightGear variable. 
		 * This is only possible if FlightGear allows to modify that particular variable. <br>
	     *
	     * @param dir the variable which should be modified<br>
	     *       (including the directory where it can be found in FlightGear's tree of variables)
	     * @param val the value (of type Object) for the variable
	     * @throws FgException if http connection failed for any reason.
		 */
		
		public void setValue(String dir, Object val) throws FgException;
		
	    /**
	     * Retrieves a string value of a FlightGear variable.
	 	 * 
	     * @param fgVariable the variable which should be read<br>
	     *       (has to include a fully directory name where it can be found in FlightGear's tree of variables)
	     * @return the value as a String (it should be subsequently converted in the right format)
	     * @throws FgException if ether http connection failed or the response was garbled.
	     */
		public String getStringValue(String fgVariable) throws FgException;
		
		/** Loads a given situation into the flightgear
		 * 
		 * @param name the name of the situation to load
		 * @throws FgException if FlightGear failed to complete this command.
		 */
		public void load(String name) throws FgException;
	
		/** Saves the current given situation.
		 * 
		 * @param name the name to give to the saved situation
		 * @throws FgException if FlightGear failed to complete this command.
		 */
		public void save(String name) throws FgException;

		/** Pauses/resumes simulation.
		 * @param pause true to pause, false to resume
		 * @throws FgException if the communication with FlightGear breaks down
		 */
		public void pause(boolean pause) throws FgException;
		
		/** Closes connection to FlightGear */
		public void close();
	}
	
	/** HTTP-based FGFS communications. */
	static class FgHttpComm implements FgComm {
		/** localhost:5500 is the predefined host/port combination for FlightGear http connection, 
		 * it can be overwritten by the Constructor 
		 */
		private String http = "http://localhost:5500/";
		
		/**
		 * Class Constructor specifying the host as
		 * <code> &lt;protocol&gt;://&lt;host&gt;:&lt;port&gt;/ </code>
		 * <br>The standard (empty Constructor) defines the host as 
		 * <code>http://localhost:5500/</code>
		 * 
		 * @param protocol a String which describe the protocol type, e.g. "http"
		 * @param host     the hostname, e.g. "localhost"
		 * @param port     the port number
	     * @param dummy ignored since there is no save/load functionality available via HTTP.
		 */
		public FgHttpComm(String protocol, String host, Integer port){
			http= protocol + "://" + host + ":" + port.toString() + "/";
		}
	
		/**
		 * Assigns a value to a FlightGear variable. 
		 * This is only possible if FlightGear allows to modify that particular variable. <br>
	     *
	     * @param dir the variable which should be modified<br>
	     *       (including the directory where it can be found in FlightGear's tree of variables)
	     * @param val the value (of type Object) for the variable
	     * @throws FgException if http connection failed for any reason.
		 */
		@Override
		public void setValue(String dir, Object val) throws FgException {
			//build http string
			String s = http + dir + "?value=" + val.toString() + "&submit=update";
			//try to connect to http-server
			try{	  
				  URL url = new URL(s);
				  HttpURLConnection conn = (HttpURLConnection)url.openConnection();
				  conn.connect();		
				  conn.getInputStream();
				  if (conn.getResponseCode() != HttpURLConnection.HTTP_OK)
					  throw new FgException("connection error, code "+conn.getResponseCode());
			}
			catch(Exception e){
				FgException setEx = new FgException("could not assign value to "+dir);
				setEx.initCause(e);throw setEx;
			}
		}
		
	    /**
	     * Retrieves a string value of a FlightGear variable.
	 	 * 
	     * @param fgVariable the variable which should be read<br>
	     *       (has to include a fully directory name where it can be found in FlightGear's tree of variables)
	     * @return the value as a String (it should be subsequently converted in the right format)
	     * @throws FgException if ether http connection failed or the response was garbled.
	     */
		@Override
		public String getStringValue(String fgVariable) throws FgException{
			//variables
	        BufferedReader inStream;
			String line,html="",s = http + fgVariable;
	        //connect to server and get data
			try{
				  URL url = new URL(s);
				  HttpURLConnection conn = (HttpURLConnection)url.openConnection();
				  conn.connect();		
				  inStream = new BufferedReader(new InputStreamReader(conn.getInputStream()));			
				  if (conn.getResponseCode() != HttpURLConnection.HTTP_OK)
					  throw new FgException("connection error, code "+conn.getResponseCode());
				  while ( (line = inStream.readLine()) != null) {
					  html += line;
				  }
			}
			catch(Exception e){
				FgException setEx = new FgException("could not assign value to "+fgVariable);
				setEx.initCause(e);throw setEx;
			}
	
			//get the important value of the html string and return it.
			return getValueFromString(html);			
		}
	
		private final String inputField = "<input";
		
		/** Extracts the requested value from HTML response returned by the fgfs's http server.
		 * @param str the response from fgfs server 
		 */
		private String getValueFromString(String str) throws FgException{
			//variables
			String result="";
			
			//search first <input
			int start = str.indexOf(inputField);
			//search for the value
			start = str.indexOf("value=\"", start)+inputField.length()+1;
			//search closing "
	        //(after the 7 chars of "value=\"")
			int end = str.indexOf("\"", start);
			
			if (end > start) {
			  //the value is okay
			   result = str.substring(start, end);
			}
			else // failed to find a closing quote 
				throw new FgException("failed to extract a value from html "+str);
			return result;
		}

		@Override
		public void load(@SuppressWarnings("unused") String name) throws FgException {
			throw new FgException("Unimplemented for HTTP connection");
		}

		@Override
		public void save(@SuppressWarnings("unused") String name) throws FgException {
			throw new FgException("Unimplemented for HTTP connection");
		}

		/** Pauses/resumes simulation.
		 * @param pause true to pause, false to resume
		 * @throws FgException if the communication with FlightGear breaks down
		 */
		@Override
		public void pause(boolean pause) throws FgException {
			if (isFg3())
				setValue("run.cgi?value=pause",pause);
			else
				throw new FgException("Unimplemented for HTTP connection");
		}
		
		
		/** Closes connection to FlightGear */
		@Override
		public void close() { // A no-op since HTTP is a stateless protocol
		}
	}
	
	static class FgPropsConn implements FgComm {
		private Socket fgSocket = null;
		private BufferedReader inStream = null;
		private BufferedWriter oStream = null;
		private final String fgfsHostName;
		
		//private final HashMap
		
		/** The current directory of fgfs and the one to store saved situations. */
		private String fgfsSaveDirectory;
		
		public FgPropsConn(String host, String saveDirectory) {
			fgfsHostName = host;
			fgfsSaveDirectory = saveDirectory;
		}
		
		/** Establishes a connection to FlightGear 
		 */
		private void createConnection() throws UnknownHostException, IOException { 
			if (fgSocket == null) {
				fgSocket = new Socket(fgfsHostName,5501);
				inStream = new BufferedReader (new InputStreamReader(fgSocket.getInputStream()));
				oStream = new BufferedWriter(new OutputStreamWriter(fgSocket.getOutputStream()));
				// now switch Fgfs into a non-interactive mode
				oStream.write("data\r\n");oStream.flush();
			}
		}

		/** Closes connection to FlightGear */
		@Override
		public void close()
		{
			if (fgSocket != null) {
				try {
					fgSocket.close();
				} catch (IOException e) {
					// ignored since we already know things have failed and it seems meaningless to do anything in this situation.
				}
				fgSocket = null;
			}			
		}
		
		@Override
		public String getStringValue(String fgVariable) throws FgException {
			String returnResult = null;
			try {
				createConnection();
				// At this point, the coonection is open so we can now retrieve the value
				oStream.write("get "+fgVariable+"\r\n");oStream.flush();
				returnResult = inStream.readLine();
				if (returnResult == null)
					throw new FgException("could not retrieve a value");
			}
			catch (Exception ex) {
				FgException setEx = new FgException("could not open connection to FlightGear");
				setEx.initCause(ex);throw setEx;					
			}
			finally
			{// important: if any communication fails, we close the connection.
				if (returnResult == null)
					close();
			}
			return returnResult;
		}

		@Override
		public void setValue(String dir, Object val) throws FgException {
			boolean success = false;
			try {
				createConnection();
				// At this point, the coonection is open so we can now set the value
				oStream.write("set "+dir+" "+val+"\r\n");oStream.flush();
				success = true;
			}
			catch (Exception ex) {
				FgException setEx = new FgException("could not open connection to FlightGear");
				setEx.initCause(ex);throw setEx;					
			}
			finally
			{// important: if any communication fails, we close the connection.
				if (!success)
					close();
			}			
		}

		private final String fgfsSaveName = "fgfs.sav";
		
		@Override
		public void load(String name) throws FgException {
			boolean success = false;
			if (name != null && (name.equals(fgfsSaveName) || name.indexOf("/") >= 0 || name.indexOf("\\") >= 0))
					 throw new FgException("reserved name");
			
			BufferedReader fileInStream = null;
			BufferedWriter fileOutStream = null;
			
			try {
				createConnection();
				// At this point, the connection is open so we can issue commands to FlightGear
				
				setValue("orientation/pitch-deg",new Integer(0));
				setValue("orientation/roll-deg",new Integer(0));
				Thread.sleep(500);
				if (name != null) {					
					// now we need to create a copy of the given file under the name in fgfsSaveName
					fileInStream = new BufferedReader(new FileReader(fgfsSaveDirectory+File.separator+name));
					fileOutStream = new BufferedWriter(new FileWriter(fgfsSaveDirectory+File.separator+fgfsSaveName));
					String line = fileInStream.readLine();
					
					while(line != null)	{
						fileOutStream.write(line+"\r\n");
						line = fileInStream.readLine();
					}
					fileInStream.close();fileOutStream.close();
					System.out.print("about to start loading "+name+" ... ");
					Thread.sleep(2000);
					oStream.write("run load\r\n");oStream.flush();
					System.out.println("finished");
				}
				else {
					oStream.write("run reset\r\n");oStream.flush();					
				}
				// reset the pause mode
				setValue("sim/freeze/master",new Integer(0));
				setValue("sim/freeze/clock",new Integer(0));
				setValue("/sim/time/warp",new Integer(0));
				success = true;
			}
			catch (Exception ex) {
				FgException setEx = new FgException("could not load the current position");
				setEx.initCause(ex);throw setEx;					
			}
			finally
			{// important: if any communication fails, we close the connection.
				if (!success)
					close();
				if (fileInStream != null)
					try { fileInStream.close();	} catch (IOException e) { /* ignore this */ }
				if (fileOutStream != null) 
					try { fileOutStream.close();	} catch (IOException e) { /* ignore this */ }
			}			
		}

		@Override
		public void save(String name) throws FgException {
			boolean success = false;
			if (name.equals(fgfsSaveName) || name.indexOf("/") >= 0 || name.indexOf("\\") >= 0)
				 throw new FgException("reserved name");
			try {
				createConnection();
				// At this point, the coonection is open so we can issue commands to FlightGear
				File currentSave = new File(fgfsSaveDirectory+File.separator+fgfsSaveName);
				currentSave.delete();
				oStream.write("run save\r\n");oStream.flush();
				success = currentSave.renameTo(new File(fgfsSaveDirectory+File.separator+name));
				int counter = 0; // counts the number of half-seconds we wait for save to complete 
				while (!success && counter++ < 10) {
					Thread.sleep(500);
					success = currentSave.renameTo(new File(fgfsSaveDirectory+File.separator+name));
				}
				if (!success)
					throw new FgException("saving failed");
			}
			catch (Exception ex) {
				FgException setEx = new FgException("could not save the current position");
				setEx.initCause(ex);throw setEx;					
			}
			finally
			{// important: if any communication fails, we close the connection.
				if (!success)
					close();
			}			
		}

		/** Pauses/resumes simulation.
		 * @param pause true to pause, false to resume
		 * @throws FgException if the communication with FlightGear breaks down
		 */
		@Override
		public void pause(boolean pause) throws FgException {
			setValue("sim/freeze/clock",pause);
		}
	}
	
	/** The directory where we keep saved situations. */
	protected String fgfsSaveDirectory;
	
	/** The host name where fgfs is being run. */
	protected String fgfsHostName = "localhost";
	
	/** An object encapsulating the communication interface to flightgear */
	private FgComm comm;

	/** Initialises the connection to FlightGear simulator and stars the simulator if appropriate.
	 * @param cmd the path to FlightGear executable and associated options.
	 * @param saveDir the directory in which FlightGear should be started in - is places save files there. 
	 */
	public void FgInit(String cmd,String saveDir) throws FgException {
		fgfsSaveDirectory = saveDir;
		try
		{
			FgInitInternal();
		}
		catch(FgException ex) {
			// failed, hence attempt to start flightgear
			startFg(cmd);
		}
	}
	
	/** This method starts FlightGear. 
	 * @param cmd the path to FlightGear executable and associated options.
	 * @param saveDir the directory in which FlightGear should be started in - is places save files there. 
	 */
	private void startFg(String cmd) throws FgException {
		List<String> params = new LinkedList<String>();
		StreamTokenizer elements = new StreamTokenizer(new StringReader(cmd));elements.resetSyntax();
		//elements.slashSlashComments(false);elements.slashStarComments(false);
		elements.wordChars(0x21, 0x92);elements.quoteChar('\"');elements.ordinaryChar(' ');
		try
		{
			int tokenType = elements.nextToken();
			String currentElement = "";
			while (tokenType != StreamTokenizer.TT_EOF) {
				switch(tokenType)
				{
				case ' ':
					if (currentElement.length() > 0)
						params.add(currentElement);
					currentElement = "";
					break;
				case StreamTokenizer.TT_WORD:// and ordinary chunk of text rather than a string constant
				case '\"':// a string constant
					currentElement+=elements.sval;
					break;
				default: // unknown token
					throw new FgException("invalid command to start FlightGear");
				}
				
				tokenType = elements.nextToken();
			}
			if (currentElement.length() > 0)
				params.add(currentElement);

			String [] args = new String[0];
			args = params.toArray(args);
			//System.out.println("launching Flightgear with "+cmd);
			Runtime.getRuntime().exec(args,null,new File(fgfsSaveDirectory));
			
		} catch (IOException e) {
			// could not start the simulator, hence give up.
			FgException setEx = new FgException("could not start FlightGear using save directory "+fgfsSaveDirectory);
			setEx.initCause(e);throw setEx;					
		}
		
		boolean success = false; // whether the connection has been established
		int counter = 0; // this one counts the number of half-seconds we've been waiting for FlightGear to start
		System.out.println("Waiting for FlightGear to start");
		final int number_of_half_seconds_to_wait = 20;
		while(counter < number_of_half_seconds_to_wait && !success) {
			try
			{
				FgInitInternal();success = true;
			}
			catch(FgException ex) {
				++counter;
				try {
					Thread.sleep(5000);
					System.out.print(".");
				} catch (InterruptedException e) {
					// assume we've been asked to terminate
					counter = number_of_half_seconds_to_wait;
				}
			}
		}
		System.out.println("\nFgfs started");
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// assume we've been asked to terminate
		}
		if (!success) // we could not establish a connection to launched fgfs, give up.
		{
			System.out.println("\nFailed");			
			throw new FgException("FlightGear did not start");
		}
		
	}
	
	/** Initialises the connection to FlightGear simulator. */
	public void FgInitInternal() throws FgException {
		FgClose(); // close the existing connection, if any.
		
		// Now attempt to create a props connection
		//comm = new FgHttpComm("http",fgfsHostName,new Integer(5500));
		comm = new FgPropsConn(fgfsHostName,fgfsSaveDirectory);
		try
		{
			comm.getStringValue("controls/flight/flaps");
		}
		catch(FgException ex)
		{// failed, hence attempt to create HTTP connection.
			/*
			 * If both protocols are enabled, it is possible that the first one to attempt fgfs connection will fail
			 * because fgfs has not yet started, but the other one will succeed. Disabling on the protocols makes the 
			 * situation less flexible but more predictable.
			 */
			//comm = new FgHttpComm("http",fgfsHostName,new Integer(5500));
			//comm.getStringValue("controls/flight/flaps");// if this throws, the exception is propagated upward, which is the intended behaviour.
			throw new FgException("right now, the http protocol is disabled");
		}
	}
	
	/** Closes the connection to FlightGear simulator. */
	public void FgClose() {
		if (comm != null) {
			comm.close();comm = null;
		}
	}
	
	public void setValue(String dir, boolean val) throws FgException {
		if (comm == null) throw new FgException("Connection with FlightGear was not initialised");
		comm.setValue(dir,val? "true":"false");
	}
	
	public void setValue(String dir, String val) throws FgException {
		if (comm == null) throw new FgException("Connection with FlightGear was not initialised");
		comm.setValue(dir,val);
	}
	
	public void setValue(String dir, int val) throws FgException {
		if (comm == null) throw new FgException("Connection with FlightGear was not initialised");
		comm.setValue(dir,new Integer(val));
	}
	public void setValue(String dir, double val) throws FgException {
		if (comm == null) throw new FgException("Connection with FlightGear was not initialised");
		comm.setValue(dir,new Double(val));
	}
	
	public void setValue(String dir, Object val) throws FgException {
		if (val instanceof String)
			setValue(dir,val);
		else
			if (val instanceof Boolean)
				setValue(dir,((Boolean)val).booleanValue());
			else
			if (val instanceof Integer)
				setValue(dir,((Integer)val).intValue());
			else
			if (val instanceof Double)
				setValue(dir,((Double)val).doubleValue());
			else
				throw new FgException("unknown type of value to set "+val.getClass()); 
	}
    /**
     * Retrieve a double value of a FlightGear variable.
 	 * 
     * @param fgVariable the variable which should be modified<br>
     *       (including the directory where it can be found in FlightGear's tree of variables)
     * @return the value as a String (it should be converted in the right format)
     * @throws FgException if there is a comms problem or the retrieved value cannot be converted to Double
     */
	public double getDoubleValue(String fgVariable) throws FgException{
		if (comm == null) throw new FgException("Connection with FlightGear was not initialised");
		String stringValue = comm.getStringValue(fgVariable);
		try
		{
			return Double.parseDouble(stringValue);
		}
		catch(NumberFormatException ex)
		{
			FgException setEx = new FgException("could convert the string into a number: "+stringValue+" returned by the server");
			setEx.initCause(ex);throw setEx;
		}
	}
	
	/** Loads a given situation into the flightgear
	 * 
	 * @param name the name of the situation to load
	 * @throws FgException if FlightGear failed to complete this command.
	 */
	public void load(String name) throws FgException {
		if (comm == null) throw new FgException("Connection with FlightGear was not initialised");
		comm.load(name);
	}

	/** Saves the current given situation.
	 * 
	 * @param name the name to give to the saved situation
	 * @throws FgException if FlightGear failed to complete this command.
	 */
	public void save(String name) throws FgException {
		if (comm == null) throw new FgException("Connection with FlightGear was not initialised");
		new File(fgfsSaveDirectory+File.separator+name).delete();
		comm.save(name);
	}

	/** Pauses/resumes simulation.
	 * @param pause true to pause, false to resume
	 * @throws FgException if the communication with FlightGear breaks down
	 */
	public void pause(boolean pause) throws FgException
	{
		if (comm == null) throw new FgException("Connection with FlightGear was not initialised");
		comm.pause(pause);		
	}

}//end of the class FgConnect

