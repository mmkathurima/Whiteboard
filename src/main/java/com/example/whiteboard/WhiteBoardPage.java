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

public class WhiteBoardPage extends Tab {
    BorderPane borderPane = new BorderPane();
    Pane drawPane = new Pane();
    ToolBar toolBar = new ToolBar();
    private final ToggleButton pen = new ToggleButton("Pen");
    private final ToggleButton line_ = new ToggleButton("Line");
    private final ToggleButton rect = new ToggleButton("Rectangle");
    private final ToggleButton circle = new ToggleButton("Circle");
    private final ToggleButton text_ = new ToggleButton("Text");
    private final ToggleButton eraser = new ToggleButton("Eraser");
    private final ToggleButton pointer = new ToggleButton("Pointer");
    private final ToggleButton arrow = new ToggleButton("Arrow");
    private final Button undo = new Button("Undo");
    private final Button redo = new Button("Redo");
    private final Button clear = new Button("Clear");
    private final Button fontPick = new Button("Fonts");
    private final Button export = new Button("Save As");
    private final Button addPage = new Button();
    private final Text thiccness = new Text();
    private final ColorPicker colorPicker = new ColorPicker();
    private final Slider thicc = new Slider();
    private final ChoiceBox<String> bg = new ChoiceBox<String>();
    private Font defaultFont;
    private Color color = Color.BLACK;
    private Color eraserColor = null;
    private Line drawLine;
    private Path path;
    private Rectangle rectangle;
    private Ellipse ellipse;
    private WhiteBoardPage.Tools tool;
    private final Stack<Node> last = new Stack<Node>();
    private final List<WhiteBoardPage.Tools> toolsList = Arrays.asList(WhiteBoardPage.Tools.values());
    private final List<ButtonBase> hist = new ArrayList<ButtonBase>();
    private TextArea addText;
    private StackPane stackPane;
    ImageView iv = new ImageView();
    private double x0;
    private double y0;
    private double x1;
    private double y1;
    private double startingPosX;
    private double startingPosY;
    private double endX;
    private double endY;
    private static int counter = 2;

    public WhiteBoardPage() {
        super();
        this.setOnCloseRequest(new EventHandler<Event>() {
                    @Override
                    public void handle(Event event) {
                        if (WhiteBoardPage.this.getTabPane().getTabs().size() == 1) {
                            if (WhiteBoardPage.this.isDirty(event)) {
                                if (WhiteBoardPage.this.confirmExit(
                                        event,
                                        new Alert(
                                                Alert.AlertType.WARNING,
                                                "You have unsaved changes.\n" +
                                                        "Are you sure you want to exit this application?",
                                                ButtonType.OK,
                                                ButtonType.CANCEL
                                        ).showAndWait())) {
                                    Platform.exit();
                                }
                            } else if (WhiteBoardPage.this.confirmExit(
                                    event,
                                    new Alert(Alert.AlertType.WARNING, "Are you sure you want to exit this application?", ButtonType.OK, ButtonType.CANCEL).showAndWait()
                            )) {
                                Platform.exit();
                            }
                        } else if (WhiteBoardPage.this.getTabPane().getTabs().size() > 1 && WhiteBoardPage.this.isDirty(event)) {
                            WhiteBoardPage.this.confirmExit(
                                    event,
                                    new Alert(
                                            Alert.AlertType.WARNING, "You have unsaved changes.\nAre you sure you want to close this tab?", ButtonType.OK, ButtonType.CANCEL
                                    )
                                            .showAndWait()
                            );
                        }

                    }
                }
        );
        this.export.addEventHandler(MouseEvent.MOUSE_CLICKED, this::toolbarBtnClicked);
        this.pointer.addEventHandler(MouseEvent.MOUSE_CLICKED, this::toolbarBtnClicked);
        this.pen.addEventHandler(MouseEvent.MOUSE_CLICKED, this::toolbarBtnClicked);
        this.line_.addEventHandler(MouseEvent.MOUSE_CLICKED, this::toolbarBtnClicked);
        this.rect.addEventHandler(MouseEvent.MOUSE_CLICKED, this::toolbarBtnClicked);
        this.circle.addEventHandler(MouseEvent.MOUSE_CLICKED, this::toolbarBtnClicked);
        this.text_.addEventHandler(MouseEvent.MOUSE_CLICKED, this::toolbarBtnClicked);
        this.eraser.addEventHandler(MouseEvent.MOUSE_CLICKED, this::toolbarBtnClicked);
        this.undo.addEventHandler(MouseEvent.MOUSE_CLICKED, this::toolbarBtnClicked);
        this.redo.addEventHandler(MouseEvent.MOUSE_CLICKED, this::toolbarBtnClicked);
        this.clear.addEventHandler(MouseEvent.MOUSE_CLICKED, this::toolbarBtnClicked);
        this.addPage.addEventHandler(MouseEvent.MOUSE_CLICKED, this::toolbarBtnClicked);
        this.thicc.addEventHandler(MouseEvent.MOUSE_CLICKED, this::toolbarBtnClicked);
        this.thiccness.addEventHandler(MouseEvent.MOUSE_CLICKED, this::toolbarBtnClicked);
        this.colorPicker.addEventHandler(MouseEvent.MOUSE_CLICKED, this::toolbarBtnClicked);
        this.fontPick.addEventHandler(MouseEvent.MOUSE_CLICKED, this::toolbarBtnClicked);
        this.bg
                .getSelectionModel()
                .selectedIndexProperty()
                .addListener(new ChangeListener<Number>() {
                            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                                if (WhiteBoardPage.this.bg.getItems().get((Integer) t1).equals("Blank")) {
                                    WhiteBoardPage.this.drawPane.getChildren().remove(WhiteBoardPage.this.iv);
                                } else if (!WhiteBoardPage.this.drawPane.getChildren().contains(WhiteBoardPage.this.iv)) {
                                    Image view = new Image(
                                            Objects.requireNonNull(this.getClass().getResource("graph2.jpg")).toString(),
                                            WhiteBoardPage.this.drawPane.getWidth(),
                                            WhiteBoardPage.this.drawPane.getHeight(),
                                            false,
                                            false
                                    );
                                    WhiteBoardPage.this.iv.setImage(view);
                                    WhiteBoardPage.this.drawPane.getChildren().add(WhiteBoardPage.this.iv);
                                }
                            }
                        }
                );
        this.drawPane.addEventHandler(MouseEvent.MOUSE_PRESSED, this::drawPaneMousePressed);
        this.drawPane.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::drawPaneMouseDragged);
        this.drawPane.addEventHandler(MouseEvent.MOUSE_RELEASED, this::drawPaneMouseReleased);
        this.borderPane.addEventHandler(KeyEvent.KEY_PRESSED, this::borderPaneKeyPressed);
        this.thicc.setMax(30.0);
        this.thicc.setMin(1.0);
        this.thicc.setValue(3.0);
        this.drawPane.setPrefHeight(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height);
        this.drawPane.setPrefWidth(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().width);
        this.drawPane.setCursor(Cursor.CROSSHAIR);
        this.drawPane.setId("drawPane");
        this.thiccness.setText(String.format("%.0f", this.thicc.getValue()));
        this.thicc.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                WhiteBoardPage.this.thiccness.setText(String.valueOf(t1.intValue()));
            }
        });
        this.colorPicker.setValue(this.color);
        this.colorPicker.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent actionEvent) {
                WhiteBoardPage.this.color = WhiteBoardPage.this.colorPicker.getValue();
            }
        });
        this.bg.getItems().addAll("Blank", "Graph");
        this.bg.getSelectionModel().select(0);
        this.export.setGraphic(new FontIcon());
        this.export.setId("export");
        this.pointer.setGraphic(new FontIcon());
        this.pointer.setId("pointer");
        this.arrow.setGraphic(new FontIcon());
        this.arrow.setId("arrow");
        this.pen.setGraphic(new FontIcon());
        this.pen.setId("pen");
        this.line_.setGraphic(new FontIcon());
        this.line_.setId("line");
        this.rect.setGraphic(new FontIcon());
        this.rect.setId("rect");
        this.circle.setGraphic(new FontIcon());
        this.circle.setId("circle");
        this.text_.setGraphic(new FontIcon());
        this.text_.setId("text");
        this.eraser.setGraphic(new FontIcon());
        this.eraser.setId("eraser");
        this.undo.setGraphic(new FontIcon());
        this.undo.setId("undo");
        this.redo.setGraphic(new FontIcon());
        this.redo.setId("redo");
        this.clear.setGraphic(new FontIcon());
        this.clear.setId("clear");
        this.fontPick.setGraphic(new FontIcon());
        this.fontPick.setId("fontPick");
        this.addPage.setGraphic(new FontIcon());
        this.addPage.setId("addPage");
        this.thicc.setTooltip(new Tooltip("Set line thickness."));
        this.colorPicker.setTooltip(new Tooltip("Set color of shape."));
        this.bg.setTooltip(new Tooltip("Set background."));
        this.fontPick.setTooltip(new Tooltip("Font settings."));
        this.addPage.setTooltip(new Tooltip("Add Page."));
        Event.fireEvent(
                this.pen,
                new MouseEvent(MouseEvent.MOUSE_CLICKED, 0.0, 0.0, 0.0, 0.0, MouseButton.PRIMARY, 1, true, true, true, true, true, true, true, true, true, true, null)
        );
        this.pen.setSelected(true);
        this.setEraserColor();
        this.toolBar
                .getItems()
                .addAll(
                        this.export,
                        this.pointer,
                        this.arrow,
                        this.pen,
                        this.line_,
                        this.rect,
                        this.circle,
                        this.text_,
                        this.eraser,
                        this.undo,
                        this.redo,
                        this.clear,
                        this.thicc,
                        this.thiccness,
                        this.colorPicker,
                        this.bg,
                        this.fontPick,
                        this.addPage
                );
        this.borderPane.setTop(this.toolBar);
        this.borderPane.setCenter(new ScrollPane(this.drawPane));
        this.setContent(this.borderPane);
    }

    protected boolean isDirty(Event event) {
        return this.drawPane.getChildren().contains(this.iv) ? this.drawPane.getChildren().size() > 1 : this.drawPane.getChildren().size() > 0;
    }

    public boolean confirmExit(Event event, Optional<ButtonType> dialog) {
        if (dialog.isPresent()) {
            if (dialog.get() == ButtonType.CANCEL) {
                event.consume();
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    private void setTools(WhiteBoardPage.Tools tool) {
        this.tool = tool;
    }

    private WhiteBoardPage.Tools getTools() {
        return this.tool;
    }

    public void toolbarBtnClicked(MouseEvent mouseEvent) {
        List<ToggleButton> toolbarBtns = Arrays.asList(this.pen, this.line_, this.rect, this.circle, this.text_, this.eraser, this.pointer);

        for (int i = 0; i < toolbarBtns.size(); ++i) {
            ToggleButton button = toolbarBtns.get(i);
            if (!mouseEvent.getSource().equals(button)) {
                button.setSelected(false);
            } else {
                this.hist.add(button);
                this.setTools(this.toolsList.get(i));
            }
        }

        if (this.undo.equals(mouseEvent.getSource())) {
            this.undo();
        }

        if (this.redo.equals(mouseEvent.getSource())) {
            this.redo();
        }

        if (this.fontPick.equals(mouseEvent.getSource())) {
            FontSelectorDialog fsd = new FontSelectorDialog(new Font(Font.getDefault().getName(), 16.0));
            Optional<Font> response = fsd.showAndWait();
            response.ifPresent(font -> this.defaultFont = font);
            toolbarBtns.stream().filter(btn -> btn == this.hist.get(this.hist.size() - 1)).forEach(btn -> btn.setSelected(true));
        }

        if (this.colorPicker.equals(mouseEvent.getSource())) {
            toolbarBtns.stream().filter(btn -> btn == this.hist.get(this.hist.size() - 1)).forEach(btn -> btn.setSelected(true));
        }

        if (this.clear.equals(mouseEvent.getSource())) {
            this.drawPane.getChildren().removeIf(child -> !child.equals(this.iv));
            toolbarBtns.stream().filter(btn -> btn == this.hist.get(this.hist.size() - 1)).forEach(btn -> btn.setSelected(true));
        }

        if (this.export.equals(mouseEvent.getSource())) {
            this.saveToFile();
            toolbarBtns.stream().filter(btn -> btn == this.hist.get(this.hist.size() - 1)).forEach(btn -> btn.setSelected(true));
        }

        if (this.addPage.equals(mouseEvent.getSource())) {
            Task<Void> sleeper = new Task<Void>() {
                protected Void call() {
                    try {
                        Thread.sleep(30L);
                    } catch (InterruptedException var2) {
                        var2.printStackTrace();
                    }

                    return null;
                }
            };
            sleeper.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                public void handle(WorkerStateEvent mouseEvent) {
                    WhiteBoardPage wbp = new WhiteBoardPage();
                    WhiteBoardPage.this.getTabPane().getTabs().add(wbp);
                    WhiteBoardPage.this.getTabPane().getSelectionModel().select(wbp);
                    wbp.setText("Page " + WhiteBoardPage.counter++);
                }
            });
            new Thread(sleeper).start();
            toolbarBtns.stream().filter(btn -> btn == this.hist.get(this.hist.size() - 1)).forEach(btn -> btn.setSelected(true));
        }

        this.drawPane.setCursor(this.pointer.equals(mouseEvent.getSource()) ? Cursor.HAND : Cursor.CROSSHAIR);
    }

    public void drawPaneMousePressed(MouseEvent mouseEvent) {
        if (this.getTools() != null) {
            if (!mouseEvent.isPrimaryButtonDown()) {
                return;
            }

            Point2D in_parent = this.drawPane.sceneToLocal(mouseEvent.getSceneX(), mouseEvent.getSceneY());
            this.x0 = in_parent.getX();
            this.y0 = in_parent.getY();
            switch (this.getTools()) {
                case PEN:
                case ERASER:
                    this.path = new Path();
                    this.path.getElements().add(new MoveTo(mouseEvent.getX(), mouseEvent.getY()));
                    this.drawPane.getChildren().add(this.path);
                    break;
                case LINE:
                    this.drawLine = new Line(mouseEvent.getX(), mouseEvent.getY(), mouseEvent.getX(), mouseEvent.getY());
                    this.drawPane.getChildren().add(this.drawLine);
                    this.path = new Path();
                    this.drawPane.getChildren().add(this.path);
                    break;
                case RECT:
                    this.rectangle = new Rectangle();
                    this.rectangle.setX(this.x0);
                    this.rectangle.setY(this.y0);
                    this.rectangle.setWidth(1.0);
                    this.rectangle.setHeight(1.0);
                    this.rectangle.setStroke(this.color);
                    this.rectangle.setStrokeWidth(this.thicc.getValue());
                    this.rectangle.setFill(Color.TRANSPARENT);
                    this.drawPane.getChildren().add(this.rectangle);
                    mouseEvent.consume();
                    break;
                case TEXT:
                    this.addText = new TextArea();
                    this.rectangle = new Rectangle();
                    this.rectangle.setX(this.x0);
                    this.rectangle.setY(this.y0);
                    this.rectangle.setWidth(1.0);
                    this.rectangle.setHeight(1.0);
                    this.addText.setPrefWidth(this.rectangle.getWidth());
                    this.addText.setPrefHeight(this.rectangle.getHeight());
                    this.rectangle.setStroke(this.color);
                    this.rectangle.setFill(Color.TRANSPARENT);
                    this.stackPane = new StackPane();
                    this.stackPane.setLayoutX(this.rectangle.getX());
                    this.stackPane.setLayoutY(this.rectangle.getY());
                    this.stackPane.getChildren().addAll(this.rectangle, this.addText);
                    this.drawPane.getChildren().add(this.stackPane);
                    mouseEvent.consume();
                    break;
                case CIRCLE:
                    this.ellipse = new Ellipse();
                    this.ellipse.setFill(Color.TRANSPARENT);
                    this.ellipse.setStroke(this.color);
                    this.ellipse.setStrokeWidth(this.thicc.getValue());
                    this.startingPosX = mouseEvent.getX();
                    this.startingPosY = mouseEvent.getY();
                    this.ellipse.setCenterX(this.startingPosX);
                    this.ellipse.setCenterY(this.startingPosY);
                    this.ellipse.setRadiusX(0.0);
                    this.ellipse.setRadiusY(0.0);
                    this.drawPane.getChildren().add(this.ellipse);
                    mouseEvent.consume();
            }
        }

    }

    public void drawPaneMouseDragged(MouseEvent mouseEvent) {
        if (this.getTools() != null) {
            if (!mouseEvent.isPrimaryButtonDown()) {
                return;
            }

            switch (this.getTools()) {
                case PEN:
                    this.path.setStroke(this.color);
                    this.path.setStrokeWidth(this.thicc.getValue());
                    this.path.setStrokeLineJoin(StrokeLineJoin.ROUND);
                    this.path.getElements().add(new LineTo(mouseEvent.getX(), mouseEvent.getY()));
                    break;
                case ERASER:
                    this.path.setStroke(this.eraserColor);
                    this.path.setStrokeWidth(this.thicc.getValue());
                    this.path.setStrokeLineJoin(StrokeLineJoin.ROUND);
                    this.path.getElements().add(new LineTo(mouseEvent.getX(), mouseEvent.getY()));
                    break;
                case LINE:
                    if (this.drawLine == null) {
                        return;
                    }

                    this.drawLine.setEndX(mouseEvent.getX());
                    this.drawLine.setEndY(mouseEvent.getY());
                    this.drawLine.setStroke(this.color);
                    this.drawLine.setStrokeWidth(this.thicc.getValue());
                    double mx = Math.max(this.drawLine.getStartX(), this.drawLine.getEndX());
                    double my = Math.max(this.drawLine.getStartY(), this.drawLine.getEndY());
                    if (mx > this.drawPane.getMinWidth()) {
                        this.drawPane.setMinWidth(mx);
                    }

                    if (my > this.drawPane.getMinHeight()) {
                        this.drawPane.setMinHeight(my);
                    }
                    break;
                case RECT:
                    Point2D parent = this.drawPane.sceneToLocal(mouseEvent.getSceneX(), mouseEvent.getSceneY());
                    this.x1 = parent.getX();
                    this.y1 = parent.getY();
                    this.rectangle.setX(Math.min(this.x0, this.x1));
                    this.rectangle.setY(Math.min(this.y0, this.y1));
                    this.rectangle.setWidth(Math.abs(this.x1 - this.x0));
                    this.rectangle.setHeight(Math.abs(this.y1 - this.y0));
                    break;
                case TEXT:
                    Point2D in_parent = this.drawPane.sceneToLocal(mouseEvent.getSceneX(), mouseEvent.getSceneY());
                    this.x1 = in_parent.getX();
                    this.y1 = in_parent.getY();
                    this.rectangle.setX(Math.min(this.x0, this.x1));
                    this.rectangle.setY(Math.min(this.y0, this.y1));
                    this.stackPane.setLayoutX(this.rectangle.getX());
                    this.stackPane.setLayoutY(this.rectangle.getY());
                    this.addText.setLayoutX(this.rectangle.getX());
                    this.addText.setLayoutY(this.rectangle.getY());
                    this.rectangle.setWidth(Math.abs(this.x1 - this.x0));
                    this.rectangle.setHeight(Math.abs(this.y1 - this.y0));
                    this.addText.setPrefWidth(this.rectangle.getWidth());
                    this.addText.setPrefHeight(this.rectangle.getHeight());
                    this.addText
                            .setStyle(
                                    "-fx-control-inner-background: "
                                            + String.format(
                                            "rgba(%d,%d,%d,%.1f)",
                                            (int) (this.eraserColor.getRed() * 255.0),
                                            (int) (this.eraserColor.getGreen() * 255.0),
                                            (int) (this.eraserColor.getBlue() * 255.0),
                                            0.2F
                                    )
                                            + "; -fx-text-fill: "
                                            + String.format(
                                            "#%02X%02X%02X;", (int) (this.color.getRed() * 255.0), (int) (this.color.getGreen() * 255.0), (int) (this.color.getBlue() * 255.0)
                                    )
                            );
                    break;
                case CIRCLE:
                    this.ellipse.setCenterX((mouseEvent.getX() + this.startingPosX) / 2.0);
                    this.ellipse.setCenterY((mouseEvent.getY() + this.startingPosY) / 2.0);
                    this.ellipse.setRadiusX(Math.abs((mouseEvent.getX() - this.startingPosX) / 2.0));
                    this.ellipse.setRadiusY(Math.abs((mouseEvent.getY() - this.startingPosY) / 2.0));
            }
        }

    }

    public void drawPaneMouseReleased(MouseEvent mouseEvent) {
        if (this.getTools() != null) {
            switch (this.getTools()) {
                case PEN:
                    this.addArrowHead(mouseEvent, false);
                case ERASER:
                    this.path = null;
                    break;
                case LINE:
                    this.addArrowHead(mouseEvent, true);
                    this.drawLine = null;
                    break;
                case RECT:
                    this.rectangle = null;
                    break;
                case TEXT:
                    this.addText.setFont(this.defaultFont != null ? this.defaultFont : new Font(Font.getDefault().getName(), 16.0));
                    this.rectangle = null;
                    break;
                case CIRCLE:
                    this.ellipse = null;
            }
        }

    }

    private void addArrowHead(MouseEvent mouseEvent, boolean line) {
        if (this.arrow.isSelected()) {
            double arrowHeadSize = 13.0;
            this.endX = mouseEvent.getX();
            this.endY = mouseEvent.getY();
            double angle = Math.atan2(this.endY - this.y0, this.endX - this.x0) - Math.PI / 2;
            double sin = Math.sin(angle);
            double cos = Math.cos(angle);
            double x1 = (-0.5 * cos + Math.sqrt(3.0) / 2.0 * sin) * arrowHeadSize + this.endX;
            double y1 = (-0.5 * sin - Math.sqrt(3.0) / 2.0 * cos) * arrowHeadSize + this.endY;
            double x2 = (0.5 * cos + Math.sqrt(3.0) / 2.0 * sin) * arrowHeadSize + this.endX;
            double y2 = (0.5 * sin - Math.sqrt(3.0) / 2.0 * cos) * arrowHeadSize + this.endY;
            if (line) {
                this.path.setStroke(this.color);
                this.path.setStrokeWidth(this.thicc.getValue());
                this.path.setStrokeLineJoin(StrokeLineJoin.ROUND);
                this.path.getElements().add(new MoveTo(mouseEvent.getX(), mouseEvent.getY()));
            }

            this.path.getElements().add(new LineTo(x1, y1));
            this.path.getElements().add(new LineTo(x2, y2));
            this.path.getElements().add(new LineTo(this.endX, this.endY));
        }

    }

    public void borderPaneKeyPressed(KeyEvent keyEvent) {
        switch (keyEvent.getCode()) {
            case Y:
                if (keyEvent.isControlDown()) {
                    this.redo();
                }
                break;
            case Z:
                if (keyEvent.isShiftDown() && keyEvent.isControlDown()) {
                    this.redo();
                } else if (keyEvent.isControlDown()) {
                    this.undo();
                }
        }

    }

    private void undo() {
        List<ToggleButton> toolbarBtns = Arrays.asList(this.pen, this.line_, this.rect, this.circle, this.text_, this.eraser, this.pointer);
        if (this.drawPane.getChildren().size() > 0) {
            this.last.push(this.drawPane.getChildren().remove(this.drawPane.getChildren().size() - 1));
        } else {
            Toolkit.getDefaultToolkit().beep();
        }

        toolbarBtns.stream().filter(btn -> Objects.equals(btn, this.hist.get(this.hist.size() - 1))).forEach(btn -> btn.setSelected(true));
    }

    private void redo() {
        List<ToggleButton> toolbarBtns = Arrays.asList(this.pen, this.line_, this.rect, this.circle, this.text_, this.eraser, this.pointer);
        if (this.last.size() > 0) {
            this.drawPane.getChildren().add(this.last.pop());
        } else {
            Toolkit.getDefaultToolkit().beep();
        }

        toolbarBtns.stream().filter(btn -> Objects.equals(btn, this.hist.get(this.hist.size() - 1))).forEach(btn -> btn.setSelected(true));
    }

    private void saveToFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png"));
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                WritableImage writableImage = new WritableImage((int) this.drawPane.getWidth() + 20, (int) this.drawPane.getHeight() + 20);
                this.drawPane.snapshot(null, writableImage);
                RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                ImageIO.write(renderedImage, "png", file);
            } catch (IOException var5) {
                var5.printStackTrace();
            }
        }

    }

    void setEraserColor() {
        Task<Void> sleeper = new Task<Void>() {
            protected Void call() {
                try {
                    Thread.sleep(2000L);
                } catch (InterruptedException var2) {
                    var2.printStackTrace();
                }

                return null;
            }
        };
        sleeper.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            public void handle(WorkerStateEvent mouseEvent) {
                Bounds boundsInScene = WhiteBoardPage.this.drawPane.localToScene(WhiteBoardPage.this.drawPane.getBoundsInLocal());
                WhiteBoardPage.this.eraserColor = new Robot().getPixelColor(boundsInScene.getCenterX(), boundsInScene.getCenterY());
            }
        });
        new Thread(sleeper).start();
    }

    static enum Tools {
        PEN,
        LINE,
        RECT,
        CIRCLE,
        TEXT,
        ERASER,
        POINTER;

        private Tools() {
        }
    }
}
