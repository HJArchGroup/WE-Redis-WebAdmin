package com.hujiang.redis.monitor;

import redis.clients.jedis.Jedis;

import com.hujiang.redis.monitor.info.CPUInfo;
import com.hujiang.redis.monitor.info.MemoryInfo;
import com.hujiang.redis.monitor.info.StatsInfo;
import com.hujiang.redis.monitor.info.SystemInfo;

public class RedisInfo {
	private CPUInfo		cpu		= new CPUInfo();
	private MemoryInfo	memory	= new MemoryInfo();
	private SystemInfo	system	= new SystemInfo();
	private StatsInfo	stats	= new StatsInfo();
	
	private Jedis		client	= null;
	private String		json	= null;
	
	private String		host	= null;
	private int			port	= 0;
	
	public RedisInfo(final String host, int port) {
		this.host = host;
		this.port = port;
		this.connect();
	}
	
	public synchronized void flush() {
		if (this.client != null) {
			this.client.flushAll();
		}
	}
	
	public void connect() {
		this.client = new Jedis(this.host, this.port);
	}
	
	public void close() {
		if (this.client != null) {
			this.client.close();
		}
	}
	
	public void clear() {
		if (this.cpu != null) {
			this.cpu.clear();
		}
		if (this.memory != null) {
			this.memory.clear();
		}
		if (this.system != null) {
			this.memory.clear();
		}
		if (this.stats != null) {
			this.stats.clear();
		}
		
		this.json = null;
	}
	
	public synchronized void update() {
		this.clear();
		
		if (this.client == null) {
			System.out.println("this.client == null");
			return;
		}
		
		/*
		if (!this.client.isConnected()) {
			System.out.println("!this.client.isConnected()");
			this.client.connect();
			
			if (!this.client.isConnected()) {
				System.out.println("!!this.client.isConnected()");
			}
		}
		*/
		
		//if (this.client.isConnected()) {
			try {
				this.cpu.process(this.client.info("cpu"));
			}
			catch (Exception e) {
				;
			}
			this.json = this.cpu.toJSON();
			
			try {
				this.memory.process(this.client.info("memory"));
			}
			catch (Exception e) {
				;
			}
			if (!this.json.isEmpty()) {
				this.json += ",";
			}
			this.json += this.memory.toJSON();
			
			try {
				this.system.process(this.client.info("sysinfo"));
			}
			catch (Exception e) {
				;
			}
			if (!this.json.isEmpty()) {
				this.json += ",";
			}
			this.json += this.system.toJSON();
			
			try {
				this.stats.process(this.client.info("stats"));
			}
			catch (Exception e) {
				;
			}
			if (!this.json.isEmpty()) {
				this.json += ",";
			}
			this.json += this.stats.toJSON();
		//}
	}
	
	public String toJSON() {
		return this.json;
	}
	
	public String clusterNodes() {
		String result = null;
		try {
			result = this.client.clusterNodes();
		}
		catch (Exception e)
		{
			result = null;
		}
		return result;
	}

	public static void main(String[] args) {
		RedisInfo redis = new RedisInfo("192.168.177.61", 10021);
		redis.update();
		System.out.println(redis.toJSON());
	}
}
