package io.github.kaltz.feldbuch.home.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeViewController {

    @GetMapping("/")
    public String home() {
        return "redirect:/conversations";
    }
}
