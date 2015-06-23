package com.hujiang.redis.monitor.deprecated;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import com.hujiang.redis.monitor.RedisClusterInfo;
import com.hujiang.redis.monitor.RedisInfo;

import redis.clients.jedis.HostAndPort;

public class ClusterInfo {
	
	private RedisClusterInfo cluster = null;
	private HostAndPort hap = null;
	
	private HashMap<HostAndPort, RedisInfo> clients = new HashMap<HostAndPort, RedisInfo>();
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	private String json = null;
	
	public ClusterInfo(final String host, int port) {
		this.hap = new HostAndPort(host, port);
		this.cluster = new RedisClusterInfo(host, port);
		this.update();
	}
	
	private RedisInfo getOrCreateClient(HostAndPort hap) {
		RedisInfo client = this.clients.get(hap);
		if (client == null) {
			client = new RedisInfo(hap.getHost(), (short)hap.getPort());
			System.out.println("new RedisInfo(" + hap.getHost() + ":" + hap.getPort() + ")");
			this.clients.put(hap, client);
		}
		return client;
	}
	
	public void update() {
		this.json = "{\"time\":\"" + this.dateFormat.format(new Date()) + "\",";
		
		// Get client hosts and ports.
		RedisInfo client = this.getOrCreateClient(this.hap);
		cluster.process(client.clusterNodes());
		Set<HostAndPort> haps = cluster.getHostAndPorts();
		
		this.json += cluster.toJSON();
		this.json += ",nodeInfo:[";
		
		// Get or create all client.
		boolean first = true;
		for (HostAndPort hap: haps) {
			client = this.getOrCreateClient(hap);
			client.update();
			
			if (first) {
				first = false;
				this.json += "{";
			}
			else {
				this.json += ",{";
			}
			this.json += client.toJSON();
			this.json += "}";
		}
		
		this.json += "]}";
	}
	
	public String toJSON() {
		return this.json;
	}

	public static void main(String[] args) {
		ClusterInfo redisCluster = new ClusterInfo("192.168.177.61", 10021);
		System.out.println(redisCluster.toJSON());
	}
}
