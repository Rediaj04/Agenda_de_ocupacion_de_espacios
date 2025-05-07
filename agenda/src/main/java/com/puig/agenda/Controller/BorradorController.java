package com.puig.agenda.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BorradorController {

    @RequestMapping("/hello")
    public String hello() {
        return "Aqui va la gracia de la agenda.............................";
    }

}
