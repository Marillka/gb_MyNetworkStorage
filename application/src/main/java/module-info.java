module application {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires io.netty.codec;
    requires io.netty.all;
    requires io.netty.transport;

    requires lombok;
    requires model;
    exports cloud;
    opens cloud to javafx.fxml;
}