package com.hujiang.redis.monitor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import com.hujiang.redis.monitor.info.Info;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;

public class RedisClusterInfo extends Info {
	
	// The key is the ID of a Redis server, and
	//   The value is the information obtained via 'CLUSTER INFO' command.
	private HashMap<String, ClusterInfoNode> clusterNodes = new HashMap<String, ClusterInfoNode>();
	
	// The key is the ID of a Redis server, and
	//   the value is the information obtained via 'INFO ALL' command.
	private HashMap<String, RedisInfo> redises = new HashMap<String, RedisInfo>();
	
	// The Redis servers whose cluster status is failure,
	//   therefore they have to be reconnected when they become connected again.
	private HashSet<RedisInfo> failure = new HashSet<RedisInfo>();
	
	final static String IPv4_LOCALHOST	= "127.0.0.1";
	final static String IPv6_LOCALHOST	= "127.0.0.1";
	
	private String publicIP				= null;
	private Jedis client				= null;
	private SimpleDateFormat dateFormat	= new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	
	public RedisClusterInfo(final String host, int port) {
		this.publicIP	= host;
		this.client		= new Jedis(host, port);
	}
	
	public RedisClusterInfo(final String url) {
		this.parseUrl(url);
	}
	private void parseUrl(String url) {
		if (url == null || url.isEmpty()) {
			return;
		}
		
		String host	= null;
		int port	= 0;
		
		int index = 0, begin = 0, len = url.length();
		while (index < len) {
			if (url.charAt(index) == ':') {
				// Get the IP address.
				host = url.substring(begin, index);
				begin = index + 1;
				break;
			}
			else {
				index ++;
			}
		}
		port = Integer.parseInt(url.substring(begin, len));
		
		this.publicIP	= host;
		this.client		= new Jedis(host, port);
	}
	
	public void close() {
		if (this.redises != null) {
			for (Entry<String, RedisInfo> e: this.redises.entrySet()) {
				e.getValue().close();
			}
			this.redises.clear();
		}
		
		if (this.client != null) {
			this.client.close();
		}
		
		this.clear();
	}
	
	@Override
	public void clear() {
		if (this.clusterNodes != null) {
			this.clusterNodes.clear();
		}
		
		if (this.redises != null) {
			this.redises.clear();
		}
		
		if (this.failure != null) {
			this.failure.clear();
		}
		
		this.s			= null;
		this.index		= 0;
		this.len		= 0;
	}
	
	public void update() {
		String s = null;
		
		if (this.redises.isEmpty()) {
			// Within the Redis cluster, no Redis servers exist.
			if (!this.client.isConnected()) {
				this.client.connect();
			}
			s = this.client.clusterNodes();
		}
		else {
			// Get the first connected Redis server within the Redis cluster.
			for (Entry<String, RedisInfo> e: this.redises.entrySet()) {
				RedisInfo info = e.getValue();
				if (info == null) {
					continue;
				}
				
				s = info.clusterNodes();
				if (s == null || s.isEmpty()) {
					continue;
				}

				break;
			}
		}
		
		this.process(s);
	}

	@Override
	public boolean process(String s) {
		if (!this.preprocess(s)) {
			return false;
		}
		
		HashMap<String, ClusterInfoNode> masterNodes = new HashMap<String, ClusterInfoNode>();
		ClusterInfoNode clusterInfoNode = null;
		while (this.index < this.len) {
			clusterInfoNode = new ClusterInfoNode();
			
			clusterInfoNode.slotStart	= 0;
			clusterInfoNode.slotEnd		= 0;
			
			clusterInfoNode.UUID = this.getNextToken();
			clusterInfoNode.host = this.getNextToken();
			clusterInfoNode.port = Integer.parseUnsignedInt(this.getNextToken());
			
			clusterInfoNode.master = this.getNextToken().contains("master");
			if (clusterInfoNode.master) {
				this.getNextToken();
				this.getNextToken();
				
				clusterInfoNode.epoch		= Long.parseLong(this.getNextToken());
				clusterInfoNode.connected	= this.getNextToken().contains("disconnected") ? false : true;
				
				if (clusterInfoNode.connected) {
					if (this.s.charAt(this.index) == ' ') {
						clusterInfoNode.slotStart	= Integer.parseInt(this.getNextToken());
						clusterInfoNode.slotEnd		= Integer.parseInt(this.getNextToken());
					}
				}
				
				masterNodes.put(clusterInfoNode.UUID, clusterInfoNode);
			}
			else {
				clusterInfoNode.masterUUID = this.getNextToken();
				
				clusterInfoNode.ping = Long.parseLong(this.getNextToken());
				clusterInfoNode.pong = Long.parseLong(this.getNextToken());
				
				clusterInfoNode.epoch		= Long.parseLong(this.getNextToken());
				clusterInfoNode.connected	= this.getNextToken().contains("disconnected") ? false : true;
			}
			this.clusterNodes.put(clusterInfoNode.UUID, clusterInfoNode);
			
			this.skipALine();
		}
		
		// Update master-slave relation.
		if (!(masterNodes.isEmpty())) {
			for (Entry<String, ClusterInfoNode> e: this.clusterNodes.entrySet()) {
				clusterInfoNode = e.getValue();
				if (!clusterInfoNode.master) {
					masterNodes.get(clusterInfoNode.masterUUID).slaves.put(clusterInfoNode.UUID, clusterInfoNode);
				}
			}
			masterNodes.clear();
		}
		
		// Update IP addresses.
		if ((this.publicIP != null) && (!this.publicIP.isEmpty())) {
			for (Entry<String, ClusterInfoNode> e: this.clusterNodes.entrySet()) {
				clusterInfoNode = e.getValue();
				if (clusterInfoNode.host.compareTo(RedisClusterInfo.IPv4_LOCALHOST) == 0) {
					clusterInfoNode.host = this.publicIP;
				}
			}
		}
		
		// Update local information for each Redis server.
		RedisInfo redisInfo = null;
		for (Entry<String, ClusterInfoNode> e: this.clusterNodes.entrySet()) {
			clusterInfoNode = e.getValue();
			redisInfo = this.redises.get(clusterInfoNode.UUID);
			if (redisInfo == null) {
				redisInfo = new RedisInfo(clusterInfoNode.host, clusterInfoNode.port);
				this.redises.put(clusterInfoNode.UUID, redisInfo);
			}
			
			if (clusterInfoNode.connected) {
				// Only process Redis servers connected to this Cluster.
				if (this.failure.contains(redisInfo)) {
					// This Redis server recovers from a failure, then
					//   we have to connect to it again.
					redisInfo.connect();
					
					// Remove it from the failure set.
					this.failure.remove(redisInfo);
				}
				redisInfo.update();
			}
			else {
				// Add this Redis server to the failure set.
				this.failure.add(redisInfo);
			}
		}
		
		return true;
	}
	
	public Set<HostAndPort> getHostAndPorts() {
		HashSet<HostAndPort> result = new HashSet<HostAndPort>();
		
		ClusterInfoNode node = null;
		for (Entry<String, ClusterInfoNode> e: this.clusterNodes.entrySet()) {
			node = e.getValue();
			result.add(new HostAndPort(node.host, node.port));
		}
		return result;
	}
	
	public void flushAll() {
		for (Entry<String, ClusterInfoNode> e: this.clusterNodes.entrySet()) {
			ClusterInfoNode node = e.getValue();
			if (node == null) {
				continue;
			}
			if (!node.master) {
				continue;
			}
			
			RedisInfo info = this.redises.get(e.getKey());
			if (info == null) {
				continue;
			}
			info.flush();
		}
	}
	
	@Override
	public String toJSON() {
		String s = "{\"time\":\"" + this.dateFormat.format(new Date()) + "\",masters:[";
		
		boolean firstMaster = true;
		boolean firstSlave = true;
		ClusterInfoNode master = null;
		ClusterInfoNode slave = null;
		RedisInfo redisInfo = null;
		String details = null;
		for (Entry<String, ClusterInfoNode> e: this.clusterNodes.entrySet()) {
			master = e.getValue();
			if (!master.master) {
				continue;
			}
			
			if (firstMaster) {
				firstMaster = false;
				s += "{";
			}
			else {
				s += ",{";
			}
			s += "\"UUID\":\"" + master.UUID + "\",";
			s += "\"host\":\"" + master.host + "\",";
			s += "\"port\":\"" + master.port + "\",";
			s += "\"epoch\":\"" + master.epoch + "\",";
			s += "\"slotStart\":\"" + master.slotStart + "\",";
			s += "\"slotEnd\":\"" + master.slotEnd + "\",";
			s += "\"connected\":\"" + master.connected + "\",";
			
			redisInfo = this.redises.get(master.UUID);
			if (redisInfo != null) {
				details = redisInfo.toJSON();
				if ((details != null) && (!details.isEmpty())) {
					s += details + ",";
				}
			}
			
			s += "slaves:[";
			firstSlave = true;
			for (Entry<String, ClusterInfoNode> d: master.slaves.entrySet()) {
				slave = d.getValue();
				
				if (firstSlave) {
					firstSlave = false;
					s += "{";
				}
				else {
					s += ",{";
				}
				s += "\"epoch\":\"" + slave.epoch + "\",";
				s += "\"host\":\"" + slave.host + "\",";
				s += "\"port\":\"" + slave.port + "\",";
				s += "\"connected\":\"" + slave.connected + "\"";
				
				redisInfo = this.redises.get(slave.UUID);
				if (redisInfo != null) {
					s += "," + redisInfo.toJSON();
				}
				
				s += "}";
			}
			s += "]}";
		}
		
		s += "]}";
		
		return s;
	}
	
	// Cluster information obtained via 'CLUSTER NODES' command, and
	//   each node represents a Redis server within the cluster.
	class ClusterInfoNode {
		public String	UUID;
		
		public String	host;
		public int		port;
		
		public int		slotStart	= 0;
		public int		slotEnd		= 0;
		
		public boolean	connected	= false;
		public boolean	master		= false;
		
		public long		ping;
		public long		pong;
		public long		epoch;
		
		public String	masterUUID;
		public HashMap<String, ClusterInfoNode> slaves = new HashMap<String, ClusterInfoNode>();
		
		public void clear() {
			this.epoch		= 0;
			this.UUID		= null;
			
			this.host		= null;
			this.epoch		= 0;
			
			this.slotStart	= 0;
			this.slotEnd	= 0;
			
			this.connected	= false;
			this.master		= false;
			
			this.master		= false;
			if (this.slaves != null) {
				this.slaves.clear();
			}
		}
	}
	
	public static void main(String[] args) {	
		RedisClusterInfo info = new RedisClusterInfo("192.168.177.61", 10011);
		info.update();
		System.out.println(info.toJSON());
	}
}
