package ru.checkdev.auth.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.checkdev.auth.dto.ProfileDTO;
import ru.checkdev.auth.dto.ChatIdProfileDTO;
import ru.checkdev.auth.service.ProfileService;

import java.util.List;
import java.util.Optional;

/**
 * CheckDev пробное собеседование
 * ProfileController контроллер отправки и приема DTO модели ProfileDTO
 *
 * @author Dmitry Stepanov
 * @version 22.09.2023T23:49
 */
@RestController
@RequestMapping("/profiles")
@Slf4j
public class ProfileController {
    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    /**
     * Обрабатывает get запрос на получение профиля пользователя по запрошенному ID.
     *
     * @param id ID ProfileDTO
     * @return ResponseEntity
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProfileDTO> getProfileById(@PathVariable int id) {
        var profileDTO = profileService.findProfileByID(id);
        return new ResponseEntity<>(
                profileDTO.orElse(new ProfileDTO()),
                profileDTO.isEmpty() ? HttpStatus.NO_CONTENT : HttpStatus.OK
        );
    }

    /**
     * Отправляет все профили пользователей
     *
     * @return ResponseEntity
     */
    @GetMapping("/")
    public ResponseEntity<List<ProfileDTO>> getAllProfilesOrderByCreateDesc() {
        var profiles = profileService.findProfilesOrderByCreatedDesc();
        return new ResponseEntity<>(
                profiles,
                profiles.isEmpty() ? HttpStatus.NO_CONTENT : HttpStatus.OK
        );
    }

    @GetMapping("/chatId/{chatId}")
    public ResponseEntity<ChatIdProfileDTO> getProfileByChatId(@PathVariable Long chatId) {
        Optional<ChatIdProfileDTO> profileDTO = profileService.findProfileByChatId(chatId);
        return new ResponseEntity<>(
                profileDTO.orElse(new ChatIdProfileDTO()),
                profileDTO.isEmpty() ? HttpStatus.NO_CONTENT : HttpStatus.OK
        );
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ChatIdProfileDTO> getProfileByEmail(@PathVariable String email) {
        Optional<ChatIdProfileDTO> profile = profileService.findProfileByEmail(email);
        return new ResponseEntity<>(
                profile.orElse(new ChatIdProfileDTO()),
                profile.isEmpty() ? HttpStatus.NO_CONTENT : HttpStatus.OK
        );
    }
}
