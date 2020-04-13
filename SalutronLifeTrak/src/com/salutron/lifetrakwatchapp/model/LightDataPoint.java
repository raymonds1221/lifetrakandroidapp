package com.salutron.lifetrakwatchapp.model;

import java.util.List;
import java.util.ArrayList;

import android.content.Context;
import android.os.Parcel;

import com.salutron.lifetrakwatchapp.annotation.*;
import com.salutron.blesdk.SALLightDataPoint;

@DataTable(name="LightDataPoint")
public class LightDataPoint extends BaseModel {
	@DataColumn(name="dataPointId")
	private int dataPointId;
	@DataColumn(name="redValue")
	private int redValue;
	@DataColumn(name="greenValue")
	private int greenValue;
	@DataColumn(name="blueValue")
	private int blueValue;
	@DataColumn(name="integrationTime")
	private int integrationTime;
	@DataColumn(name="sensorGain")
	private int sensorGain;
	@DataColumn(name="wristOff02")
	private int wristOff02;
	@DataColumn(name="wristOff24")
	private int wristOff24;
	@DataColumn(name="wristOff46")
	private int wristOff46;
	@DataColumn(name="wristOff68")
	private int wristOff68;
	@DataColumn(name="wristOff810")
	private int wristOff810;
	
	@DataColumn(name="dataHeaderAndPoint", isPrimary=true)
	private StatisticalDataHeader statisticalDataHeader;
	
	public LightDataPoint() { }
	
	public LightDataPoint(Context context) {
		super(context);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		
	}

	@Override
	public void readFromParcel(Parcel source) {
		
	}

	public int getDataPointId() {
		return dataPointId;
	}

	public void setDataPointId(int dataPointId) {
		this.dataPointId = dataPointId;
	}

	public int getRedValue() {
		return redValue;
	}

	public void setRedValue(int redValue) {
		this.redValue = redValue;
	}

	public int getGreenValue() {
		return greenValue;
	}

	public void setGreenValue(int greenValue) {
		this.greenValue = greenValue;
	}

	public int getBlueValue() {
		return blueValue;
	}

	public void setBlueValue(int blueValue) {
		this.blueValue = blueValue;
	}
	
	public int getSensorGain() {
		return sensorGain;
	}

	public void setSensorGain(int sensorGain) {
		this.sensorGain = sensorGain;
	}
	
	public int getIntegrationTime() {
		return integrationTime;
	}

	public void setIntegrationTime(int integrationTime) {
		this.integrationTime = integrationTime;
	}

	public int getWristOff02() {
		return wristOff02;
	}

	public void setWristOff02(int wristOff02) {
		this.wristOff02 = wristOff02;
	}

	public int getWristOff24() {
		return wristOff24;
	}

	public void setWristOff24(int wristOff24) {
		this.wristOff24 = wristOff24;
	}

	public int getWristOff46() {
		return wristOff46;
	}

	public void setWristOff46(int wristOff46) {
		this.wristOff46 = wristOff46;
	}

	public int getWristOff68() {
		return wristOff68;
	}

	public void setWristOff68(int wristOff68) {
		this.wristOff68 = wristOff68;
	}

	public int getWristOff810() {
		return wristOff810;
	}

	public void setWristOff810(int wristOff810) {
		this.wristOff810 = wristOff810;
	}

	public StatisticalDataHeader getStatisticalDataHeader() {
		return statisticalDataHeader;
	}

	public void setStatisticalDataHeader(StatisticalDataHeader statisticalDataHeader) {
		this.statisticalDataHeader = statisticalDataHeader;
	}
	
	public float getRedCoeff()
	{
	    switch (this.sensorGain) {
	        case 0:
	            return 0.16625f;
	        case 1:
	            return 0.665f;
	        case 2:
	            return 2.66f;
	        case 3:
	            return 42.56f;
	        default:
	            break;
	    }
	    return 0.0f;
	}

	public float getGreenCoeff()
	{
	    switch (this.sensorGain) {
	        case 0:
	            return 0.262233f;
	        case 1:
	            return 1.048933f;
	        case 2:
	            return 4.19573f;
	        case 3:
	            return 67.13168f;
	        default:
	            break;
	    }
	    return 0.0f;
	}

	public float getBlueCoeff()
	{
	    switch (this.sensorGain) {
	        case 0:
	            return 0.262225f;
	        case 1:
	            return 1.048898f;
	        case 2:
	            return 4.195593f;
	        case 3:
	            return 67.12949f;
	        default:
	            break;
	    }
	    return 0.0f;
	}

	public float getClearCoeff()
	{
	    switch (this.sensorGain) {
	        case 0:
	            return 0.361903f;
	        case 1:
	            return 1.44761f;
	        case 2:
	            return 5.79044f;
	        case 4:
	            return 92.64693f;
	        default:
	            break;
	    }
	    return 0.0f;
	}
	
	public static final List<LightDataPoint> buildLightDataPoint(Context context, List<SALLightDataPoint> salLightDataPoints) {
		List<LightDataPoint> lightDataPoints = new ArrayList<LightDataPoint>();
		
		int index = 0;
		
		for (SALLightDataPoint salLightDataPoint : salLightDataPoints) {
			LightDataPoint lightDataPoint = buildLightDataPoint(context, salLightDataPoint);
			lightDataPoint.setDataPointId(index + 1);
			lightDataPoints.add(lightDataPoint);
			index++;
		}
		
		return lightDataPoints;
	}
	
	public static final LightDataPoint buildLightDataPoint(Context context, SALLightDataPoint salLightDataPoint) {
		LightDataPoint lightDataPoint = new LightDataPoint(context);
		lightDataPoint.setRedValue(salLightDataPoint.nRedValue);
		lightDataPoint.setGreenValue(salLightDataPoint.nGreenValue);
		lightDataPoint.setBlueValue(salLightDataPoint.nBlueValue);
		lightDataPoint.setIntegrationTime(salLightDataPoint.nIntegrationTime);
		lightDataPoint.setSensorGain(salLightDataPoint.nSensorGain);
		
		return lightDataPoint;
	}
}
