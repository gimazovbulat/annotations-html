package ru.itis.html.generator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@HtmlForm(method = "post", action = "/users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @HtmlInput(type = "text", name = "first_name", placeholder = "name")
    private String firstName;
    @HtmlInput(type = "text", name = "last_name", placeholder = "last_name")
    private String lastName;
    @HtmlInput(type = "email", name = "email", placeholder = "Email")
    private String email;
    @HtmlInput(type = "password", name = "password", placeholder = "password")
    private String password;
}