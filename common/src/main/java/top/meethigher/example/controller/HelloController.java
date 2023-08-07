package top.meethigher.example.controller;

import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 你好控制器
 *
 * @author chenchuancheng
 * @date 2023/08/03 16:04
 */
@Api(tags = "Hello")
@RestController
public class HelloController {
    @GetMapping("/hello")
    public String test1() {
        return "hello world";
    }

}
