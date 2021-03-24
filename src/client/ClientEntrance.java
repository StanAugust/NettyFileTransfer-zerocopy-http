package client;

import java.util.logging.Logger;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import common.ServerConfig;

/**
 * @ClassName: ClientEntrance   
 * @Description: 启动客户端   
 * @author Stan
 * @date: 2020年3月24日
 */
public class ClientEntrance {
	
	private static final Logger logger = Logger.getLogger(ClientEntrance.class.getName()); 
	
	public static void main(String[] args) {
		ClientEntrance.start();		
	}
	
	/**
	 * @Description: 启动客户端
	 */
	public static void start() {
		
		EventLoopGroup group = new NioEventLoopGroup();	//线程组
		
		try {
			Bootstrap b = new Bootstrap();	//启动配置
			
			b.group(group)	
				.channel(NioSocketChannel.class)
				.handler(new ClientInitializer());
			
			
			//连接服务器
			ChannelFuture future = b.connect(ServerConfig.getHostname(), ServerConfig.getServerport()).sync();
			
			logger.info("客户端启动完成，地址：" + future.channel().localAddress());
			
			future.channel().closeFuture().sync();	//保证连接一直在线
				
		} catch (Exception e) {
			e.printStackTrace();
			
		} finally {
			group.shutdownGracefully();
		}
		
	}
	
	
	
}
