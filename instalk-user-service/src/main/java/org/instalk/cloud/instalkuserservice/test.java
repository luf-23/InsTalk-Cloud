package org.instalk.cloud.instalkuserservice;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class test {
    @RequestMapping("/hello")
    public String hello() {
        return "hello world";
    }
}
