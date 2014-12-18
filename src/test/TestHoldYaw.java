package test;
import org.junit.Assert;
import org.junit.Test;

import flight.*;


public class TestHoldYaw {
	FgAircraft myPlane = new FgAircraft();
	Examples examp = new Examples(myPlane);
	
	public TestHoldYaw() throws FgException {
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
	public void testHoldYaw1() throws FgException
	{
		examp.controllerReset();
		examp.prevRudder = 1;
		examp.holdYaw(myPlane.getHeadingDeg() - 200, 40);
		Assert.assertEquals(examp.prevRudder, myPlane.getRudder(), doublePrecision);
	}
	
	@Test
	public void testHoldYaw2() throws FgException
	{
//		double curr = myPlane.getPitchDeg();
		examp.controllerReset();
		examp.prevRudder = -1;
		examp.holdYaw(myPlane.getHeadingDeg() + 200, 40);
		Assert.assertEquals(examp.prevRudder, myPlane.getRudder(), doublePrecision);
	}
	

	@Test
	public void testHoldYaw3() throws FgException
	{
		examp.controllerReset();
		examp.holdYaw(myPlane.getHeadingDeg() + 120, 40);
		Assert.assertEquals(examp.prevRudder, myPlane.getRudder(), doublePrecision);
	}
	
	@Test
	public void testHoldYaw4() throws FgException
	{
		examp.controllerReset();
		examp.holdYaw(myPlane.getHeadingDeg() - 120, 40);
		Assert.assertEquals(examp.prevRudder, myPlane.getRudder(), doublePrecision);
	}
	
	@Test
	public void testHoldYaw5() throws FgException
	{
		examp.controllerReset();
		examp.holdYaw(myPlane.getHeadingDeg() - 10, 40);
		Assert.assertEquals(examp.prevRudder, myPlane.getRudder(), doublePrecision);
	}
}
