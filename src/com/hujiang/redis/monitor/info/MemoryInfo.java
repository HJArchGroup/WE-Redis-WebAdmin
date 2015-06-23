package com.hujiang.redis.monitor.info;

import redis.clients.jedis.Jedis;

public class MemoryInfo extends Info {
	public int		used;
	public int		rss;
	public int		peak;
	public int		lua;
	public float	fragRatio;
	
	@Override
	public void clear() {
		this.used		= 0;
		this.rss		= 0;
		this.peak		= 0;
		this.lua		= 0;
		this.fragRatio	= 0.0f;
		
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
		
		this.skipALine();
		
		// used_memory
		this.used = Integer.parseInt(this.getNextValue());
		this.skipLineBreak();
		
		// used_memory_human
		this.skipLineBreak();
		this.skipALine();
		
		// used_memory_rss
		this.rss = Integer.parseInt(this.getNextValue());
		this.skipLineBreak();
		
		// used_memory_peak
		this.peak = Integer.parseInt(this.getNextValue());
		this.skipLineBreak();
		
		// used_memory_peak_human
		this.skipLineBreak();
		this.skipALine();
		
		// used_memory_lua
		this.lua = Integer.parseInt(this.getNextValue());
		this.skipLineBreak();
		
		// mem_fragmentation_ratio
		this.fragRatio = Float.parseFloat(this.getNextValue());
		
		// mem_allocator
		
		return true;
	}

	@Override
	public String toJSON() {
		String s = "memory:{";
		s += "\"used\":\"" + String.format("%,d", this.used) + "\",";
		s += "\"rss\":\"" + String.format("%,d", this.rss) + "\",";
		s += "\"peak\":\"" + String.format("%,d", this.peak) + "\",";
		s += "\"lua\":\"" + String.format("%,d", this.lua) + "\",";
		s += "\"fragRatio\":\"" + String.format("%.02f", this.fragRatio) + "\"";
		s += "}";
		
		return s;
	}
	
	public static void main(String[] args) {
		Jedis redis = new Jedis("192.168.177.61", 10021);
		String s = redis.info("memory");
		redis.close();
		System.out.println(s);
		
		Info info = new MemoryInfo();
		info.process(s);
		
		System.out.println(info.toJSON());
	}
}
