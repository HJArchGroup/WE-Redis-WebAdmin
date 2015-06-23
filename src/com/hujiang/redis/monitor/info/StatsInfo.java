package com.hujiang.redis.monitor.info;

import redis.clients.jedis.Jedis;

public class StatsInfo extends Info {
	
	public long	receivedConnections;
	public long	processedCommands;
	
	public long netInput;
	public long netOutput;
	
	public int instantaneousOps;
	public float instantaneousInput;
	public float instantaneousOutput;

	@Override
	public void clear() {
		this.receivedConnections	= 0;
		this.processedCommands		= 0;
		this.netInput				= 0;
		this.netOutput				= 0;
		this.instantaneousOps		= 0;
		this.instantaneousInput		= 0.0f;
		this.instantaneousOutput	= 0.0f;
	}

	@Override
	public String toJSON() {
		String s = "stats:{";
		s += "\"conn\":\"" + String.format("%,d", this.receivedConnections) + "\",";
		s += "\"cmd\":\"" + String.format("%,d", this.processedCommands) + "\",";
		s += "\"in\":\"" + String.format("%,d", this.netInput) + "\",";
		s += "\"out\":\"" + String.format("%,d", this.netOutput) + "\",";
		s += "\"insOps\":\"" + this.instantaneousOps + "\",";
		s += "\"insIn\":\"" + String.format("%.02f", this.instantaneousInput) + "\",";
		s += "\"insOut\":\"" + String.format("%.02f", this.instantaneousOutput) + "\"";
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
		
		this.receivedConnections = Long.parseLong(this.getNextValue());
		this.skipLineBreak();
		
		this.processedCommands = Long.parseLong(this.getNextValue());
		this.skipLineBreak();
		
		this.instantaneousOps = Integer.parseInt(this.getNextValue());
		this.skipLineBreak();
		
		this.netInput = Long.parseLong(this.getNextValue());
		this.skipLineBreak();
		
		this.netOutput = Long.parseLong(this.getNextValue());
		this.skipLineBreak();
		
		this.instantaneousInput = Float.parseFloat(this.getNextValue());
		this.skipLineBreak();
		
		this.instantaneousOutput = Float.parseFloat(this.getNextValue());
		this.skipLineBreak();
		
		return true;
	}

	public static void main(String[] args) {
		Jedis redis = new Jedis("192.168.177.61", 10021);
		String s = redis.info("stats");
		redis.close();
		System.out.println(s);
		
		Info info = new StatsInfo();
		info.process(s);
		
		System.out.println(info.toJSON());
	}

}
