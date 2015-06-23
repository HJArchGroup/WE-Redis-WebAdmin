package com.hujiang.redis.monitor.info;

import redis.clients.jedis.Jedis;

public class SystemInfo extends Info {
	public long		upTime;
	
	public float	load1Min;
	public float	load5Min;
	public float	load15Min;
	
	public long		totalRam;
	public long		freeRam;
	public long		sharedRam;
	public long		bufferRam;
	
	public long		totalSwap;
	public long		freeSwap;
	
	public int		processes;
	
	@Override
	public void clear() {
		this.upTime		= 0;
		
		this.load1Min	= 0.0f;
		this.load5Min	= 0.0f;
		this.load15Min	= 0.0f;
		
		this.totalRam	= 0;
		this.freeRam	= 0;
		this.sharedRam	= 0;
		this.bufferRam	= 0;
		
		this.totalSwap	= 0;
		this.freeSwap	= 0;
		
		this.processes	= 0;
		
		this.index		= 0;
		this.len		= 0;
		this.s			= "";
	}

	@Override
	public boolean process(String s) {
		this.clear();
		
		if (!this.preprocess(s)) {
			return false;
		}
		
		this.skipLineBreak();
		
		this.upTime = Long.parseLong(this.getNextValue());
		this.skipLineBreak();
		
		this.load1Min = Float.parseFloat(this.getNextValue());
		this.skipLineBreak();
		
		this.load5Min = Float.parseFloat(this.getNextValue());
		this.skipLineBreak();
		
		this.load15Min = Float.parseFloat(this.getNextValue());
		this.skipLineBreak();
		
		this.totalRam = Long.parseLong(this.getNextValue());
		this.skipLineBreak();
		
		this.freeRam = Long.parseLong(this.getNextValue());
		this.skipLineBreak();
		
		this.sharedRam = Long.parseLong(this.getNextValue());
		this.skipLineBreak();
		
		this.bufferRam = Long.parseLong(this.getNextValue());
		this.skipLineBreak();
		
		this.totalSwap = Long.parseLong(this.getNextValue());
		this.skipLineBreak();
		
		this.freeSwap = Long.parseLong(this.getNextValue());
		this.skipLineBreak();
		
		this.processes = Integer.parseInt(this.getNextValue());
		this.skipLineBreak();
		
		return true;
	}

	@Override
	public String toJSON() {
		String s = "sysinfo:{";
		s += "\"upTime\":\"" + this.upTime + "\",";
		s += "\"load1Min\":\"" + String.format("%.02f", this.load1Min) + "\",";
		s += "\"load5Min\":\"" + String.format("%.02f", this.load5Min) + "\",";
		s += "\"load15Min\":\"" + String.format("%.02f", this.load15Min) + "\",";
		s += "\"totalRam\":\"" + String.format("%,d", this.totalRam) + "\",";
		s += "\"freeRam\":\"" + String.format("%,d", this.freeRam) + "\",";
		s += "\"sharedRam\":\"" + String.format("%,d", this.sharedRam) + "\",";
		s += "\"bufferRam\":\"" + String.format("%,d", this.bufferRam) + "\",";
		s += "\"totalSwap\":\"" + String.format("%,d", this.totalSwap) + "\",";
		s += "\"freeSwap\":\"" + String.format("%,d", this.freeSwap) + "\",";
		s += "\"processes\":\"" + this.processes + "\"";
		s += "}";
		
		return s;
	}
	
	public static void main(String[] args) {
		Jedis redis = new Jedis("192.168.177.61", 10021);
		String s = redis.info("sysinfo");
		redis.close();
		System.out.println(s);
		
		Info info = new SystemInfo();
		info.process(s);
		
		System.out.println(info.toJSON());
	}
}
