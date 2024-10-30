package ru.checkdev.notification.telegram.action;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.domain.PersonDTO;
import ru.checkdev.notification.telegram.config.TgConfig;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;

import java.util.Calendar;

/**
 * 3. Мидл
 * Класс реализует пункт меню регистрации нового пользователя в телеграм бот
 *
 * @author Dmitry Stepanov, user Dmitry
 * @since 12.09.2023
 */
@AllArgsConstructor
@Slf4j
public class RegAction implements Action {
    private static final String ERROR_OBJECT = "error";
    private static final String URL_AUTH_REGISTRATION = "/registration";
    private static final String URL_CHECK_CHATID = "profiles/chatId/";
    private final TgConfig tgConfig = new TgConfig("tg/", 8);
    private final TgAuthCallWebClint tgAuthCallWebClint;
    private final String urlSiteAuth;

    @Override
    public BotApiMethod<Message> handle(Message message) {
        Long chatId = message.getChatId();

        Object object;
        try {
            object = tgAuthCallWebClint.doGet(URL_CHECK_CHATID + chatId).block();
        } catch (Exception e) {
            return new SendMessage(chatId.toString(), "Сервис не доступен, попробуйте позже.");
        }
        if (object != null) {
            return new SendMessage(String.valueOf(chatId),
                    "К этому аккаунту Telegram уже привязан профиль CheckDev," + System.lineSeparator()
                            + "/check для проверки" + System.lineSeparator()
                            + "/unbind - отвязать аккаунт");
        }

        var text = "Введите email для регистрации:";
        return new SendMessage(String.valueOf(chatId), text);
    }

    /**
     * Метод формирует ответ пользователю.
     * Весь метод разбит на 4 этапа проверки.
     * 1. Проверка на соответствие формату Email введенного текста.
     * 2. Отправка данных в сервис Auth и если сервис не доступен сообщаем
     * 3. Если сервис доступен, получаем от него ответ и обрабатываем его.
     * 3.1 ответ при ошибке регистрации
     * 3.2 ответ при успешной регистрации.
     *
     * @param message Message
     * @return BotApiMethod<Message>
     */
    @Override
    public BotApiMethod<Message> callback(Message message) {
        Long chatId = message.getChatId();
        String username = message.getFrom().getFirstName();
        String email = message.getText();
        String text;
        String sl = System.lineSeparator();

        if (!tgConfig.isEmail(email)) {
            text = "Email: " + email + " не корректный." + sl
                    + "попробуйте снова." + sl
                    + "/new";
            return new SendMessage(String.valueOf(chatId), text);
        }

        String password = tgConfig.getPassword();
        PersonDTO person = new PersonDTO(0, username, email, password, true, null,
                Calendar.getInstance(), chatId);
        Object result;
        try {
            result = tgAuthCallWebClint.doPost(URL_AUTH_REGISTRATION, person).block();
        } catch (Exception e) {
            log.error("WebClient doPost error: {}", e.getMessage());
            text = "Сервис не доступен попробуйте позже" + sl
                    + "/start";
            return new SendMessage(String.valueOf(chatId), text);
        }

        var mapObject = tgConfig.getObjectToMap(result);

        if (mapObject.containsKey(ERROR_OBJECT)) {
            text = "Ошибка регистрации: " + mapObject.get(ERROR_OBJECT);
            return new SendMessage(String.valueOf(chatId), text);
        }

        text = "Вы зарегистрированы: " + sl
                + "Логин: " + email + sl
                + "Пароль: " + password + sl
                + urlSiteAuth;
        return new SendMessage(String.valueOf(chatId), text);
    }
}
