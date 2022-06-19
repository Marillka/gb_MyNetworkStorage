package cloud;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import models.Enums;
import models.requests.GetFileListRequest;
import models.responses.BasicResponse;
import models.responses.GetFileListResponse;


import java.io.File;
import java.util.List;


public class ClientHandler extends ChannelInboundHandlerAdapter {

    private final ClientService clientService = new ClientService();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg.equals(Enums.LOGIN_OK_RESPONSE)) {
            System.out.println("Соединение установлено");
            clientService.loginSuccessful();
            ctx.writeAndFlush(new GetFileListRequest());
            return;
        } else if (msg.equals(Enums.LOGIN_BAD_RESPONSE)) {
            System.out.println("Неверный логин или пароль");
            LoginPanelController controllerObject = (LoginPanelController) ControllerRegistry.getControllerObject(LoginPanelController.class);
            controllerObject.setVisibleBadLogin();
            return;
        } else {
            BasicResponse response = (BasicResponse) msg;
            System.out.println("Получено " + response.getType());

            if (response instanceof GetFileListResponse) {
                List<File> serverItemsList = ((GetFileListResponse) response).getItemsList();
                String pathOfList = ((GetFileListResponse) response).getPathOfList();
                clientService.putServerFileList(serverItemsList, pathOfList);
                return;
            }

        }
    }

}

