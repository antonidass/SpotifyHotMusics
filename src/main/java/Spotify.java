import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


public class Spotify {
    private static WebDriver driver;
    private static ArrayList<ArrayList<String>> urls;
    private static final String URLS_FILENAME = "release/urls.txt";
    private static final String ADDED_TRACKS = "release/added.txt";
    private static final String TOKEN = "release/token.txt";
    private static final HashMap<String, ArrayList<String>> hotTracks = new HashMap<>();

    public static void handleURL(String url, String description) throws InterruptedException, IOException {
        driver.get(url);

        Thread.sleep(5000);
        System.out.println("OK  " + description + "\n");

        List<WebElement> list_artists = driver.findElements(By.cssSelector("._cx_B0JpuGl6cJE7YqU1.G9bIawShCshI3fTSDVhR"));

        for (WebElement element : list_artists) {
            WebElement elem = null;
            try {
                elem = element.findElement(By.className("Pgykhr3SgX_EkuqacWvT"));
            } catch (Exception e) {
            }

            if (elem != null) {
                List<WebElement> data_artist = element.findElements(By.cssSelector(".standalone-ellipsis-one-line.FS85JWWz3ayMxrFzBjRD"));
                WebElement track_name = element.findElement(By.cssSelector(".eyyspMJ_K_t8mHpLP_kP.standalone-ellipsis-one-line.d7vg_NZ238PPPSLAWecY"));

                ArrayList<String> temp = new ArrayList<>();
                temp.add(track_name.getText());
                temp.add(data_artist.get(0).getText());
                temp.add(data_artist.get(1).getAttribute("href"));
                hotTracks.put(track_name.getText(), temp);
            }
        }
    }


    public static void readUrls() throws IOException {
        urls = new ArrayList<>();

        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(URLS_FILENAME));
        } catch (Exception e) {
            System.out.println("Error while open URLS_FILE!");
        }
        String str;

        while ((str = in.readLine()) != null) {
            ArrayList<String> temp = new ArrayList<>();
            temp.add(str.split(" ")[0]);
            temp.add(str.split(" ")[1]);
            urls.add(temp);
        }
        in.close();
    }


    public static void handleUniqueTracks() throws IOException {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(ADDED_TRACKS));
        } catch (Exception e) {
            System.out.println("Error while open UNIQUE_FILE!");
        }
        String str;

        while ((str = in.readLine()) != null) {

            if (hotTracks.get(str) != null) {
                hotTracks.remove(str);
            }
        }
        in.close();
    }

    public static void appendUniqueTracks() throws IOException {
        Set<String> keys = hotTracks.keySet();
        for (String key : keys) {
            Files.write(Paths.get(ADDED_TRACKS), (key + "\n").getBytes() , StandardOpenOption.APPEND);
        }
    }


    public static void main(String[] args) throws InterruptedException, IOException {
        BufferedReader in = new BufferedReader(new FileReader(TOKEN));
        TgBot.bot = new TelegramBot(in.readLine());
        in.close();

        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        TgBot tgBot = new TgBot();

        Thread thread = new Thread(() -> tgBot.serve());
        thread.start();

        readUrls();

        for (int i = 0; i < urls.size(); i++) {
            handleURL(urls.get(i).get(0), urls.get(i).get(1));
        }

        driver.close();

        handleUniqueTracks();
        appendUniqueTracks();

        for (String key : hotTracks.keySet()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(hotTracks.get(key).get(0) + "\n");
            stringBuilder.append(hotTracks.get(key).get(1) + "\n");
            stringBuilder.append(hotTracks.get(key).get(2) + "\n");
            tgBot.sendMessage(stringBuilder.toString());
        }
    }
}
