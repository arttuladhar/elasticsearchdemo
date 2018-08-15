package com.aayushtuladhar.demo.elasticsearchdemo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GreetingsController {

  @GetMapping("/")
  public String greetings(Model model){
    String messageString = "Hello ElasticSearch User";
    model.addAttribute("message", messageString);
    return "greetings";
  }

}
