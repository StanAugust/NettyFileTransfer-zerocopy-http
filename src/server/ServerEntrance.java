package server;

import java.util.logging.Logger;

import common.ServerConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @ClassName: ServerEntrance   
 * @Description: 启动服务器   
 * @author Stan
 * @date: 2020年3月24日
 */
public class ServerEntrance {
	
	private static final Logger logger = Logger.getLogger(ServerEntrance.class.getName());
	
	public static void main(String[] args) {
		ServerEntrance.start();
	}
	
	/**
	 * @Description: 启动服务器
	 */
	public static void start() {
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);	//设置线程组
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		
		try {
			ServerBootstrap b = new ServerBootstrap();		
			
			b.group(bossGroup, workerGroup)
				.channel(NioServerSocketChannel.class)
				.option(ChannelOption.SO_BACKLOG, 200)	
				.childHandler(new ServerInitializer());
			
			//绑定端口
			ChannelFuture future = b.bind(ServerConfig.getServerport()).sync();
			logger.info("服务器已开启 >>>>>>>>>>>>>");
			
			future.channel().closeFuture().sync();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
}
