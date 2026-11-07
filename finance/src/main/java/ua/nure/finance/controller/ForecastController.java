package ua.nure.finance.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ForecastController {

    @GetMapping("/forecast")
    public String getForecast() {
        return "forecast";
    }
}
