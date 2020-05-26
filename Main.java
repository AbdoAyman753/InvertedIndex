package sample;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.Vector;



public class Main extends Application {
    VBox mainBox;
    HBox topBox;
    HBox bottomBox;

    public static TableView<Word> Table;
    public static TableView<Document> DocTable;
    //Old Data Structure
    public static Vector<Word> wordVector =new Vector<Word>();
    public static Vector<Vector<Document>> documents=new Vector<Vector<Document>>();
    //New Data Structure
    public static HashFunc<String,Integer> WordsMap = new HashFunc<>();
    public static HashFunc<Integer,HashFunc<String,Document>> DocsMap = new HashFunc<>();
    //Number Of Different Words
    public static int WordCounter=-1;
    public static int FileCount ;
    //Word Counted Per File (Same Word)
    public static int FileWordCount;
    public static String TheWord;

    Service<Void> background;
    Service<Void> foreground;
    boolean flag =true;

    Button BrowseButton;
    Button SearchButton;
    Button BuildButton;

    TextField directory;
    TextField WordField;
    public static Label status;

    @Override
    public void start(Stage primaryStage) throws Exception{
//******************************* Window GUI ****************************************
        Stage window=primaryStage;
        window.setTitle("Inverted Index");
        window.setMinWidth(650);
        window.setMinHeight(580);

        mainBox = new VBox(10);
        mainBox.setPadding(new Insets(10,10,10,10));
        topBox = new HBox(50);
        topBox.setPadding(new Insets(10,10,10,10));
        bottomBox = new HBox(150);
        bottomBox.setPadding(new Insets(10,10,10,10));

        status = new Label("Waiting For Input");

        status.setAlignment(Pos.CENTER_RIGHT);
        status.setPadding(new Insets(10,10,10,10));


        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File("src"));

        SearchButton = new Button("Search");
        BrowseButton = new Button("Browse");
        BuildButton = new Button("Build");
        directory = new TextField();
        WordField = new TextField();
        WordField.setPromptText("Search Word");
        directory.setEditable(false);
        directory.setMinWidth(500);
        directory.setPromptText("Select File...");


//******************************* Table GUI *****************************************
        //Word Column
        TableColumn<Word, String> WordColumn = new TableColumn<>("Word");
        WordColumn.setMinWidth(200);
        WordColumn.setCellValueFactory(new PropertyValueFactory<>("Name"));

        //Word Column
        TableColumn<Word, Integer> TotalColumn = new TableColumn<>("Total Documents No.");
        TotalColumn.setMinWidth(250);
        TotalColumn.setCellValueFactory(new PropertyValueFactory<>("DocNum"));

        //Documents column
        TableColumn<Word,String> DocColumn = new TableColumn<>("Documents");
        DocColumn.setMinWidth(300);
        DocColumn.setCellValueFactory(new PropertyValueFactory<>("Documents"));


        //Adding Elements To Table
        Table = new TableView<>();
        Table.setItems(getWord());
        Table.getColumns().addAll(WordColumn,TotalColumn,DocColumn);

//******************************* Documents Table GUI *****************************************
        //Document ID Column
        TableColumn<Document, String> DocIDColumn = new TableColumn<>("Document");
        DocIDColumn.setMinWidth(200);
        DocIDColumn.setCellValueFactory(new PropertyValueFactory<>("DocID"));

        //Document Content
        TableColumn<Document, String> ContentColumn = new TableColumn<>("Text");
        ContentColumn.setMinWidth(300);
        ContentColumn.setCellValueFactory(new PropertyValueFactory<>("DocContent"));



        //Adding Elements To Table
        DocTable = new TableView<>();
        DocTable.setItems(getDocs());
        DocTable.getColumns().addAll(DocIDColumn,ContentColumn);
        Scene neu=new Scene(DocTable);
//**************************** Handling Requests **************************************
        BrowseButton.setOnAction(e -> {
            File selectedDirectory = directoryChooser.showDialog(window);
            directoryChooser.setTitle("Select directory");
            directoryChooser.setInitialDirectory(new File("C:\\"));

            directory.setText(selectedDirectory.getAbsolutePath());
        });


        BuildButton.setOnAction(e -> {

            background = new Service<Void>() {
                @Override
                protected Task<Void> createTask() {
                    return new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            listAllFiles(directory.getText());
                            return null;
                        }
                    };
                }
            };
            foreground = new Service<Void>() {
                @Override
                protected Task<Void> createTask() {
                    return new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            while(flag){
                                updateMessage("Read "+FileCount+" Files.");
                            }
                            return null;
                        }
                    };
                }
            };
            background.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    status.textProperty().unbind();
                    status.setText("Reading Files,Done.");
                }
            });

            status.textProperty().bind(foreground.messageProperty());
            background.restart();
            foreground.restart();
            //status.setText("Reading Files,Done.");
        });


        SearchButton.setOnAction(e->{
            status.setText("Searching For "+WordField.getText());
            TheWord=WordField.getText();
            //showing data in table
            try{Table.getItems().add(wordVector.elementAt(WordsMap.get(TheWord)));}
            catch (Exception o){
                status.setText("Word Can't be Found !!!");
            }        });

        Table.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                //show segments table on double click
                if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                    DocTable.getItems().clear();
                    Stage win=new Stage();
                    win.setTitle("Documents");
                    win.initModality(Modality.APPLICATION_MODAL);
                    int selectedIndexSize=documents.elementAt(WordsMap.get(Table.getSelectionModel().getSelectedItem().getName())).size();
                    win.setMinWidth(250);
                    for(int i=0; i<selectedIndexSize; i++){
                        DocTable.getItems()
                                .add(documents.elementAt(WordsMap.get(Table.getSelectionModel().getSelectedItem().getName())).elementAt(i));
                    }

                    DocTable.setPadding(new Insets(10, 10, 10, 10));
                    win.setScene(neu);
                    win.showAndWait();
                }
            }
        });
//**************************** Scene Building ***************************************
        mainBox.getChildren().addAll(topBox,Table,bottomBox);
        topBox.getChildren().addAll(directory,BrowseButton,BuildButton);
        bottomBox.getChildren().addAll(WordField,status,SearchButton);

        Scene scene = new Scene(mainBox);
        window.setScene(scene);
        window.show();

    }



    public static void main(java.lang.String[] args) {
        launch(args);
    }

    public ObservableList<Word> getWord(){
        ObservableList<Word> wordObservableList = FXCollections.observableArrayList();
        return wordObservableList;
    }
    public ObservableList<Document> getDocs(){
        ObservableList<Document> Docs = FXCollections.observableArrayList();
        return Docs;
    }

    public  void listAllFiles(java.lang.String path){
        //System.out.print("Reading Files");
        FileCount=0;
        FileWordCount=0;
        try(Stream<Path> paths = Files.walk(Paths.get(path))) {
            paths.forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    try {
                        readContent(filePath);
                        FileCount++;
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        flag=false;
    }
    public  void readContent(Path filePath) throws IOException{
        String Temp;
        String FileName=String.valueOf(filePath.getFileName());
        String[] words=null;  //Intialize the word Array
        FileReader fr = new FileReader(String.valueOf(filePath));  //Creation of File Reader object
        BufferedReader br = new BufferedReader(fr); //Creation of BufferedReader object
        String s;
        int count=0;   //Intialize the word to zero
        while((s=br.readLine())!=null)   //Reading Content from the file
        {
            Temp=s;
            words=s.split(" ");  //Split the word using space
            for (String word : words)
            {
                //New Word
                if(WordsMap.get(word) == null){
                    WordCounter++;
                    WordsMap.add(word, WordCounter);
                    wordVector.add(new Word(word));
                    documents.add(new Vector<>());
                    DocsMap.add(WordCounter,new HashFunc<String,Document>());
                }
                //New Document
                if(DocsMap.get(WordsMap.get(word)).get(FileName) == null){
                    DocsMap.get(WordsMap.get(word)).add(FileName , new Document(FileName,Temp));
                    wordVector.elementAt(WordsMap.get(word)).setDocuments(", "+FileName+"...");
                    wordVector.elementAt(WordsMap.get(word)).
                            setDocNum(wordVector.elementAt(WordsMap.get(word)).getDocNum()+1);
                    documents.elementAt(WordsMap.get(word)).add(new Document(FileName,Temp));
                }
            }
        }
        fr.close();
    }
}