package common;

/**
 * @ClassName: ServerConfig   
 * @Description: 服务器的相关参数   
 * @author Stan
 * @date: 2021年3月24日
 */
public class ServerConfig {
	
	
	private static final String hostName = "127.0.0.1";
	private static final int serverPort = 8887;
	
	
	public static String getHostname() {
		return hostName;
	}
	public static int getServerport() {
		return serverPort;
	}
}
