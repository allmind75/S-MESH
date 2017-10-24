package com.smesh.timeline;


public class TimelineInfo {
	private String userId;
	private String date;
	private String time;
	private String location;
	private String text;
	private String mac;
	private String used; //"TC ����̵Ǿ����� üũ�� ��  FC ����̾ȵǰ� üũ���� TN ����̵ǰ� üũ�� �ȵ�  FN ����� �ȵǰ� üũ�� �ȵ�"
	private String check;
	
	public String getTo_check(){
		return check;
	}
	
	public String getTo_used(){
		return used;
	}
	
	public void setTo_used(String used){
		if(used.contains("T"))
			this.used = "T";
		else
			this.used = "F";
		
		if(used.contains("C"))
			setTo_check("C");
		else
			setTo_check("N");
	}
	
	public void setTo_check(String check){
		this.check = check;
	}
	
	public String getTo_mac() {
		return mac;
	}

	public void setTo_mac(String mac) {
		this.mac = mac;
	}

	public String getTo_userId() {
		return userId;
	}

	public void setTo_userId(String userId) {
		this.userId = userId;
	}

	public String getTo_date() {
		return date;
	}

	public void setTo_date(String date) {
		this.date = date;
	}

	public String getTo_time() {
		return time;
	}

	public void setTo_time(String time) {
		this.time = time;
	}

	public String getTo_location() {
		return location;
	}

	public void setTo_location(String location) {
		this.location = location;
	}

	public String getTo_text() {
		return text;
	}

	public void setTo_text(String text) {
		this.text = text;
	}
}
