package sample;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.awt.*;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.*;

import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.swing.text.html.ImageView;
import java.net.URI;
import java.util.List;

public class Controller implements Initializable {
    // Deklariranje elemenata (gumbi, izbornik, klizač...)
    @FXML
    private ListView list;
    @FXML
    private Label songLabel;
    @FXML
    private Label vrijeme, ukupnoVrijeme;
    @FXML
    private Button playButton, pauseButton, resetButton, previousButton, nextButton;
    @FXML
    private ComboBox speedBox;
    @FXML
    private Slider volumeSlider;
    @FXML
    private ProgressBar songProgressBar;
    @FXML
    private CheckBox loop;
    @FXML
    private AnchorPane pane;
    private int[] brzina={25,50,75,100,125,150,175,200};
    private MediaPlayer mediaPlayer;
    private String mapa;
    private String pjesma;
    private Window stage;
    private Media media;
    private boolean a=false;
    private int redniBroj;
    private List<String> lista;
    private String rate1;
    private double rate2;
    private Timer timer;
    private TimerTask task;
    private boolean running;




    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    // For petlja koja će u izbornik za brzine dodati brzine koje su zapisane u polju brzina
    for (int i=0; i<brzina.length; i++){
        speedBox.getItems().add((brzina[i])+"%");
    }

    // Postavlja funkciju koja će se izvršiti kada odaberemo brzinu
    speedBox.setOnAction(this::changeSpeed);

    // Postavlja poćetnu brzinu  na 100%
    rate2=100;

    // Dohvaća popunjenost klizača te pretvara tu vrijednost u postotke kako bi se smanjio ili povećao zvuk
    volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
        @Override
        public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
            mediaPlayer.setVolume(volumeSlider.getValue()*0.01);
        }
    });

    }


    public void selectedsong(int a){
        // Klikom na pjesmu u listi prebacuje se na nju
        list.getSelectionModel().select(a);
    }

    public void loopMedia(){
        // Ukoliko je potvrdni okvir pritisnut pjesma će se ponavljati, te ako nije neće.
        if (loop.isSelected()){
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        } else {
            mediaPlayer.setCycleCount(0);
        }
    }

    public void playMedia(){
        // Pokreće pjesmu od zaustavljenog vremena
        mediaPlayer.play();
    }

    public void pauseMedia(){
        // Zaustavlja pjesmu
        mediaPlayer.pause();
    }

    public void resetMedia(){
        // Vraća pjesmu na početak
        mediaPlayer.seek(Duration.seconds(0));
    }

    public void previousMedia(){
        // Ako je pjesma prva u listi postavlja pjesmu na zadnju u listi ako nije vraća na prošlu pjesmu
        mediaPlayer.stop();
        if (redniBroj>0) {
            redniBroj--;
        }
        else{
            redniBroj=lista.size()-1;
        }
        // Dohvaća pjesmu lokaciju pjesme na listi i pokreće ju
        selectedsong(redniBroj);
        pjesma=lista.get(redniBroj);
        media = new Media(new File(mapa+"\\"+pjesma).toURI().toString());
        mediaPlayer =new MediaPlayer(media);
        mediaPlayer.play();
        // Ako je već puštena jedna pjesma zaustavlja timer koji prati vrijeme pjesme
        if (running) {
            cancelTimer();
        }
        beginTimer();
        mediaPlayer.setRate(rate2*0.01);
        // Postavlja pjesmu na određeni zvuk
        mediaPlayer.setVolume(volumeSlider.getValue()*0.01);
        // Poziva funkciju za ponavljanje pjesme
        loopMedia();
    }

    public void nextMedia(){
        // Ako je pjesma zadnja u listi postavlja pjesmu na zadnju u listi ako nije vraća na prošlu pjesmu
        mediaPlayer.stop();
        if (redniBroj<lista.size()-1) {
            redniBroj++;
        }
        else{
            redniBroj=0;
        }
        // Dohvaća pjesmu lokaciju pjesme na listi i pokreće ju
        selectedsong(redniBroj);
        pjesma=lista.get(redniBroj);
        media = new Media(new File(mapa+"\\"+pjesma).toURI().toString());
        mediaPlayer =new MediaPlayer(media);
        mediaPlayer.play();
        // Ako je već puštena jedna pjesma zaustavlja timer koji prati vrijeme pjesme
        if (running) {
            cancelTimer();
        }
        beginTimer();
        mediaPlayer.setRate(rate2*0.01);
        // Postavlja pjesmu na određeni zvuk
        mediaPlayer.setVolume(volumeSlider.getValue()*0.01);
        // Poziva funkciju za ponavljanje pjesme
        loopMedia();
    }

    public void changeSpeed(Event event){
        // Dohvaća vrijednost izbornika za brzine te ga pretvara u integer
        rate1  = speedBox.getValue().toString().substring(0,speedBox.getValue().toString().length()-1);
        rate2 = Integer.parseInt(rate1);
        // Postavlja pjesmu na određenu brzinu
        mediaPlayer.setRate(rate2*0.01);
    }

    public void beginTimer(){
        // Postavlja novi timer ukoliko ima pjesme
        timer = new Timer();
        task = new TimerTask() {
            public void run(){
            running = true;
            // Dohvaća trenutno vrijeme i ukupno vrijeme pjesme te ispunjava progressbar po njoj
            double current = mediaPlayer.getCurrentTime().toSeconds();
            double end =media.getDuration().toSeconds();
            songProgressBar.setProgress(current/end);

                if(current==end){
                    // Ako je pjesma zaustavljena zaustavlja timer
                    cancelTimer();
                    if (!loop.isSelected()) {
                        nextMedia();
                    }
                }

                Platform.runLater(new Runnable(){

                    @Override
                    public void run() {
                        // Dohvaća trenutno vrijeme i ukupno vrijeme pjesme te ih ispisuje na ekranu
                        String sekunde = String.valueOf((int) media.getDuration().toSeconds()%60);
                        if ((int) media.getDuration().toSeconds()%60<10){
                            sekunde="0"+sekunde;
                        }
                        String sekunde2 = String.valueOf((int) mediaPlayer.getCurrentTime().toSeconds()%60);
                        if ((int) mediaPlayer.getCurrentTime().toSeconds()%60<10){
                            sekunde2="0"+sekunde2;
                        }
                        ukupnoVrijeme.setText(String.valueOf((int) media.getDuration().toSeconds()/60)+":"+sekunde);
                        vrijeme.setText(String.valueOf((int)mediaPlayer.getCurrentTime().toSeconds()/60)+":"+sekunde2);
                    }
                });
            }


        };
        timer.scheduleAtFixedRate(task,1000,1000);
    }

    public void cancelTimer(){
        //  Ukoliko nije pokrenuta pjesma gasi timer
        running=false;
        timer.cancel();
    }




    public void setTime(MouseEvent event){
        // Postavlja pjesmu na kliknuto vrijeme na progress baru
        double a = event.getX()/songProgressBar.getWidth();
        int b = (int) (media.getDuration().toSeconds()*a);
        mediaPlayer.seek(Duration.seconds(b));
    }

    public void selectSong(){
        // Ako je pjesma već pokrenut zaustavlja ju
        if (a){
            mediaPlayer.stop();
        }
        // Dohvaća pjesmu lokaciju pjesme na listi i pokreće ju
        pjesma=list.getSelectionModel().getSelectedItem().toString();
        media = new Media(new File(mapa+"\\"+pjesma).toURI().toString());
        mediaPlayer =new MediaPlayer(media);
        redniBroj = lista.indexOf(pjesma);
        mediaPlayer.play();
        if (running) {
            cancelTimer();
        }
        beginTimer();
        mediaPlayer.setRate(rate2*0.01);
        mediaPlayer.setVolume(volumeSlider.getValue()*0.01);
        loopMedia();
        a=true;
    }

    public void selectMedia(){
        // Otvara novi prozor za odabiranje datoteke
        DirectoryChooser directoryChooser = new DirectoryChooser();

        File selectedDirectory = directoryChooser.showDialog(stage);
        mapa=selectedDirectory.getAbsolutePath();

        if(selectedDirectory == null){

        }else{
            // Datoteke koje na kraju imaju .mp3 nastavak dodaje na listu
            list.getItems().clear();
            String[] lista1 = {};
            lista = new ArrayList<>();
            File f = new File(selectedDirectory.getAbsolutePath());
            lista1 = f.list();
            for (int i=0; i<lista1.length; i++){
                if (lista1[i].contains(".mp3")){
                    lista.add(lista1[i]);
                }
            }
            list.getItems().addAll(lista);
        }

    }


}
