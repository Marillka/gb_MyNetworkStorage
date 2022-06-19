package server;

import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;


import java.nio.file.Path;
import java.nio.file.Paths;


import models.Enums;
import models.requests.*;
import models.responses.GetFileListResponse;
import models.responses.BasicResponse;


public class BasicHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Подключился клиент с адреса " + ctx.channel().remoteAddress() + ", контекст = " + ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        BasicRequest request = (BasicRequest) msg;

        if (request instanceof AuthRequest) {
            System.out.println("Прилетел AUTH_REQUEST");
            AuthRequest authRequest = (AuthRequest) request;
            if (authRequest.getLogin().equals("login") && authRequest.getPassword().equals("pass")) {
                ctx.writeAndFlush(Enums.LOGIN_OK_RESPONSE);
            } else {
                ctx.writeAndFlush(Enums.LOGIN_BAD_RESPONSE);
            }
            return;

        } else if (request instanceof GetFileListRequest) {
            Path serverPath = Paths.get(System.getProperty("user.home"));
            BasicResponse basicResponse = new GetFileListResponse(serverPath);
            ctx.writeAndFlush(basicResponse);
            return;
        } else if (request instanceof OpenDirRequest) {
            Path serverPath = Paths.get(((OpenDirRequest) request).getPathStr());
            BasicResponse basicResponse = new GetFileListResponse(serverPath);
            ctx.writeAndFlush(basicResponse);

        } else if (request instanceof OpenUpperDirRequest) {
            Path serverPath = Paths.get(((OpenUpperDirRequest) request).getGetParentPathStr());
            BasicResponse basicResponse = new GetFileListResponse(serverPath);
            ctx.writeAndFlush(basicResponse);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
