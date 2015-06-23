package com.hujiang.redis.monitor;

import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.hujiang.redis.monitor.RedisClusterInfo;
import com.hujiang.redis.monitor.command.Command;

@ServerEndpoint(value = "/info")
public class Broadcaster {
	private static final RedisClusterInfo info = new RedisClusterInfo(Configuration.getInstance().getRedisServers());

	@OnOpen
	public void OnOpen(Session session, EndpointConfig config) {  
		;
	}
	
	@OnMessage
	public void OnMessage(Session session, String message) {
		switch (Command.parse(message, null)) {
		case INFO:
			Broadcaster.info.update();
			session.getAsyncRemote().sendText(Broadcaster.info.toJSON());
			break;
		case FLUSH:
		case FLUSH_ALL:
			Broadcaster.info.flushAll();
			break;
		default:
			session.getAsyncRemote().sendText("Unknown Command");
			break;
		}
	}
	
	@OnClose
	public void OnClose(Session session) {
		;
	}
}
