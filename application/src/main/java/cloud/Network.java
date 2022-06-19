package cloud;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
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

    // Singleton - это когда у нас класс, объект которого будет во всей программе только один (до спринга был на всех проектах)
    // описали класс так, что из него может создасться только один объект, и сделали конструктор приватным, чтобы никто не смог создать объект этого класса. Таким образом у нас будет один класс Network - который будет отвечать за работу с сетью.
    private static final Network INSTANCE = new Network();

    private Channel channel;
    private static final String HOST = "localhost";
    private static final int PORT = 8189;

    private Network() {
        Thread t = new Thread(() -> {
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(workerGroup);
                bootstrap.channel(NioSocketChannel.class);
                bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(
//                                new ObjectDecoder(MB_20, ClassResolvers.cacheDisabled(null)),
                                new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                new ObjectEncoder(),
                                new ClientHandler()
                        );
                    }
                });
                ChannelFuture future = bootstrap.connect(HOST, PORT).sync();
                this.channel = future.channel();
                future.channel().closeFuture().sync();
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
        channel.close();
    }

    public void sendRequest(BasicRequest basicRequest) throws InterruptedException {
        channel.writeAndFlush(basicRequest).sync();
    }

    public static Network getInstance() {
        return INSTANCE;
    }

}






