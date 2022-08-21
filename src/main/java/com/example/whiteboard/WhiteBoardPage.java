package com.example.whiteboard;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import org.controlsfx.dialog.FontSelectorDialog;
import org.kordamp.ikonli.javafx.FontIcon;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class WhiteBoardPage extends javafx.scene.control.Tab {
    BorderPane borderPane = new BorderPane();
    Pane drawPane = new Pane();
    ToolBar toolBar = new ToolBar();
    private final ToggleButton pen = new ToggleButton("Pen"), line_ = new ToggleButton("Line"),
            rect = new ToggleButton("Rectangle"), circle = new ToggleButton("Circle"),
            text_ = new ToggleButton("Text"), eraser = new ToggleButton("Eraser"),
            pointer = new ToggleButton("Pointer");
    private final Button undo = new Button("Undo"), redo = new Button("Redo"), clear = new Button("Clear"),
            fontPick = new Button("Fonts"), export = new Button("Save As"), addPage = new Button();
    private final Text thiccness = new Text();
    private final ColorPicker colorPicker = new ColorPicker();
    private final Slider thicc = new Slider();
    private final ChoiceBox<String> bg = new ChoiceBox<>();

    private Font defaultFont;
    private Color color = Color.BLACK, eraserColor = null;
    private Line drawLine;
    private Path path;
    private Rectangle rectangle;
    private Ellipse ellipse;

    private Tools tool;
    private final Stack<Node> last = new Stack<>();
    private final List<Tools> toolsList = Arrays.asList(Tools.values());
    private final List<ButtonBase> hist = new ArrayList<>();

    private TextArea addText;
    private StackPane stackPane;
    private final WhiteBoardPage me = this;
    ImageView iv = new ImageView();

    private double x0, y0, x1, y1, startingPosX, startingPosY;
    private static int counter = 2;

    public WhiteBoardPage() {
        this.setOnCloseRequest(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                //System.out.println(drawPane.getChildren().size());
                if (getTabPane().getTabs().size() == 1) {
                    if (isDirty(event)) {
                        if (confirmExit(event, new Alert(Alert.AlertType.WARNING,
                                "You have unsaved changes.\nAre you sure you want to exit this application?",
                                ButtonType.OK, ButtonType.CANCEL).showAndWait())) {
                            Platform.exit();
                        }
                    } else if (confirmExit(event, new Alert(Alert.AlertType.WARNING,
                            "Are you sure you want to exit this application?",
                            ButtonType.OK, ButtonType.CANCEL).showAndWait())) {
                        Platform.exit();
                    }
                } else if (getTabPane().getTabs().size() > 1) {
                    if (isDirty(event)) confirmExit(event, new Alert(Alert.AlertType.WARNING,
                            "You have unsaved changes.\nAre you sure you want to close this tab?",
                            ButtonType.OK, ButtonType.CANCEL).showAndWait());
                }
            }
        });

        export.addEventHandler(MouseEvent.MOUSE_CLICKED, this::toolbarBtnClicked);
        pointer.addEventHandler(MouseEvent.MOUSE_CLICKED, this::toolbarBtnClicked);
        pen.addEventHandler(MouseEvent.MOUSE_CLICKED, this::toolbarBtnClicked);
        line_.addEventHandler(MouseEvent.MOUSE_CLICKED, this::toolbarBtnClicked);
        rect.addEventHandler(MouseEvent.MOUSE_CLICKED, this::toolbarBtnClicked);
        circle.addEventHandler(MouseEvent.MOUSE_CLICKED, this::toolbarBtnClicked);
        text_.addEventHandler(MouseEvent.MOUSE_CLICKED, this::toolbarBtnClicked);
        eraser.addEventHandler(MouseEvent.MOUSE_CLICKED, this::toolbarBtnClicked);
        undo.addEventHandler(MouseEvent.MOUSE_CLICKED, this::toolbarBtnClicked);
        redo.addEventHandler(MouseEvent.MOUSE_CLICKED, this::toolbarBtnClicked);
        clear.addEventHandler(MouseEvent.MOUSE_CLICKED, this::toolbarBtnClicked);
        addPage.addEventHandler(MouseEvent.MOUSE_CLICKED, this::toolbarBtnClicked);
        thicc.addEventHandler(MouseEvent.MOUSE_CLICKED, this::toolbarBtnClicked);
        thiccness.addEventHandler(MouseEvent.MOUSE_CLICKED, this::toolbarBtnClicked);
        colorPicker.addEventHandler(MouseEvent.MOUSE_CLICKED, this::toolbarBtnClicked);
        fontPick.addEventHandler(MouseEvent.MOUSE_CLICKED, this::toolbarBtnClicked);
        bg.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                if (bg.getItems().get((Integer) t1).equals("Blank")) {
                    drawPane.getChildren().remove(iv);
                } else {
                    if (!drawPane.getChildren().contains(iv)) {
                        Image view = new Image(Objects.requireNonNull(getClass().getResource("graph2.jpg"))
                                .toString(), drawPane.getWidth(), drawPane.getHeight(), false, false);
                        iv.setImage(view);
                        drawPane.getChildren().add(iv);
                    }
                }
            }
        });
        drawPane.addEventHandler(MouseEvent.MOUSE_PRESSED, this::drawPaneMousePressed);
        drawPane.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::drawPaneMouseDragged);
        drawPane.addEventHandler(MouseEvent.MOUSE_RELEASED, this::drawPaneMouseReleased);
        borderPane.addEventHandler(KeyEvent.KEY_PRESSED, this::borderPaneKeyPressed);

        thicc.setMax(30d);
        thicc.setMin(1d);
        thicc.setValue(3d);
        // colorPicker.setPrefWidth(35d);
        drawPane.setPrefHeight(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height);
        drawPane.setPrefWidth(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().width);
        drawPane.setCursor(Cursor.CROSSHAIR);
        drawPane.setId("drawPane");
        thiccness.setText(String.format("%.0f", thicc.getValue()));
        thicc.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                thiccness.setText(String.valueOf(t1.intValue()));
            }
        });
        colorPicker.setValue(color);
        colorPicker.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                color = colorPicker.getValue();
            }
        });
        bg.getItems().addAll("Blank", "Graph");
        bg.getSelectionModel().select(0);
        export.setGraphic(new FontIcon());
        export.setId("export");
        pointer.setGraphic(new FontIcon());
        pointer.setId("pointer");
        pen.setGraphic(new FontIcon());
        pen.setId("pen");
        line_.setGraphic(new FontIcon());
        line_.setId("line");
        rect.setGraphic(new FontIcon());
        rect.setId("rect");
        circle.setGraphic(new FontIcon());
        circle.setId("circle");
        text_.setGraphic(new FontIcon());
        text_.setId("text");
        eraser.setGraphic(new FontIcon());
        eraser.setId("eraser");
        undo.setGraphic(new FontIcon());
        undo.setId("undo");
        redo.setGraphic(new FontIcon());
        redo.setId("redo");
        clear.setGraphic(new FontIcon());
        clear.setId("clear");
        fontPick.setGraphic(new FontIcon());
        fontPick.setId("fontPick");
        addPage.setGraphic(new FontIcon());
        addPage.setId("addPage");
        thicc.setTooltip(new Tooltip("Set line thickness."));
        colorPicker.setTooltip(new Tooltip("Set color of shape."));
        bg.setTooltip(new Tooltip("Set background."));
        fontPick.setTooltip(new Tooltip("Font settings."));
        addPage.setTooltip(new Tooltip("Add Page."));

        Event.fireEvent(pen, new MouseEvent(MouseEvent.MOUSE_CLICKED, 0,
                0, 0, 0, MouseButton.PRIMARY, 1, true,
                true, true, true, true,
                true, true, true, true,
                true, null));
        pen.setSelected(true);
        setEraserColor();

        toolBar.getItems().addAll(export, pointer, pen, line_, rect, circle, text_, eraser,
                undo, redo, clear, thicc, thiccness, colorPicker, bg, fontPick, addPage);
        borderPane.setTop(toolBar);
        borderPane.setCenter(new ScrollPane(drawPane));
        this.setContent(borderPane);
    }

    protected boolean isDirty(Event event) {
        return drawPane.getChildren().contains(iv) ?
                drawPane.getChildren().size() > 1 : drawPane.getChildren().size() > 0;
    }

    public boolean confirmExit(Event event, Optional<ButtonType> dialog) {
        if (dialog.isPresent()) {
            if (dialog.get() == ButtonType.CANCEL) {
                event.consume();
                return false;
            }
            return true;
        }
        return false;
    }

    enum Tools {
        PEN, LINE, RECT, CIRCLE, TEXT, ERASER, POINTER
    }

    private void setTools(Tools tool) {
        this.tool = tool;
    }

    private Tools getTools() {
        return tool;
    }

    public void toolbarBtnClicked(MouseEvent mouseEvent) {
        List<ToggleButton> toolbarBtns = Arrays.asList(pen, line_, rect, circle, text_, eraser, pointer);
        for (int i = 0; i < toolbarBtns.size(); i++) {
            ToggleButton button = toolbarBtns.get(i);
            if (!(mouseEvent.getSource().equals(button))) {
                button.setSelected(false);
            } else {
                hist.add(button);
                setTools(toolsList.get(i));
            }
        }
        if (undo.equals(mouseEvent.getSource())) {
            undo();
        }
        if (redo.equals(mouseEvent.getSource())) {
            redo();
        }
        if (fontPick.equals(mouseEvent.getSource())) {
            FontSelectorDialog fsd = new FontSelectorDialog(new Font(Font.getDefault().getName(), 16));
            Optional<Font> response = fsd.showAndWait();
            response.ifPresent(font -> defaultFont = font);
            toolbarBtns.stream().filter(btn -> btn == hist.get(hist.size() - 1)).forEach(btn -> btn.setSelected(true));
        }
        if (colorPicker.equals(mouseEvent.getSource())) {
            toolbarBtns.stream().filter(btn -> btn == hist.get(hist.size() - 1)).forEach(btn -> btn.setSelected(true));
        }
        if (clear.equals(mouseEvent.getSource())) {
            drawPane.getChildren().removeIf(child -> !child.equals(iv));
            //drawPane.getChildren().clear();
            toolbarBtns.stream().filter(btn -> btn == hist.get(hist.size() - 1)).forEach(btn -> btn.setSelected(true));
        }
        if (export.equals(mouseEvent.getSource())) {
            saveToFile();
            toolbarBtns.stream().filter(btn -> btn == hist.get(hist.size() - 1)).forEach(btn -> btn.setSelected(true));
        }
        if (addPage.equals(mouseEvent.getSource())) {
            Task<Void> sleeper = new Task<>() {
                @Override
                protected Void call() {
                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };
            sleeper.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent mouseEvent) {
                    WhiteBoardPage wbp = new WhiteBoardPage();
                    getTabPane().getTabs().add(wbp);
                    wbp.setText("Page " + counter++);
                }
            });
            new Thread(sleeper).start();
            toolbarBtns.stream().filter(btn -> btn == hist.get(hist.size() - 1)).forEach(btn -> btn.setSelected(true));
        }
        drawPane.setCursor(pointer.equals(mouseEvent.getSource()) ? Cursor.HAND : Cursor.CROSSHAIR);
    }

    public void drawPaneMousePressed(MouseEvent mouseEvent) {
        if (getTools() != null) {
            if (!mouseEvent.isPrimaryButtonDown()) return;
            switch (getTools()) {
                case PEN:
                case ERASER:
                    path = new Path();
                    path.getElements().add(new MoveTo(mouseEvent.getX(), mouseEvent.getY()));
                    drawPane.getChildren().add(path);
                    break;
                case LINE:
                    drawLine = new Line(mouseEvent.getX(), mouseEvent.getY(), mouseEvent.getX(), mouseEvent.getY());
                    drawPane.getChildren().add(drawLine);
                    break;
                case RECT:
                    final Point2D in_parent = drawPane.sceneToLocal(mouseEvent.getSceneX(), mouseEvent.getSceneY());
                    x0 = in_parent.getX();
                    y0 = in_parent.getY();
                    rectangle = new javafx.scene.shape.Rectangle();
                    rectangle.setX(x0);
                    rectangle.setY(y0);
                    rectangle.setWidth(1);
                    rectangle.setHeight(1);
                    rectangle.setStroke(color);
                    rectangle.setStrokeWidth(thicc.getValue());
                    rectangle.setFill(javafx.scene.paint.Color.TRANSPARENT);
                    drawPane.getChildren().add(rectangle);
                    mouseEvent.consume();
                    break;
                case TEXT:
                    final Point2D parent = drawPane.sceneToLocal(mouseEvent.getSceneX(), mouseEvent.getSceneY());
                    x0 = parent.getX();
                    y0 = parent.getY();
                    addText = new TextArea();
                    rectangle = new Rectangle();
                    rectangle.setX(x0);
                    rectangle.setY(y0);
                    rectangle.setWidth(1);
                    rectangle.setHeight(1);
                    addText.setPrefWidth(rectangle.getWidth());
                    addText.setPrefHeight(rectangle.getHeight());
                    //System.out.println(defaultFont.getName());
                    //System.out.println(addText.getStyle());
                    rectangle.setStroke(color);
                    rectangle.setFill(javafx.scene.paint.Color.TRANSPARENT);
                    stackPane = new StackPane();
                    stackPane.setLayoutX(rectangle.getX());
                    stackPane.setLayoutY(rectangle.getY());

                    stackPane.getChildren().addAll(rectangle, addText);
                    drawPane.getChildren().add(stackPane);
                    //drawPane.getChildren().stream().flatMap(node -> ((StackPane) node).getChildren().stream()).forEach(System.out::println);
                    mouseEvent.consume();
                    break;
                case CIRCLE:
                    ellipse = new Ellipse();
                    ellipse.setFill(Color.TRANSPARENT);
                    ellipse.setStroke(color);
                    ellipse.setStrokeWidth(thicc.getValue());
                    startingPosX = mouseEvent.getX();
                    startingPosY = mouseEvent.getY();
                    ellipse.setCenterX(startingPosX);
                    ellipse.setCenterY(startingPosY);
                    ellipse.setRadiusX(0);
                    ellipse.setRadiusY(0);
                    drawPane.getChildren().add(ellipse);
                    mouseEvent.consume();
                    break;
            }
        }
    }

    public void drawPaneMouseDragged(MouseEvent mouseEvent) {
        if (getTools() != null) {
            if (!mouseEvent.isPrimaryButtonDown()) return;
            switch (getTools()) {
                case PEN:
                    path.setStroke(color);
                    path.setStrokeWidth(thicc.getValue());
                    path.setStrokeLineJoin(StrokeLineJoin.ROUND);
                    path.getElements().add(new LineTo(mouseEvent.getX(), mouseEvent.getY()));
                    break;
                case ERASER:
                    path.setStroke(eraserColor);
                    path.setStrokeWidth(thicc.getValue());
                    path.setStrokeLineJoin(StrokeLineJoin.ROUND);
                    path.getElements().add(new LineTo(mouseEvent.getX(), mouseEvent.getY()));
                    break;
                case LINE:
                    if (drawLine == null) return;
                    drawLine.setEndX(mouseEvent.getX());
                    drawLine.setEndY(mouseEvent.getY());
                    drawLine.setStroke(color);
                    drawLine.setStrokeWidth(thicc.getValue());
                    double mx = Math.max(drawLine.getStartX(), drawLine.getEndX());
                    double my = Math.max(drawLine.getStartY(), drawLine.getEndY());
                    if (mx > drawPane.getMinWidth()) drawPane.setMinWidth(mx);
                    if (my > drawPane.getMinHeight()) drawPane.setMinHeight(my);
                    break;
                case RECT:
                    final Point2D parent = drawPane.sceneToLocal(mouseEvent.getSceneX(), mouseEvent.getSceneY());
                    x1 = parent.getX();
                    y1 = parent.getY();
                    rectangle.setX(Math.min(x0, x1));
                    rectangle.setY(Math.min(y0, y1));
                    rectangle.setWidth(Math.abs(x1 - x0));
                    rectangle.setHeight(Math.abs(y1 - y0));
                    break;
                case TEXT:
                    final Point2D in_parent = drawPane.sceneToLocal(mouseEvent.getSceneX(), mouseEvent.getSceneY());
                    x1 = in_parent.getX();
                    y1 = in_parent.getY();
                    rectangle.setX(Math.min(x0, x1));
                    rectangle.setY(Math.min(y0, y1));
                    stackPane.setLayoutX(rectangle.getX());
                    stackPane.setLayoutY(rectangle.getY());
                    addText.setLayoutX(rectangle.getX());
                    addText.setLayoutY(rectangle.getY());
                    rectangle.setWidth(Math.abs(x1 - x0));
                    rectangle.setHeight(Math.abs(y1 - y0));
                    addText.setPrefWidth(rectangle.getWidth());
                    addText.setPrefHeight(rectangle.getHeight());
                    addText.setStyle("-fx-control-inner-background: " +
                            String.format("rgba(%d,%d,%d,%.1f)",
                                    (int) (eraserColor.getRed() * 255),
                                    (int) (eraserColor.getGreen() * 255),
                                    (int) (eraserColor.getBlue() * 255), 0.2f) +
                            "; -fx-text-fill: " +
                            String.format("#%02X%02X%02X;",
                                    (int) (color.getRed() * 255),
                                    (int) (color.getGreen() * 255),
                                    (int) (color.getBlue() * 255)));
                    //System.out.println(addText.getStyle());
                    break;
                case CIRCLE:
                    ellipse.setCenterX((mouseEvent.getX() + startingPosX) / 2);
                    ellipse.setCenterY((mouseEvent.getY() + startingPosY) / 2);
                    ellipse.setRadiusX(Math.abs((mouseEvent.getX() - startingPosX) / 2));
                    ellipse.setRadiusY(Math.abs((mouseEvent.getY() - startingPosY) / 2));
                    break;
            }
        }
    }

    public void drawPaneMouseReleased(MouseEvent mouseEvent) {
        if (getTools() != null) {
            switch (getTools()) {
                case PEN:
                case ERASER:
                    path = null;
                    break;
                case LINE:
                    drawLine = null;
                    break;
                case RECT:
                    rectangle = null;
                    break;
                case TEXT:
                    addText.setFont(defaultFont != null ? defaultFont : new Font(Font.getDefault().getName(), 16));
                    rectangle = null;
                    break;
                case CIRCLE:
                    ellipse = null;
                    break;
            }
        }
    }

    public void borderPaneKeyPressed(KeyEvent keyEvent) {
        switch (keyEvent.getCode()) {
            case Y:
                if (keyEvent.isControlDown()) {
                    redo();
                }
                break;
            case Z:
                if (keyEvent.isShiftDown() && keyEvent.isControlDown()) {
                    redo();
                } else if (keyEvent.isControlDown()) {
                    undo();
                }
                break;
        }
    }

    private void undo() {
        List<ToggleButton> toolbarBtns = Arrays.asList(pen, line_, rect, circle, text_, eraser, pointer);
        if (drawPane.getChildren().size() > 0) {
            last.push(drawPane.getChildren().remove(drawPane.getChildren().size() - 1));
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
        toolbarBtns.stream().filter(btn -> Objects.equals(btn, hist.get(hist.size() - 1)))
                .forEach(btn -> btn.setSelected(true));
    }

    private void redo() {
        List<ToggleButton> toolbarBtns = Arrays.asList(pen, line_, rect, circle, text_, eraser, pointer);
        if (last.size() > 0) {
            drawPane.getChildren().add(last.pop());
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
        toolbarBtns.stream().filter(btn -> Objects.equals(btn, hist.get(hist.size() - 1)))
                .forEach(btn -> btn.setSelected(true));
    }

    private void saveToFile() {
        FileChooser fileChooser = new FileChooser();
        //Set extension filter
        fileChooser.getExtensionFilters().add(new FileChooser
                .ExtensionFilter("PNG files (*.png)", "*.png"));
        //Prompt user to select a file
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                //Pad the capture area
                WritableImage writableImage = new WritableImage((int) drawPane.getWidth() + 20,
                        (int) drawPane.getHeight() + 20);
                drawPane.snapshot(null, writableImage);
                RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                //Write the snapshot to the chosen file
                ImageIO.write(renderedImage, "png", file);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    void setEraserColor() {
        Task<Void> sleeper = new Task<>() {
            @Override
            protected Void call() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        sleeper.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent mouseEvent) {
                Bounds boundsInScene = drawPane.localToScene(drawPane.getBoundsInLocal());
                eraserColor = new Robot().getPixelColor(boundsInScene.getCenterX(), boundsInScene.getCenterY());
                //System.out.printf("%f, %f%n", drawPane.getWidth(), drawPane.getHeight());
                //colorPicker.setValue(eraserColor);
            }
        });
        new Thread(sleeper).start();
    }
}
