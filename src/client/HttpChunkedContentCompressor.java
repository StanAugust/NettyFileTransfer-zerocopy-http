package client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.HttpContentCompressor;

/**
 * @ClassName: HttpChunkedContentCompressor
 * @Description: 将 {@link Bytebuf}封装到 {@link DefaultHttpContent} 中
 * @author Stan
 * @date: 2021年3月11日
 */
public class HttpChunkedContentCompressor extends HttpContentCompressor {

	public HttpChunkedContentCompressor(int compressionLevel) {
		super(compressionLevel);
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		if (msg instanceof ByteBuf) {

			/*
			 * 将ByteBuf转换为HttpContent，使其能够使用HttpContentCompressor
			 * 
			 * 如果pipeline中有 HttpContentCompressor，那么使用ChunkedWriteHandler发送文件就需要添加这一步
			 */
			ByteBuf buf = (ByteBuf) msg;
			
			if (buf.isReadable()) {
				/*
				 * 只编码非空缓冲区，因为空缓冲区可用于确定内容何时被刷新
				 */
				msg = new DefaultHttpContent(buf);
			}
		}
		super.write(ctx, msg, promise);
	}

}
