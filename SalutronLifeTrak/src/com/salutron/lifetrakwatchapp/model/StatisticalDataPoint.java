package com.salutron.lifetrakwatchapp.model;

import java.util.List;
import java.util.ArrayList;

import android.os.Parcel;
import android.util.Log;
import android.content.Context;

import com.salutron.lifetrakwatchapp.annotation.DataTable;
import com.salutron.lifetrakwatchapp.annotation.DataColumn;
import com.salutron.blesdk.SALStatisticalDataPoint;

@DataTable(name="StatisticalDataPoint")
public class StatisticalDataPoint extends BaseModel {
	@DataColumn(name="dataPointId")
	private long dataPointId;
	@DataColumn(name="averageHR")
	private int averageHR;
	@DataColumn(name="distance")
	private double distance;
	@DataColumn(name="steps")
	private int steps;
	@DataColumn(name="calorie")
	private double calorie;
	@DataColumn(name="sleepPoint02")
	private int sleepPoint02;
	@DataColumn(name="sleepPoint24")
	private int sleepPoint24;
	@DataColumn(name="sleepPoint46")
	private int sleepPoint46;
	@DataColumn(name="sleepPoint68")
	private int sleepPoint68;
	@DataColumn(name="sleepPoint810")
	private int sleepPoint810;
	@DataColumn(name="dominantAxis")
	private int dominantAxis;
	@DataColumn(name="lux")
	private double lux;
	@DataColumn(name="axisDirection")
	private int axisDirection;
	@DataColumn(name="axisMagnitude")
	private int axisMagnitude;
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
	@DataColumn(name="bleStatus")
	private int bleStatus;
	@DataColumn(name="dataHeaderAndPoint", isPrimary=true)
	private StatisticalDataHeader statisticalDataHeader;
	
	public StatisticalDataPoint() { }
	
	private StatisticalDataPoint(Context context) {
		super(context);
	}
	
	public static final StatisticalDataPoint buildStatisticalDataPoint(Context context, SALStatisticalDataPoint salStatisticalDataPoint) {
		StatisticalDataPoint statisticalDataPoint = new StatisticalDataPoint(context);
		
		statisticalDataPoint.setAverageHR(salStatisticalDataPoint.averageHR);
		statisticalDataPoint.setDistance(salStatisticalDataPoint.distance);
		statisticalDataPoint.setSteps(salStatisticalDataPoint.steps);
		statisticalDataPoint.setCalorie(salStatisticalDataPoint.calorie);
		statisticalDataPoint.setSleepPoint02(salStatisticalDataPoint.sleepPoint_0_2);
		statisticalDataPoint.setSleepPoint24(salStatisticalDataPoint.sleepPoint_2_4);
		statisticalDataPoint.setSleepPoint46(salStatisticalDataPoint.sleepPoint_4_6);
		statisticalDataPoint.setSleepPoint68(salStatisticalDataPoint.sleepPoint_6_8);
		statisticalDataPoint.setSleepPoint810(salStatisticalDataPoint.sleepPoint_8_10);
		statisticalDataPoint.setDominantAxis(salStatisticalDataPoint.dominantAxis);
		statisticalDataPoint.setLux(salStatisticalDataPoint.lux);
		statisticalDataPoint.setAxisDirection(salStatisticalDataPoint.axisDirection);
		statisticalDataPoint.setAxisMagnitude(salStatisticalDataPoint.axisMagnitude);
		statisticalDataPoint.setWristOff02(salStatisticalDataPoint.wristOffPoint_0_2);
		statisticalDataPoint.setWristOff24(salStatisticalDataPoint.wristOffPoint_2_4);
		statisticalDataPoint.setWristOff46(salStatisticalDataPoint.wristOffPoint_4_6);
		statisticalDataPoint.setWristOff68(salStatisticalDataPoint.wristOffPoint_6_8);
		statisticalDataPoint.setWristOff810(salStatisticalDataPoint.wristOffPoint_8_10);
		statisticalDataPoint.setBleStatus(salStatisticalDataPoint.statusBLE);
		
		return statisticalDataPoint;
	}
	
	public static final List<StatisticalDataPoint> buildStatisticalDataPoint(Context context, List<SALStatisticalDataPoint> salStatisticalDataPoints) {
		List<StatisticalDataPoint> statisticalDataPoints = new ArrayList<StatisticalDataPoint>();
		//long dataPointId = 0;
		
		int index = 0;
		
		for(SALStatisticalDataPoint salStatisticalDataPoint : salStatisticalDataPoints) {
			//dataPointId++;
			Log.i(TAG, "R450 -" + String.format("averageHR:%s, steps:%s, distance:%s, calories:%s", String.valueOf(salStatisticalDataPoint.averageHR),
					String.valueOf(salStatisticalDataPoint.steps), String.valueOf(salStatisticalDataPoint.distance), String.valueOf(salStatisticalDataPoint.calorie)));
			StatisticalDataPoint statisticalDataPoint = StatisticalDataPoint.buildStatisticalDataPoint(context, salStatisticalDataPoint);
			//statisticalDataPoint.setDataPointId(dataPointId);
			statisticalDataPoint.setDataPointId(index + 1);
			statisticalDataPoints.add(statisticalDataPoint);
			index++;
		}
		
		return statisticalDataPoints;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
	}

	@Override
	public void readFromParcel(Parcel source) {
	}

	public long getDataPointId() {
		return dataPointId;
	}

	public void setDataPointId(long dataPointId) {
		this.dataPointId = dataPointId;
	}

	public int getAverageHR() {
		return averageHR;
	}

	public void setAverageHR(int averageHR) {
		this.averageHR = averageHR;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public int getSteps() {
		return steps;
	}

	public void setSteps(int steps) {
		this.steps = steps;
	}

	public double getCalorie() {
		return calorie;
	}

	public void setCalorie(double calorie) {
		this.calorie = calorie;
	}

	public int getSleepPoint02() {
		return sleepPoint02;
	}

	public void setSleepPoint02(int sleepPoint02) {
		this.sleepPoint02 = sleepPoint02;
	}

	public int getSleepPoint24() {
		return sleepPoint24;
	}

	public void setSleepPoint24(int sleepPoint24) {
		this.sleepPoint24 = sleepPoint24;
	}

	public int getSleepPoint46() {
		return sleepPoint46;
	}

	public void setSleepPoint46(int sleepPoint46) {
		this.sleepPoint46 = sleepPoint46;
	}

	public int getSleepPoint68() {
		return sleepPoint68;
	}

	public void setSleepPoint68(int sleepPoint68) {
		this.sleepPoint68 = sleepPoint68;
	}

	public int getSleepPoint810() {
		return sleepPoint810;
	}

	public void setSleepPoint810(int sleepPoint810) {
		this.sleepPoint810 = sleepPoint810;
	}

	/**
	 * Convenience method for getting the sum of individual sleep points
	 * @return sum of all sleep points
	 */
	public int getTotalSleepPoints() {
		return sleepPoint02 + sleepPoint24 + sleepPoint46 + sleepPoint68 + sleepPoint810;
	}

	public int getDominantAxis() {
		return dominantAxis;
	}

	public void setDominantAxis(int dominantAxis) {
		this.dominantAxis = dominantAxis;
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

	public double getLux() {
		return lux;
	}

	public void setLux(double lux) {
		this.lux = lux;
	}

	public int getAxisDirection() {
		return axisDirection;
	}

	public void setAxisDirection(int axisDirection) {
		this.axisDirection = axisDirection;
	}

	public int getAxisMagnitude() {
		return axisMagnitude;
	}

	public void setAxisMagnitude(int axisMagnitude) {
		this.axisMagnitude = axisMagnitude;
	}

	public int getBleStatus() {
		return bleStatus;
	}

	public void setBleStatus(int bleStatus) {
		this.bleStatus = bleStatus;
	}

	public StatisticalDataHeader getStatisticalDataHeader() {
		return statisticalDataHeader;
	}

	public void setStatisticalDataHeader(StatisticalDataHeader statisticalDataHeader) {
		this.statisticalDataHeader = statisticalDataHeader;
	}
	
	@Override
	public String toString() {
		return String.format("steps:%s, calories: %s, distance: %s, heart rate: %s", 
					String.valueOf(this.steps), String.valueOf(this.calorie), String.valueOf(this.distance), String.valueOf(this.averageHR));
	}
}
