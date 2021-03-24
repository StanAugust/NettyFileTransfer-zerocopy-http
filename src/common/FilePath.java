package common;

/**
 * @ClassName: FilePath   
 * @Description: 存储相关文件路径   
 * @author Stan
 * @date: 2021年3月24日
 */
public class FilePath {
	
	// 服务器CA证书的路径
	public static final String SSL_PK_PATH = System.getProperty("user.dir") + "/SSLCertificate/ServerCA.jks";
	
	// 客户端CA验证文件的路径
	public static final String CA_PATH = System.getProperty("user.dir") + "/ssl/cChat.jks";
}
