package com.springcli.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebGuiPageController {

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }
}