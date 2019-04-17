import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * Created by HP xw8400
 * Author: Jacob
 * Date: 4/16/2019.
 */
public class ApplicationLauncher extends Application {

    private ArrayList<String> fileNames = new ArrayList<>();

    private String outputDirectory = "";
    private String inputFile = "";

    @Override
    public void start(Stage stage) {
        final DownloaderTask task = new DownloaderTask();

        final Label downloadLocation = new Label("Output: Application directory");
        final Label inputLocation = new Label("Input:");

        final Button inputSelector = new Button("Select input directory");
        inputSelector.setOnAction(action -> {

            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select an input file");
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files", "*.txt"));

            File selected = fileChooser.showOpenDialog(new Stage());

            if (selected != null) {
                inputFile = selected.getAbsolutePath() + System.getProperty("file.separator");
                inputLocation.textProperty().setValue("Input file: " + inputFile);

                fileNames.clear();
                try {
                    final FileReader reader = new FileReader(inputFile);
                    final BufferedReader fileBuff = new BufferedReader(reader);
                    String line;
                    while ((line = fileBuff.readLine()) != null) {
                        fileNames.add(line.trim());
                    }
                    fileBuff.close();
                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        final Button outputSelector = new Button("Select output directory");
        outputSelector.setOnAction(action -> {

            final DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            directoryChooser.setTitle("Select an output directory");

            File selected = directoryChooser.showDialog(stage);

            if (selected != null) {
                outputDirectory = selected.getAbsolutePath() + System.getProperty("file.separator");
                downloadLocation.textProperty().setValue("Download location: " + outputDirectory);
            }
        });

        final Button start = new Button("Start download");

        start.setOnAction(action -> {
            fileNames.forEach(link -> {
                task.start(link, outputDirectory);
            });
        });

        final VBox contentPane = new VBox(25, task.getContentPane(), start, new HBox(20, inputSelector, outputSelector), downloadLocation, inputLocation);
        contentPane.setPadding(new Insets(40));
        contentPane.setAlignment(Pos.CENTER);

        stage.setResizable(false);
        stage.setTitle("YouTube to .mp3");
        stage.setScene(new Scene(contentPane));
        stage.centerOnScreen();
        stage.show();
    }
}
