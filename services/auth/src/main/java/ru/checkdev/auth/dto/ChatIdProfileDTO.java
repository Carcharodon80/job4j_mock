package ru.checkdev.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ChatIdProfileDTO {
    @EqualsAndHashCode.Include
    private Integer id;
    private String username;
    private String password;
    private String email;
    private Long chatId;
}