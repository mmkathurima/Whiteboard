package com.example.whiteboard;

import javafx.fxml.Initializable;
import javafx.scene.control.TabPane;

import java.net.URL;
import java.util.ResourceBundle;

public class WhiteBoardController implements Initializable {
    public TabPane tabPane;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        WhiteBoardPage first = new WhiteBoardPage();
        tabPane.getTabs().add(first);
        first.setText("Page 1");
    }
}
