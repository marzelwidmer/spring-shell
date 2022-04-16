package com.example.shell

import org.jline.utils.AttributedString
import org.jline.utils.AttributedStyle
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.shell.Availability
import org.springframework.shell.jline.PromptProvider
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellMethodAvailability
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicReference

@SpringBootApplication
class ShellApplication

fun main(args: Array<String>) {
    runApplication<ShellApplication>(*args)
}

@Component
class LoginService(private var user: AtomicReference<String> = AtomicReference<String>()) {

    fun logout() {
        this.user.set(null)
    }

    fun login(username: String, password: String) {
        this.user.set(username)
    }

    fun isLoggedIn() : Boolean {
        return user.get() != null
    }

    fun loggedInUser() = user.get()

}

@ShellComponent
class LoginCommands(val service: LoginService) {

    @ShellMethod(value = "login")
    fun login(username: String, password: String) {
        this.service.login(username, password)
    }

    @ShellMethod(value = "logout")
    @ShellMethodAvailability(value = ["logoutAvailability"])
    fun logout() {
        this.service.logout()
    }

    fun logoutAvailability(): Availability? {
        return when (this.service.isLoggedIn()) {
            true -> Availability.available()
            false -> Availability.unavailable("you must be logged in")
        }
    }
}

@Component
data class LoginPromptProvider(val loginService: LoginService) : PromptProvider {
    override fun getPrompt(): AttributedString {
        return when (this.loginService.isLoggedIn()) {
            true ->  AttributedString(
                "${this.loginService.loggedInUser()} > ", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN)
            )
            false ->  AttributedString(
                " (unknown) > ", AttributedStyle.DEFAULT.foreground(AttributedStyle.RED)
            )
        }
    }
}

