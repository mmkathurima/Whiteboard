package com.example.whiteboard;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.scene.control.TabPane;

public class WhiteBoardController implements Initializable {
   public TabPane tabPane;

   public WhiteBoardController() {
      super();
   }

   @Override
   public void initialize(URL url, ResourceBundle resourceBundle) {
      WhiteBoardPage first = new WhiteBoardPage();
      this.tabPane.getTabs().add(first);
      first.setText("Page 1");
   }
}
