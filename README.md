#WE-Redis Web Admin#

Monitoring a Redis cluster is fairly a big challenge yet, that is why we develop such an off-the-shelf tool. Generally speaking, this tool is Java application server providing a Web UI to show information on the status of a Redis cluster.

##Requirements##

- WE-Redis cluster (or Redis cluster).
- Apache Tomcat Server (version 8.0 or higher).
- Jedis (supporting Redis 3.0.0 or higher).
- Browser with WebSocket support.

##Installation##

1. Add `jedis.jar` (try to compile one by yourself) to the `WebContent/WEB-INF/lib` fold, or simply append it to your `CLASSPATH` environment variable.
2. Open the `WebContent/WEB-INF/web.xml` file with a text editor, find a parameter named `RedisServers`, and then change its value to the address of a Redis server within your Redis cluster. The default value is `127.0.0.1:6379`, pointing to the default service port of local Redis server.
3. Place the entire `WebContent` fold in the `{CATALINA_HOME}/webapps` fold, and change its name to someone you feel good to remember, for instance, `redis-monitor` is a recommended one.
4. Launch your Apache Tomcat server, and visit the web app through a web browser that supports WebSocket. The monitor UI will be shown in 5 seconds, if everything goes well. Currently, the web UI supports American English and Simplified Chinese. And you can switch the UI language through two small flags on your right top.

Enjoy!

