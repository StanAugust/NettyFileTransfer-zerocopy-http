package server;

import java.util.logging.Logger;

import javax.net.ssl.SSLEngine;

import HttpSSL.ReadCA;
import HttpSSL.SSLMODE;
import common.FilePath;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslHandler;

/**
 * @ClassName: ServerInitializer
 * @Description: 装配子通道流水线，在server.ServerEntrance startServer中调用
 * @author Stan
 * @date: 2020年3月24日
 */
public class ServerInitializer extends ChannelInitializer<Channel> {

	private static final Logger logger = Logger.getLogger(ServerInitializer.class.getName());

	@Override
	protected void initChannel(Channel ch) throws Exception {

		logger.info("客户端 " + ch.remoteAddress() + " 已连接");

		ChannelPipeline pipeline = ch.pipeline();

		// 进行CA认证
		SSLEngine engine = verifyCA("CA");
		pipeline.addLast(new SslHandler(engine));

		// 编解码器
		pipeline.addLast(new HttpServerCodec());
		// 聚合器，负责将http聚合成完整的消息，而不是原始的多个部分
		pipeline.addLast(new HttpObjectAggregator(1024*1024));
		//自定义的处理器
		pipeline.addLast(new ServerReceiveFile());

	}

	/**
	 * @Description: 进行CA认证
	 * @return
	 */
	private synchronized SSLEngine verifyCA(String tlsMode) {

		SSLEngine engine = null;
		if (SSLMODE.CA.toString().equals(tlsMode)) {
			// 当模式是CA的时候，导入证书的内容
			engine = ReadCA.getServerContext(SSLMODE.CA.toString(), FilePath.SSL_PK_PATH, null).createSSLEngine();
		} else {
			// 如果证书不是CA模式就显示错误信息
			logger.info("CA验证错误：" + tlsMode);
			System.exit(-1);
		}
		// 目前是单向认证
		engine.setUseClientMode(false);

		return engine;
	}

}
