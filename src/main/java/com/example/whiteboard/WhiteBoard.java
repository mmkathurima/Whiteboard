package com.example.whiteboard;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.Objects;

public class WhiteBoard extends Application {
    public WhiteBoard() {
        super();
    }

    @Override
    public void start(Stage stage) throws IOException {
        final Scene scene = new Scene(FXMLLoader.load(Objects.requireNonNull(this.getClass().getResource("whiteboard.fxml"))), 1020.0, 600.0);
        scene.getStylesheets().add(String.valueOf(this.getClass().getResource("style.css")));
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                                    public void handle(WindowEvent windowEvent) {
                                        for (Tab t : ((TabPane) scene.getRoot()).getTabs()) {
                                            WhiteBoardPage page = (WhiteBoardPage) t;
                                            if (page.isDirty(windowEvent)) {
                                                page.confirmExit(windowEvent, new Alert(Alert.AlertType.WARNING,
                                                                "You have unsaved changes.\nAre you sure you want to exit this application?",
                                                                ButtonType.OK,
                                                                ButtonType.CANCEL
                                                        ).showAndWait()
                                                );
                                                break;
                                            }
                                        }

                                    }
                                }
        );
        stage.setTitle("WhiteBoard");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
