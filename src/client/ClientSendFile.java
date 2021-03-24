package client;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.activation.MimetypesFileTypeMap;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @ClassName: ClientSendFile
 * @Description: 客户端的处理器，在在client.ClientInitializer initChannel中被调用
 * @author Stan
 * @date: 2020年3月24日
 */
public class ClientSendFile extends ChannelInboundHandlerAdapter {

	private static final Logger logger = Logger.getLogger(ClientSendFile.class.getName());
	
	// TODO 指定待传文件路径
	private String filePath = "test2.txt";
	private RandomAccessFile file;
	private long fileLen = -1;
	
	private ChannelHandlerContext ctx;

	/**
	 * @Description: 连接一激活就向服务器发送文件，发送文件的函数在这里修改
	 * 
	 * @param ctx
	 * @throws Exception
	 * @see io.netty.channel.ChannelInboundHandlerAdapter#channelActive(io.netty.channel.ChannelHandlerContext)
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		
		this.ctx = ctx;

		try {
			file = new RandomAccessFile(filePath, "r");
			fileLen = file.length();

		} catch (Exception e) {
			e.printStackTrace();
			
		} finally {
			if (fileLen < 0 && file != null) {
				file.close();
			}
		}
		
		// 具体处理发送
		send0();
	}

	/**
	 * @Description: 具体处理发送  
	 * @throws IOException
	 */
	private void send0() throws IOException {
		// 构建http请求
		HttpRequest request = initHttpRequest(new File(filePath));

		// 写入http request首行和头部
		ctx.write(request);

		// 写入http content
		ChannelFuture sendFileFuture;
		ChannelFuture lastContentFuture;

		// SSL not enabled - can use zero-copy file transfer.
		if (ctx.pipeline().get(SslHandler.class) == null) {
			sendFileFuture = ctx.write(new DefaultFileRegion(file.getChannel(), 0, fileLen),
					ctx.newProgressivePromise());

	        // 写入结束符
			lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);

		// SSL enabled - cannot use zero-copy file transfer.
		} else {
			/*
			 * TODO 如果要往pipeline中添加{@link HttpContentCompressor}
			 * 
			 * 不能使用{@link HttpContentCompressor}来压缩ChunkedFile，因为它不支持ByteBuf
			 * 使用{@link HttpChunkedContentCompressor}来处理ByteBuf实例，它扩展了HttpContentCompressor。
			 * 
			 * 实际上{@link HttpContentCompressor}是用来压缩{@link HttpResponse}的，所以这里在客户端添加只是举个例子
			 */
//			ctx.pipeline().addBefore("sender", "chunked compressor", new HttpChunkedContentCompressor(6));
//			ctx.pipeline().addBefore("chunked compressor", "chunked writer", new ChunkedWriteHandler());
	        
			// 如果pipeline中无{@link HttpContentCompressor}，只需添加 {@link ChunkedWriteHandler} 用来传输  ChunkedFile
			ctx.pipeline().addBefore("sender", "chunked writer", new ChunkedWriteHandler());
			// 使用 {@link HttpChunkedInput}，会自动写入 LastHttpContent
			sendFileFuture = ctx.writeAndFlush(new HttpChunkedInput(new ChunkedFile(file)),
					ctx.newProgressivePromise());
			lastContentFuture = sendFileFuture;
			
		}

		// 监听请求是否传输成功
		sendFileFuture.addListener(new ChannelProgressiveFutureListener() {

			@Override
			public void operationComplete(ChannelProgressiveFuture future) throws Exception {
				if (future.isSuccess()) {
					logger.info("文件上传完毕>>>>>>>>>");
					file.close();
				}
			}

			@Override
			public void operationProgressed(ChannelProgressiveFuture future, long progress, long total)
					throws Exception {
				if (total < 0) { // total unknown
					logger.info(future.channel() + " Transfer progress:" + progress);
				} else {
					logger.info(future.channel() + " Transfer progress:" + progress + "/" + total);
				}
			}
		});
	}
	
	/**
	 * @Description: 初始化一个http request,设置基本信息
	 * @return
	 */
	private HttpRequest initHttpRequest(File file) {
		
		HttpRequest request = new DefaultHttpRequest(HttpVersion.valueOf("HTTP/2.0"), 
													 HttpMethod.valueOf("TRANSFER"),
													 "/");
		
		request.headers().set(HttpHeaderNames.DATE, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		request.headers().set(HttpHeaderNames.CONTENT_TYPE, new MimetypesFileTypeMap().getContentType(file.getPath()));		
		HttpUtil.setContentLength(request, fileLen);		
		
		return request;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
}
