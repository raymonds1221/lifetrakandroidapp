package com.salutron.lifetrakwatchapp.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.content.Context;

import com.salutron.lifetrakwatchapp.annotation.*;
import com.salutron.blesdk.SALUserProfile;

@DataTable(name="UserProfile")
public class UserProfile extends BaseModel {
	@DataColumn(name="weight")
	private int weight;
	@DataColumn(name="height")
	private int height;
	@DataColumn(name="birthDay")
	private int birthDay;
	@DataColumn(name="birthMonth")
	private int birthMonth;
	@DataColumn(name="birthYear")
	private int birthYear;
	@DataColumn(name="sensitivity")
	private int sensitivity;
	@DataColumn(name="gender")
	private int gender;
	@DataColumn(name="unitSystem")
	private int unitSystem;
	@DataColumn(name="watchUserProfile", isPrimary=true)
	private Watch watch;
	@DataColumn(name="firstname")
	private String firstname;
	@DataColumn(name="lastname")
	private String lastname;
	@DataColumn(name="email")
	private String email;



	@DataColumn(name="password")
	private String password;

	@DataColumn(name="profile_image_web")
	private String profileImageWeb;
	@DataColumn(name="profile_image_local")
	private String profileImageLocal;
	@DataColumn(name="accessToken")
	private String accessToken;


	
	public UserProfile() { }
	
	public UserProfile(Parcel source) {
		readFromParcel(source);
	}
	
	UserProfile(Context context) {
		super(context);
	}
	
	public static final UserProfile buildUserProfile(Context context, SALUserProfile salUserProfile) {
		UserProfile userProfile = new UserProfile(context);
		
		userProfile.setWeight(salUserProfile.getWeight());
		userProfile.setHeight(salUserProfile.getHeight());
		userProfile.setBirthDay(salUserProfile.getBirthDay());
		userProfile.setBirthMonth(salUserProfile.getBirthMonth());
		userProfile.setBirthYear(salUserProfile.getBirthYear());
		userProfile.setSensitivity(salUserProfile.getSensitivityLevel());
		userProfile.setGender(salUserProfile.getGender());
		userProfile.setUnitSystem(salUserProfile.getUnitSystem());
		
		return userProfile;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeInt(weight);
		dest.writeInt(height);
		dest.writeInt(birthDay);
		dest.writeInt(birthMonth);
		dest.writeInt(birthYear);
		dest.writeInt(sensitivity);
		dest.writeInt(gender);
		dest.writeInt(unitSystem);
	}

	@Override
	public void readFromParcel(Parcel source) {
		id = source.readLong();
		weight = source.readInt();
		height = source.readInt();
		birthDay = source.readInt();
		birthMonth = source.readInt();
		birthYear = source.readInt();
		sensitivity = source.readInt();
		gender = source.readInt();
		unitSystem = source.readInt();
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Get user's current weight
	 * @return weight in pounds
	 */
	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	/**
	 * Get user's current height
	 * @return height in centimeters
	 */
	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getBirthDay() {
		return birthDay;
	}

	public void setBirthDay(int birthDay) {
		this.birthDay = birthDay;
	}

	public int getBirthMonth() {
		return birthMonth;
	}

	public void setBirthMonth(int birthMonth) {
		this.birthMonth = birthMonth;
	}

	public int getBirthYear() {
		return birthYear;
	}

	public void setBirthYear(int birthYear) {
		this.birthYear = birthYear;
	}

	public int getSensitivity() {
		return sensitivity;
	}

	public void setSensitivity(int sensitivity) {
		this.sensitivity = sensitivity;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public int getUnitSystem() {
		return unitSystem;
	}

	public void setUnitSystem(int unitSystem) {
		this.unitSystem = unitSystem;
	}

	public Watch getWatch() {
		return watch;
	}

	public void setWatch(Watch watch) {
		this.watch = watch;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getProfileImageWeb() {
		return profileImageWeb;
	}

	public void setProfileImageWeb(String profileImageWeb) {
		this.profileImageWeb = profileImageWeb;
	}

	public String getProfileImageLocal() {
		return profileImageLocal;
	}

	public void setProfileImageLocal(String profileImageLocal) {
		this.profileImageLocal = profileImageLocal;
	}
	
	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public static final Parcelable.Creator<BaseModel> CREATOR = new Parcelable.Creator<BaseModel>() {
		@Override
		public BaseModel createFromParcel(Parcel source) {
			return new UserProfile(source);
		}

		@Override
		public BaseModel[] newArray(int size) {
			return new UserProfile[size];
		}
	};
	
	@Override
	public String toString() {
		return accessToken + " ," + firstname + " " + lastname;
	}
}
