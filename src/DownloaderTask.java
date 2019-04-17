import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by HP xw8400
 * Author: Jacob
 * Date: 4/16/2019.
 */
public class DownloaderTask {

    private final ProgressBar bar;
    private Task downloader;

    private VBox contentPane;
    private Label statusLabel;

    public DownloaderTask() {
        bar = new ProgressBar(0);
        bar.setPrefSize(200, 20);

        contentPane = new VBox(20, statusLabel = new Label("Youtube .mp3 downloader"), bar);
        contentPane.setAlignment(Pos.CENTER);
    }

    public void start(String url, String outputDirectory) {
        downloader = downloadMP3(url, outputDirectory);
        bar.progressProperty().bind(this.downloader.progressProperty());
        new Thread(downloader).start();
    }

    private String getYouTubeVideoTitle(String url) {
        String title = "";
        try {
            final URLConnection con = new URL(url).openConnection();
            final BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

            final String pattern = "title\\\\\":\\\\.+?[\\\\]";
            final Pattern compiledPattern = Pattern.compile(pattern);

            String line;

            while ((line = in.readLine()) != null) {
                Matcher matcher = compiledPattern.matcher(line);
                if (matcher.find()) {
                    // replace every character that isn't a letter, number, hyphen, or space
                    title = matcher.group().substring(matcher.group().lastIndexOf('"') + 1, matcher.group().length() - 1).replaceAll("[^A-Za-z0-9\\- ]", "");
                    break;
                }
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return title;
    }

    private Task downloadMP3(String url, String outputDirectory) {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    updateStatus("Fetching title");

                    String title = DownloaderTask.this.getYouTubeVideoTitle(url);
                    if (title.isEmpty()) {
                        title = url;
                    }

                    updateStatus("Establishing a connection");

                    // establish a connection
                    final HttpURLConnection connection = (HttpURLConnection) new URL("https://convertmp3.io/fetch/?video=" + url).openConnection();

                    connection.setUseCaches(false);
                    connection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");

                    final int responseCode = connection.getResponseCode();

                    // error on client error response
                    if (responseCode > 399) {
                        error("Error response " + responseCode + " possible bad link");
                        return false;
                    }

                    // open an input stream so data can be read
                    final InputStream reader = connection.getInputStream();
                    final double length = connection.getContentLength();

                    if (length < 1) {
                        error("Stream error - " + title + " ");
                        return false;
                    }

                    double writtenOut = 0;

                    final byte[] maxMemory = new byte[4096];

                    // open a file output stream to write the data out to
                    final FileOutputStream writer = new FileOutputStream(new File(outputDirectory + title + ".mp3"));

                    int buffer;
                    while ((buffer = reader.read(maxMemory)) != -1) {
                        writer.write(maxMemory, 0, buffer);
                        updateProgress(writtenOut += buffer, length);
                        updateStatus(title + " " + Math.round(writtenOut / length * 100) + "%");
                    }

                    // close our streams
                    reader.close();
                    writer.flush();
                    writer.close();

                    updateStatus("Download complete");

                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    error("Error - see stack trace");
                }
                return false;
            }
        };
    }

    public VBox getContentPane() {
        return contentPane;
    }

    private void updateStatus(String status) {
        Platform.runLater(() ->
                statusLabel.setText(status)
        );
    }

    private void error(String status) {
        System.err.println(status);
        updateStatus(status);
    }
}
