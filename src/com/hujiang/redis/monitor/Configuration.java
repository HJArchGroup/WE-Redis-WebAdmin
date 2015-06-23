package com.hujiang.redis.monitor;

public class Configuration {
	
	private static Configuration instance = null;
	public synchronized static Configuration getInstance() {
		if (Configuration.instance == null) {
			Configuration.instance = new Configuration();
		}
		return Configuration.instance;
	}
	private Configuration() {
		this.clear();
	}
	
	private String redisServers;
	public synchronized void setRedisServers(String s) {
		this.redisServers = s;
	}
	public synchronized String getRedisServers() {
		return this.redisServers;
	}
	
	private void clear() {
		this.redisServers = "127.0.0.1:6379";
	}
}
