package com.internshala.connect4;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
import org.omg.CORBA.PRIVATE_MEMBER;

import java.awt.*;
import java.net.URL;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Controller implements Initializable {

    private static final int COLUMNS =7;
    private static final int ROWS= 6;
    private static final int CIRCLE_DIAMETER = 80;
    private static final String discColor1 ="#24303E" ;
    private static final String discColor2 = "#4CAA88";

    private static  String PLAYER_ONE = "Player One";
    private static  String PLAYER_TWO = "Player Two";

    private boolean isPlayerOneTurn = true;

    private boolean isAllowedToInsert = true;

    private Disc[][] insertedDiscsArray = new Disc[ROWS][COLUMNS]; //for structural change

    @FXML
    public GridPane rootGridPane;

    @FXML
    public Pane insertedDiscPane;

    @FXML
    public Label playerNameLabel;


    @FXML
    public javafx.scene.control.TextField playerOneTextField;

    @FXML
    public TextField PlayerTwoTextField;

    @FXML
    public javafx.scene.control.Button setNamesButton;




    public void createPlayground(){

        Shape shape = createGameStructuralGrid();

        rootGridPane.add(shape,0,1);

        List<Rectangle> rectangleList = createClickableColumns();

        for(Rectangle rectangle :rectangleList){
            rootGridPane.add(rectangle,0,1);
        }

        // set names according to entered by user
        Platform.runLater(() -> setNamesButton.requestFocus());

        setNamesButton.setOnAction(event -> {
            PLAYER_ONE = playerOneTextField.getText();
            PLAYER_TWO = PlayerTwoTextField.getText();
            playerNameLabel.setText(isPlayerOneTurn? PLAYER_ONE : PLAYER_TWO);
        });

}

    private Shape createGameStructuralGrid(){

        Shape rectangleWithHoles = new Rectangle((COLUMNS+1) * CIRCLE_DIAMETER , (ROWS+1) *CIRCLE_DIAMETER);

        for(int row =0; row< ROWS; row++){
            for(int column =0; column < COLUMNS; column++){
                Circle circle = new Circle();
                circle.setRadius(CIRCLE_DIAMETER / 2);
                circle.setCenterX(CIRCLE_DIAMETER/2);
                circle.setCenterY(CIRCLE_DIAMETER/2);
                circle.setSmooth(true);

                circle.setTranslateX( column * (CIRCLE_DIAMETER+5) + CIRCLE_DIAMETER /4);
                circle.setTranslateY(row * (CIRCLE_DIAMETER+5) + CIRCLE_DIAMETER /4);

                rectangleWithHoles = Shape.subtract(rectangleWithHoles,circle);

            }
        }

        rectangleWithHoles.setFill(Color.WHITE);

        return rectangleWithHoles;
    }

    private List<Rectangle> createClickableColumns(){

        List<Rectangle> rectangleList = new ArrayList();
            for(int col = 0; col <COLUMNS; col++)
            {
                Rectangle rectangle = new Rectangle(CIRCLE_DIAMETER,(ROWS+1) *CIRCLE_DIAMETER);
                rectangle.setFill(Color.TRANSPARENT);

                rectangle.setTranslateX(col* (CIRCLE_DIAMETER+5)+CIRCLE_DIAMETER /4);

                rectangle.setOnMouseEntered(event -> rectangle.setFill(Color.valueOf("#eeeeee26")));
                rectangle.setOnMouseExited(event -> rectangle.setFill(Color.TRANSPARENT));

                final int column = col;
                rectangle.setOnMouseClicked(event -> {
                    if(isAllowedToInsert) {
                        isAllowedToInsert = false;
                        insertDisc(new Disc(isPlayerOneTurn), column);
                    }
                });
                rectangleList.add(rectangle);
            }
            return rectangleList;
    }

    private  void insertDisc(Disc disc, int column) {

        int row = ROWS - 1;

        while(row >= 0){
            if(getDiscIfPresent(row,column) == null)
                break;
            row--;
        }

        if(row < 0)
            return;
        insertedDiscsArray[row][column] = disc;
        insertedDiscPane.getChildren().add(disc);

        disc.setTranslateX( column * (CIRCLE_DIAMETER+5) + CIRCLE_DIAMETER /4);

         int currentRow =row;
        TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.5),disc);
        translateTransition.setToY( row * (CIRCLE_DIAMETER+5) + CIRCLE_DIAMETER /4);
        translateTransition.setOnFinished(event -> {

            isAllowedToInsert = true;

            if(gameEnded(currentRow, column)){
                gameOver();
                return;

            }
            isPlayerOneTurn = !isPlayerOneTurn;

            playerNameLabel.setText(isPlayerOneTurn ? PLAYER_ONE : PLAYER_TWO);

        });
        translateTransition.play();


    }

    private void gameOver() {
        String winner = isPlayerOneTurn ? PLAYER_ONE:  PLAYER_TWO;
        System.out.println("winner" + winner);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Connect Four");
        alert.setHeaderText("The Winner is " + winner);
        alert.setContentText("Want to Play Again?");

        ButtonType yesBtn = new ButtonType("Yes");
        ButtonType NoBtn =  new ButtonType("No, Exit");
        alert.getButtonTypes().setAll(yesBtn,NoBtn);

        Platform.runLater(()->{
            Optional<ButtonType> btnClicked = alert.showAndWait();

            if(btnClicked.isPresent() && btnClicked.get() == yesBtn){
                //reset
                resetGame();
            }
            else{
                Platform.exit();
                System.exit(0);
            }
        });
    }

    public void resetGame() {

        insertedDiscPane.getChildren().clear();

        for (int row =0; row < insertedDiscsArray.length; row++ ){
            for(int col = 0; col < insertedDiscsArray[row].length; col++){

                insertedDiscsArray[row][col] = null;
            }

        }
        isPlayerOneTurn = true;
        playerNameLabel.setText(PLAYER_ONE);

        createPlayground();


    }

    private boolean gameEnded(int row, int column) {

        //vertical point
        List<Point2D> verticalPoint = IntStream.rangeClosed(row-3,row+3) // range of row values
                .mapToObj( r -> new Point2D(r, column)) // return Point2D object
                .collect(Collectors.toList());

        //horizontal point
        List<Point2D> HorizontalPoint = IntStream.rangeClosed(column-3,column+3) // range of row values
                .mapToObj( c -> new Point2D(row, c)) // return Point2D object
                .collect(Collectors.toList());

        Point2D starPoint1 = new Point2D(row-3,column +3);
        List<Point2D> diagonal1Points = IntStream.rangeClosed(0,6)
                .mapToObj(i-> starPoint1.add(i,-i))
                .collect(Collectors.toList());

        Point2D starPoint2 = new Point2D(row-3,column - 3 );
        List<Point2D> diagonal2Points = IntStream.rangeClosed(0,6)
                .mapToObj(i-> starPoint2.add(i,i))
                .collect(Collectors.toList());

        boolean isEnded = checkCombinations(HorizontalPoint) || checkCombinations(verticalPoint)
                || checkCombinations(diagonal1Points) || checkCombinations(diagonal2Points);

        return isEnded;
    }

    private Disc getDiscIfPresent(int row, int column){

        if(row >= ROWS || row < 0 || column >= COLUMNS || column < 0)
            return null;
        return insertedDiscsArray[row][column];

    }
    private boolean checkCombinations(List<Point2D> Points) {

        int chain =0;
        for (Point2D point: Points) {

            int rowIndexForArray = (int) point.getX();
            int colIndexForArray = (int) point.getY();

            Disc disc = getDiscIfPresent(rowIndexForArray,colIndexForArray);

            if(disc!= null && disc.isPlayerOneMove == isPlayerOneTurn){

                chain++;
                if(chain == 4){
                    return true;
                }

            }else{
                chain =0;
            }
        }
        return false;
    }

    private static class Disc extends Circle{

        private final boolean isPlayerOneMove;
        public Disc(boolean isPlayerOneMove){

            this.isPlayerOneMove =isPlayerOneMove;
            setRadius(CIRCLE_DIAMETER/2);
            setFill(isPlayerOneMove ? Color.valueOf(discColor1) : Color.valueOf(discColor2));
            setCenterX(CIRCLE_DIAMETER/2);
            setCenterY(CIRCLE_DIAMETER/2);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
