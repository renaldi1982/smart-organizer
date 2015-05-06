package edu.uco.rnolastname.program6.dbutilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Task implements Parcelable{
	private int id;	
	private String modified_at;
	private int account_id;
	private int reminder_id; // 0. Alarm, 1. Email 2.Both
	private String task_name;	
	private String category; // "regularreminder","mapreminder","voiceReminder","pictureReminder"
	private String date;		
	private int priority; // 0. Urgent, 1.Regular, 2.Not Important		
	private double map_distance;
	private String map_latlongname;
	private String map_latlong;	
	private String audio_path;
	private String picture_path;
	private String notes;

	//Convert back from json to arraylist
	private ArrayList<String> markerToJSON = new ArrayList<String>();
	private ArrayList<LatLng> latlongToJSON = new ArrayList<LatLng>();
	private ArrayList<String> markers;
	private ArrayList<LatLng> latlong;
	
	public Task(){
		super();
		account_id = -1;
		reminder_id = -1;
		priority = -1;
		map_distance = -1;		
	}
	
	public Task(Bundle taskInfo){		
		super();
		this.account_id = taskInfo.getInt("account_id");
		this.reminder_id = taskInfo.getInt("reminder_id");
		this.task_name = taskInfo.getString("task_name");
		this.category = taskInfo.getString("category");
		this.date = taskInfo.getString("date");
		this.map_distance = taskInfo.getDouble("map_distance");
		this.map_latlongname = taskInfo.getString("map_latlongname");
		this.map_latlong = taskInfo.getParcelable("map_latlong");		
		this.priority = taskInfo.getInt("priority");		
		this.audio_path = taskInfo.getString("audio_path");		
		this.picture_path = taskInfo.getString("picture_path");
		this.notes = taskInfo.getString("notes");
	}
	
	public Task(int account_id, int reminder_id, String task_name, String category, String date, 
			int priority, double distance, String  latlongname, String latlong, String audio_path, String picture_path,
			String notes){
		super();
		this.account_id = account_id;
		this.reminder_id = reminder_id;
		this.task_name = task_name;
		this.category = category;
		this.date = date;
		this.map_distance = distance;
		this.map_latlongname = latlongname;
		this.map_latlong = latlong;
		this.priority = priority;		
		this.audio_path = audio_path;
		this.picture_path = picture_path;		
		this.notes = notes;
	}
	
	public Task(Parcel parcel){
		this.id = parcel.readInt();
		this.modified_at = parcel.readString();
		this.account_id = parcel.readInt();		
		this.reminder_id = parcel.readInt();
		this.task_name = parcel.readString();
		this.category = parcel.readString();
		this.date = parcel.readString();
		this.priority = parcel.readInt();
		this.map_distance = parcel.readDouble();
		this.map_latlongname = parcel.readString();
		this.map_latlong = parcel.readString();				
		this.audio_path = parcel.readString();
		this.picture_path = parcel.readString();		
		this.notes = parcel.readString();
	}
	
	@Override
	public String toString(){
		return this.task_name;
	}
			
	public void setId(int id){
		this.id = id;
	}
	
	public int getId(){
		return id;
	}
	
	public void setTaskName(String task_name){
		this.task_name = task_name;
	}
	
	public String getTaskName(){
		return task_name;
	}
	
	public void setAccountId(int account_id){
		this.account_id = account_id;
	}
	
	public int getAccountId(){
		return account_id;
	}
	
	public void setReminderId(int reminder_id){
		this.reminder_id = reminder_id;
	}
	
	public int getReminderId(){
		return this.reminder_id;
	}
	
	public void setModifiedAt(String modified_at){
		this.modified_at = modified_at;
	}
	
	public String getModifiedAt(){
		return modified_at;
	}
	
	public void setCategory(String category){
		this.category = category;
	}
	
	public String getCategory(){
		return category;
	}
	
	public void setDate(String date){
		this.date = date;
	}
	
	public String getDate(){
		return date;
	}
	
	public void setPriority(int priority){
		this.priority = priority;
	}
	
	public int getPriority(){
		return priority;
	}		
	
	public void setMapDistance(double distance){
		this.map_distance = distance;
	}
	
	public double getMapDistance(){
		return map_distance;
	}
	
	public void setMapLatlongName(String latlongName){
		this.map_latlongname = latlongName;
		
	}
	
	public String getMapLatlongName(){
		return map_latlongname;
	}
	
	public void setMapLatlong(String latlong){
		this.map_latlong = latlong;
				
	}
	
	public String getMapLatlong(){
		return map_latlong;
	}
	
	public void setAudioPath(String audio_path){
		this.audio_path = audio_path;		
	}
	
	public String getAudioPath(){
		return audio_path;
	}
	
	public void setPicturePath(String picture_path){
		this.picture_path = picture_path;
	}
	
	public String getPicturePath(){
		return picture_path;
	}		
	
	public void setNotes(String notes){
		this.notes = notes;
	}
	
	public String getNotes(){
		return this.notes;
	}
	
	public String convertMarkerNameToJson(List<HashMap<String,LatLng>> mapLatlong){
		String json = "";					
		
		for(int i=0;i<mapLatlong.size();i++){
			for(Map.Entry<String, LatLng> key: mapLatlong.get(i).entrySet()){
				markerToJSON.add(key.getKey());
			}
		}
		
		if(markerToJSON.size() != 0){
			Gson gson = new Gson();
			
			json = gson.toJson(markerToJSON);
			
		}else{
			Log.d("DEBUG","Error - Task - markerString array size is 0");
		}
		
		return json;
	}	
	
	public String convertMarkerLatlongToJson(List<HashMap<String,LatLng>> mapLatlong){
		
		String json = "";
		for(int i=0;i<mapLatlong.size();i++){
			for(Map.Entry<String, LatLng> val: mapLatlong.get(i).entrySet()){
				latlongToJSON.add(val.getValue());
			}
		}					
				
		if(latlongToJSON.size() != 0){
			Gson gson = new Gson();
			
			json = gson.toJson(latlongToJSON);
		}else{
			Log.d("DEBUG","Error - Task - latlongString array size is 0");
		}
			
		return json;
	}
	
	public ArrayList<String> getArrayMarkerTitle(){		
		markers = new Gson().fromJson(this.map_latlongname, new TypeToken<List<String>>(){}.getType());
		return markers;
	}
	
	public ArrayList<LatLng> getArrayLatLng(){
		convertMarkerLatlongToArray(this.map_latlong);
		return latlong;
	}
	
	public void convertMarkerLatlongToArray(String json){
		latlong = new Gson().fromJson(json, new TypeToken<List<LatLng>>(){}.getType());
	}
		
	public static Creator<Task> CREATOR = new Creator<Task>() {
        public Task createFromParcel(Parcel parcel) {
            return new Task(parcel);
        }

        public Task[] newArray(int size) {
            return new Task[size];
        }
    };
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(modified_at);
		dest.writeInt(account_id);
		dest.writeInt(reminder_id);
		dest.writeString(task_name);
		dest.writeString(category);
		dest.writeString(date);
		dest.writeInt(priority);	
		dest.writeDouble(map_distance);
		dest.writeString(map_latlongname);
		dest.writeString(map_latlong);
		dest.writeString(audio_path);
		dest.writeString(picture_path);
		dest.writeString(notes);
	}
}
