package client;

import javax.net.ssl.SSLEngine;

import HttpSSL.ReadCA;
import HttpSSL.SSLMODE;
import common.FilePath;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.ssl.SslHandler;


/**
 * @ClassName: ClientInitializer   
 * @Description: 装配流水线，在{@link ClientEntrance#start()}中被调用
 * @author Stan
 * @date: 2020年3月24日
 */
public class ClientInitializer extends ChannelInitializer<Channel>{
	
	@Override
	protected void initChannel(Channel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		
		// 进行CA认证
		SSLEngine engine = verifyCA("CA");
		pipeline.addLast(new SslHandler(engine));
				
		// 编解码器
		pipeline.addLast(new HttpClientCodec());
		// 聚合器，负责将http聚合成完整的消息，而不是原始的多个部分
		pipeline.addLast(new HttpObjectAggregator(1024*1024));
		//自定义的处理器
		pipeline.addLast("sender", new ClientSendFile());
	}
	
	
	/**
	 * @Description: CA认证
	 * @return
	 * @return: SSLEngine
	 */
	private SSLEngine verifyCA(String tlsMode) {
		SSLEngine engine = null;

		if (SSLMODE.CA.toString().equals(tlsMode)) {
			// 当模式是CA的时候，导入证书的内容
			engine = ReadCA.getClientContext(SSLMODE.CA.toString(), null, FilePath.CA_PATH)
						   .createSSLEngine();
		} else {
			// 如果证书不是CA模式就显示错误信息
//			logger.info("CA验证错误：" + tlsMode);
			System.exit(-1);
		}
		
		// 目前是单向认证
		engine.setUseClientMode(true);

		return engine;
	}
}
