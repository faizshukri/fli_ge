import org.junit.Assert;
import org.junit.Test;


public class TestPitch {
	FgAircraft myPlane = new FgAircraft();
	Examples examp = new Examples(myPlane);
	
	public TestPitch() throws FgException {
		  myPlane.Init("\""+Autopilot.fgfsBinary+"\" --fg-root=\"" + Autopilot.fgfsRoot + "/Contents/Resources/data\"  "+
				  //"--fg-scenery=\"" +  fgfsRoot + "/data/scenery"+" "+
				       		(FgConnect.isFg3()?"--airport=":"--airport-id=") + Autopilot.airport + " "+
				           "--aircraft=c172p " +
				           (FgConnect.isFg3()?"--disable-ai-traffic ":"")+
				           //"--enable-random-objects " +
				           "--enable-hud " +
				           "--disable-real-weather-fetch "+
				           //"--enable-anti-alias-hud " +
				           //"--enable-horizon-effect " +
				           //"--enable-enhanced-lighting " +
				           //"--enable-ai-models " +
				           //"--enable-clouds3d " +
				           " --geometry="+Autopilot.resolution + " "+
				           "--timeofday="+Autopilot.timeOfDay + " "  +
				           "--httpd=5500 --props=foo,bar,100,localhost,5501,bar" // medium,direction,hz,hostname,port#,style
				           ,
				           Autopilot.save_path);
	  
	  myPlane.load("turned_to_alca.sav");
	  myPlane.pause(true);
	}

	public static final double doublePrecision = 1e-5;
	
	@Test
	public void testPitch1a() throws FgException
	{
		double curr = myPlane.getPitchDeg();
		examp.controllerReset();
		examp.pitch(curr-100);
		Assert.assertEquals(examp.elevLimit,myPlane.getElevator(), doublePrecision);
	}
	@Test
	public void testPitch1b() throws FgException
	{
		double curr = myPlane.getPitchDeg();
		examp.controllerReset();
		examp.pitch(curr+100);
		Assert.assertEquals(-examp.elevLimit,myPlane.getElevator(), doublePrecision);
	}
	
	@Test
	public void testPitch2() throws FgException
	{
		double curr = myPlane.getPitchDeg();
		examp.controllerReset();
		examp.pitch(curr);
		Assert.assertEquals(0,myPlane.getElevator(), doublePrecision);
	}
	
	@Test
	public void testPitch3a() throws FgException
	{
		double curr = myPlane.getPitchDeg();
		examp.controllerReset();
		examp.pitch(curr-10);
		examp.pitch(curr-10);
		examp.pitch(curr-10);
		examp.pitch(curr-10);
		Assert.assertEquals(examp.elevLimit,myPlane.getElevator(), doublePrecision);
	}
	
}
