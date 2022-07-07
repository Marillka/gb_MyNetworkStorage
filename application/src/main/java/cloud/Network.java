package cloud;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import models.requests.BasicRequest;

public class Network {

    private static final Network INSTANCE = new Network();
    private ChannelFuture channelFuture;
    private static final String HOST = "localhost";
    private static final int PORT = 8189;

    private Network() {
        Thread t = new Thread(() -> {
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(workerGroup);
                bootstrap.channel(NioSocketChannel.class);
                bootstrap.remoteAddress(HOST, PORT);
                bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(
                                new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                new ObjectEncoder(),
                                new ClientHandler()
                        );
                    }
                });
                channelFuture = bootstrap.connect().sync();
                channelFuture.channel().closeFuture().sync();

//                ChannelFuture future = bootstrap.connect(HOST, PORT).sync();
//                this.channel = future.channel();
//                future.channel().closeFuture().sync();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                workerGroup.shutdownGracefully();
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public void close() {
        channelFuture.channel().close();
    }

    public void sendRequest(BasicRequest basicRequest) throws InterruptedException {
        channelFuture.channel().writeAndFlush(basicRequest);// sync();
    }

    public static Network getInstance() {
        return INSTANCE;
    }

}






