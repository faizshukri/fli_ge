class Autopilot
{
	//set these variable to your preferences
	public static final String resolution ="1024x768";
	public static final String save_path ="h:/fg_save";
	
	//these paths are the settings for DCS-PCs
	public static final String fgfsRoot = (FgConnect.isFg3())? "C:/Program Files/FlightGear":"C:/Program Files (x86)/FlightGear";
	public static final String fgfsBinary = fgfsRoot+(FgConnect.isFg3()?"/bin/Win64/fgfs.exe":"/bin/Win32/fgfs.exe");
	
	//other settings like aircraft etc
	public static final String airport = "KSFO";
	public static final String timeOfDay = "noon"; //possibilities dawn, noon, ...
	
  public static void main(String args[])
  {
	  FgAircraft myPlane = new FgAircraft();
	  Examples examp = new Examples(myPlane);
	  try {
		  
		  if (args.length >= 3)
			  myPlane.Init(args[1], args[2]);
		  else{
			  myPlane.Init("\""+fgfsBinary+"\" --fg-root=\"" + fgfsRoot + "/data\"  "+
					  //"--fg-scenery=\"" +  fgfsRoot + "/data/scenery"+" "+
					       		(FgConnect.isFg3()?"--airport=":"--airport-id=") + airport + " "+
					           "--aircraft=c172p --control=mouse " +
					           (FgConnect.isFg3()?"--disable-ai-traffic ":"")+
					           //"--enable-random-objects " +
					           "--enable-hud " +
					           "--disable-real-weather-fetch "+
					           //"--enable-anti-alias-hud " +
					           //"--enable-horizon-effect " +
					           //"--enable-enhanced-lighting " +
					           //"--enable-ai-models " +
					           //"--enable-clouds3d " +
					           " --geometry="+resolution + " "+
					           "--timeofday="+timeOfDay + " "  +
					           "--httpd=5500 --props=foo,bar,100,localhost,5501,bar" // medium,direction,hz,hostname,port#,style
					           ,
					  save_path);
		  
		  }

		  examp.takeOff((FgConnect.isFg3()?40.:60.),300.,10);
		  examp.manoeuvre();
	} catch (FgException e) {
		e.printStackTrace();
	}
	finally
	{
		  examp.closeGui();
		  myPlane.close();
	}
	
  }
}//end Class

