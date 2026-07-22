package io.github.kaltz.feldbuch.conversation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ConversationViewController {

    @GetMapping("/conversations")
    public String conversations() {
        return "conversation/index";
    }

}
