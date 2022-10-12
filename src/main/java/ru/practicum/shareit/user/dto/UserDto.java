package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Getter
@EqualsAndHashCode
@Builder
public class UserDto {
    private long id;
    @NotNull
    private String name;
    @NotNull
    @Email
    private String email;
}