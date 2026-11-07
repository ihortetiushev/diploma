package ua.nure.finance.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OptimizationController {

    @GetMapping("/optimization")
    public String getOptimization() {
        return "optimization";
    }
}
