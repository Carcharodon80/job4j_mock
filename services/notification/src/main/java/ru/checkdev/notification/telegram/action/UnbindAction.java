package ru.checkdev.notification.telegram.action;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.domain.PersonDTO;
import ru.checkdev.notification.telegram.config.TgConfig;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;

@AllArgsConstructor
@Slf4j
public class UnbindAction implements Action {

    private static final String URL_UNBIND = "/updateChatId";
    private static final String GET_PROFILE_BY_EMAIL = "/profiles/email/";
    private static final String GET_PROFILE_BY_CHATID = "profiles/chatId/";
    private static final String SL = System.lineSeparator();
    private final TgConfig tgConfig = new TgConfig("tg/", 8);
    private final TgAuthCallWebClint tgAuthCallWebClint;


    @Override
    public BotApiMethod<Message> handle(Message message) {
        String chatId = message.getChatId().toString();

        Object object;
        try {
            object = tgAuthCallWebClint.doGet(GET_PROFILE_BY_CHATID + chatId).block();
        } catch (Exception e) {
            return new SendMessage(chatId, "Сервис не доступен, попробуйте позже.");
        }

        if (object == null) {
            return new SendMessage(chatId, "Отсутствуют аккаунты CheckDev," + SL
                    + "привязанные к текущему аккаунту Telegram." + SL
            + "/new для регистрации нового аккаунта," + SL
            + "/bind для привязки существующего аккаунта.");
        }

        return new SendMessage(chatId, "Введите почту и пароль чтобы отвязать текущий аккаунт Telegram:");
    }

    @Override
    public BotApiMethod<Message> callback(Message message) {
        Long chatId = message.getChatId();
        String email = message.getText();

        Object object;

        try {
            object = tgAuthCallWebClint.doGet(GET_PROFILE_BY_EMAIL + email).block();
        } catch (Exception e) {
            return new SendMessage(chatId.toString(), "Сервис недоступен, попробуйте позже");
        }

        PersonDTO personDTO = tgConfig.getObjectToPersonDTO(object);

        if (personDTO == null) {
            return new SendMessage(chatId.toString(), "Пользователь с такой почтой не найден");
        }

        try {
            personDTO.setChatId(null);
        } catch (Exception e) {
            return new SendMessage(chatId.toString(), "Сервис недоступен, попробуйте позже");
        }

        tgAuthCallWebClint.doPost(URL_UNBIND, personDTO).block();

        return new SendMessage(chatId.toString(), "Текущий аккаунт Telegram отвязан от аккаунта CheckDev,"
                + SL + "/bind - привязать аккаунт,"
                + SL + "/check для проверки");
    }
}
