package edu.uco.rnolastname.program6.dbutilities;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Account extends Object implements Parcelable{	
	private int id;
	private String modified_at;
	private String username;
	private String password;	
	private String fName;
	private String lName;
	private String dob;
	private String address;
	private String city;
	private String state;
	private int zip;
	private String gender;
	private String email;		
	
	public Account(){}
	
	public Account(String username, String password, String fname, String lname, String dob,
			String address, String city, String state, int zip, String gender, String email){
		this.username = username;
		this.password = password;
		this.fName = fname;
		this.lName = lname;		
		this.address = address;
		this.city = city;
		this.state = state;
		this.zip = zip;
		this.dob = dob;
		this.gender = gender;
		this.email = email;
	}
	
	public Account(Bundle accInfo){
		this.username = accInfo.getString("username");
		this.password = accInfo.getString("password");
		this.fName = accInfo.getString("fname");
		this.lName = accInfo.getString("lname");		
		this.address = accInfo.getString("address");
		this.city = accInfo.getString("city");
		this.state = accInfo.getString("state");
		this.zip = accInfo.getInt("zip");
		this.dob = accInfo.getString("dob");
		this.gender = accInfo.getString("gender");
		this.email = accInfo.getString("email");
	}
		
	@Override 
	public String toString(){
		return this.username;
	}
	
	public void setId(int id){
		this.id = id;
	}
	
	public int getId(){
		return this.id;
	}
	
	public void setModifiedat(String modifiedAt){
		this.modified_at = modifiedAt;
	}
	
	public String getModifiedat(){
		return this.modified_at;
	}
	
	public void setUsername(String username){
		this.username = username;
	}
	
	public String getUsername(){
		return this.username;
	}
	
	public void setPassword(String password){
		this.password = password;
	}				
	
	public String getPassword(){
		return this.password;
	}
	
	public void setfName(String fName){
		this.fName = fName;
	}
	
	public String getfName(){
		return this.fName;
	}
	
	public void setlName(String lName){
		this.lName = lName;
	}
	
	public String getlName(){
		return this.lName;
	}
	
	public void setDob(String dob){
		this.dob = dob;
	}
	
	public String getDob(){
		return this.dob;
	}
	
	public void setAddress(String address){
		this.address = address;
	}
	
	public String getAddress(){
		return this.address;
	}
	
	public void setCity(String city){
		this.city = city;
	}
	
	public String getCity(){
		return this.city;
	}
	
	public void setState(String state){
		this.state = state;
	}
	
	public String getState(){
		return this.state;
	}
	
	public void setZip(int zip){
		this.zip = zip;
	}
	
	public int getZip(){
		return this.zip;
	}
	
	public void setEmail(String email){
		this.email = email;
	}
	
	public String getEmail(){
		return this.email;
	}
	
	public void setGender(String gender){
		this.gender = gender;
	}
	
	public String getGender(){
		return this.gender;
	}

	public static Creator<Account> CREATOR = new Creator<Account>() {
        public Account createFromParcel(Parcel parcel) {
            return new Account(parcel);
        }

        public Account[] newArray(int size) {
            return new Account[size];
        }
    };
	
	@Override
	public int describeContents() {		
		return 0;
	}

	public Account(Parcel parcel){				
		this.id = parcel.readInt();
		this.modified_at = parcel.readString();
		this.username = parcel.readString();
		this.password = parcel.readString();
		this.fName = parcel.readString();
		this.lName = parcel.readString();		
		this.address = parcel.readString();
		this.city = parcel.readString();
		this.state = parcel.readString();		
		this.zip = parcel.readInt();
		this.dob = parcel.readString();
		this.gender = parcel.readString();
		this.email = parcel.readString();				
	}
	
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {		
		dest.writeInt(id);
		dest.writeString(modified_at);
		dest.writeString(username);
		dest.writeString(password);		
		dest.writeString(fName);
		dest.writeString(lName);		
		dest.writeString(address);
		dest.writeString(city);		
		dest.writeString(state);
		dest.writeInt(zip);
		dest.writeString(dob);
		dest.writeString(gender);
		dest.writeString(email);					
	}
}
