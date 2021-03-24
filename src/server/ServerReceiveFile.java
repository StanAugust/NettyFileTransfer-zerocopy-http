package server;

import java.io.RandomAccessFile;
import java.util.logging.Logger;

import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * @ClassName: ServerReceiveFile   
 * @Description: 服务器端的处理器，在{@link ServerInitializer#initChannel(io.netty.channel.Channel)}中调用 
 * @author Stan
 * @date: 2020年3月24日
 */
public class ServerReceiveFile extends ChannelInboundHandlerAdapter{
	
	private static final Logger logger = Logger.getLogger(ServerReceiveFile.class.getName());
	
	// TODO 存储路径需指定	
	private String path = "server_receive.txt";
	private RandomAccessFile file;
	
	/**
	 * @Description: 接收文件
	 * @param ctx
	 * @param msg
	 * @throws Exception   
	 * @see io.netty.channel.ChannelInboundHandlerAdapter#channelRead(io.netty.channel.ChannelHandlerContext, java.lang.Object)
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
	
		if(msg instanceof FullHttpRequest) {
			FullHttpRequest request = (FullHttpRequest)msg;
			
			file = new RandomAccessFile(path, "rw");
			file.seek(0);
			file.write(ByteBufUtil.getBytes(request.content()));
			
			file.close();
			logger.info("server receive all>>>>>>>>>>>>>>>");
		}
	}

	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.close();
	}
	
}
