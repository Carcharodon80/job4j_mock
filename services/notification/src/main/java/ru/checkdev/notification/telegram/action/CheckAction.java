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
public class CheckAction implements Action {
    private static final String URL_CHECK_CHATID = "profiles/chatId/";
    private final TgConfig tgConfig = new TgConfig("tg/", 8);
    private final TgAuthCallWebClint tgAuthCallWebClint;

    @Override
    public BotApiMethod<Message> handle(Message message) {

        String sl = System.lineSeparator();
        String chatId = message.getChatId().toString();
        Long userChatId = message.getFrom().getId();

        Object object;

        try {
            object = tgAuthCallWebClint.doGet(URL_CHECK_CHATID + userChatId).block();
        } catch (Exception e) {
            return new SendMessage(chatId, "Сервис не доступен, попробуйте позже.");
        }

        PersonDTO personDTO = tgConfig.getObjectToPersonDTO(object);

        if (personDTO == null) {
            String result = "Пользователь не зарегистрирован," + System.lineSeparator()
                    + "/new для регистрации.";
            return new SendMessage(chatId, result);
        }

        String text = "Вы зарегистрированы." + sl
                + "Имя: " + personDTO.getUsername() + sl
                + "Почта: " + personDTO.getEmail();

        return new SendMessage(chatId, text);
    }

    @Override
    public BotApiMethod<Message> callback(Message message) {
        return handle(message);
    }
}
