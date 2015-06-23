package com.hujiang.redis.monitor.info;

import redis.clients.jedis.Jedis;

public class CPUInfo extends Info {
	public float sys;
	public float user;
	public float sysChildren;
	public float userChildren;
	
	@Override
	public void clear() {
		this.sys			= 0.0f;
		this.user			= 0.0f;
		this.sysChildren	= 0.0f;
		this.userChildren	= 0.0f;
		
		this.index			= 0;
		this.len			= 0;
		this.s				= "";
	}
	
	@Override
	public String toJSON() {
		String s = "CPU:{";
		s += "\"sys\":\"" + String.format("%.02f", this.sys) + "\",";
		s += "\"user\":\"" + String.format("%.02f", this.user) + "\",";
		s += "\"sysChildren\":\"" + String.format("%.02f", this.sysChildren) + "\",";
		s += "\"userChildren\":\"" + String.format("%.02f", this.userChildren) + "\"";
		s += "}";
		
		return s;
	}

	@Override
	public boolean process(String s) {
		this.clear();
		
		if (!this.preprocess(s)) {
			return false;
		}
		
		this.skipALine();
		
		this.sys = Float.parseFloat(this.getNextValue());
		this.skipLineBreak();
		
		this.user = Float.parseFloat(this.getNextValue());
		this.skipLineBreak();
		
		this.sysChildren = Float.parseFloat(this.getNextValue());
		this.skipLineBreak();
		
		this.userChildren = Float.parseFloat(this.getNextValue());
		
		return true;
	}
	
	public static void main(String[] args) {
		Jedis redis = new Jedis("192.168.177.61", 10021);
		String s = redis.info("cpu");
		redis.close();
		
		Info info = new CPUInfo();
		info.process(s);
		
		System.out.println(s);
		System.out.println(info.toJSON());
	}
}
