package com.salutron.lifetrakwatchapp.util;

import com.salutron.lifetrakwatchapp.model.LightDataPoint;

public class LightDataUtility {
	private static final float MAX_LIGHT_VALUE = 110000;
	
	public static final float getAllLux(LightDataPoint lightDataPoint) {
		float redLux = lightDataPoint.getRedValue() * lightDataPoint.getRedCoeff();
		float greenLux = lightDataPoint.getGreenValue() * lightDataPoint.getGreenCoeff();
		float blueLux = lightDataPoint.getBlueValue() * lightDataPoint.getBlueCoeff();
		float allLux = redLux  + greenLux + blueLux;
		
		if  (allLux > MAX_LIGHT_VALUE) {
			allLux = MAX_LIGHT_VALUE;
		}
		
		if (Math.log10(allLux) > MAX_LIGHT_VALUE) {
			allLux = MAX_LIGHT_VALUE;
		}
		
		return allLux;
	}
	
	public static final float getBlueLux(LightDataPoint lightDataPoint) {
		float blueLux = lightDataPoint.getBlueValue() * lightDataPoint.getBlueCoeff();
		
		if (blueLux > MAX_LIGHT_VALUE) {
			blueLux = MAX_LIGHT_VALUE;
		}
		
		return blueLux;
	}
	
	/*public float getAllLux(LightDataPoint dataPoint, float maxLightValue, float maxAllLightLuxValue)
	{
	    float allLux = (float) ((dataPoint.getRedValue() * dataPoint.redCoefficient(dataPoint.getSensorGain())) + 
	    		(dataPoint.getGreenValue() * dataPoint.greenCoefficient(dataPoint.getSensorGain())) + 
	    		(dataPoint.getBlueValue() * dataPoint.blueCoefficient(dataPoint.getSensorGain())));
	    allLux = allLux < 1.0f ? 1.0f : allLux;
	   
	    if (allLux > MAX_ALL_LIGHT) {
	        allLux = MAX_ALL_LIGHT;
	    }

	    if (Math.log10(allLux) > maxLightValue) {
	        return maxLightValue;
	    }
	    
	    return allLux;
	}

	public float getRedLux(LightDataPoint dataPoint, float maxLightValue)
	{
	    float redLux = (float) (dataPoint.getRedValue() * dataPoint.redCoefficient(dataPoint.getSensorGain()));
	    redLux = redLux < 1.0f ? 1.0f : redLux;
	    
	    if (Math.log10(redLux) > maxLightValue) {
	        return maxLightValue;
	    }
	    return redLux;
//	    return log10f(redLux);
	}

	public float getGreenLux(LightDataPoint dataPoint, float maxLightValue)
	{
	    float greenLux = (float) (dataPoint.getGreenValue() * dataPoint.greenCoefficient(dataPoint.getSensorGain()));
	    greenLux = greenLux < 1.0f ? 1.0f : greenLux;
	    
	    if (Math.log10(greenLux) > maxLightValue) {
	        return maxLightValue;
	    }
	    return greenLux;
//	    return log10f(greenLux);
	}

	public float getBlueLux(LightDataPoint dataPoint, float maxLightValue)
	{
	    float blueLux = (float) (dataPoint.getBlueValue() * dataPoint.blueCoefficient(dataPoint.getSensorGain()));
	    blueLux = blueLux < 1.0f ? 1.0f : blueLux;
	    
	    if (Math.log10(blueLux) > maxLightValue) {
	        return maxLightValue;
	    }
	    return blueLux;
//	    return log10f(blueLux);
	}*/
	
}
