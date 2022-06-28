package com.example.whiteboard;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.Objects;

public class WhiteBoard extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Scene scene = new Scene(FXMLLoader.load(Objects.requireNonNull(getClass().getResource("whiteboard.fxml"))),
                950, 600);
        scene.getStylesheets().add(String.valueOf(getClass().getResource("style.css")));
        //stage.setTitle("Hello!");
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                for (Tab t : ((TabPane) scene.getRoot()).getTabs()) {
                    if (((WhiteBoardPage) t).checkDirty(windowEvent)) break;
                }
            }
        });
        stage.setTitle("WhiteBoard BETA");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}