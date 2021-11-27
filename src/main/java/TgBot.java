import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;

import java.io.*;

public class TgBot {
    private final String USER_FILENAME = "release/user.txt";
    private static long chatId = 0;
    public static TelegramBot bot;

    public void serve() {
        bot.setUpdatesListener(updates -> {
            updates.forEach(this::process);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private void process(Update update) {
        Message message = update.message();

        if (message.text().equals("get")) {
            try (BufferedReader br = new BufferedReader(new FileReader(USER_FILENAME))) {
                String userId = br.readLine();
                if (userId != null) {
                    chatId = Long.parseLong(userId);
                } else {
                    chatId = message.chat().id();
                    br.close();

                    BufferedWriter out = new BufferedWriter(new FileWriter(USER_FILENAME));
                    out.write(String.valueOf(chatId));
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message) throws IOException {
        if (chatId == 0) {
            try (BufferedReader br = new BufferedReader(new FileReader(USER_FILENAME))) {
                String userId = br.readLine();
                chatId = Long.parseLong(userId);
            }
        }

        BaseRequest request = null;

        request = new SendMessage(chatId, message);
        if (request != null) {
            bot.execute(request);
        }
    }
}