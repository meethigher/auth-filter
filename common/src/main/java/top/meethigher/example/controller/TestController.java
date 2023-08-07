package top.meethigher.example.controller;


import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.meethigher.utils.UserUtils;

/**
 * 测试控制器
 *
 * @author chenchuancheng
 * @date 2023/08/03 10:43
 */
@Api(tags = "Test")
@RestController
@RequestMapping("/test")
public class TestController {


    @GetMapping("/test1")
    public String test1() {
        return UserUtils.get();
    }

    @GetMapping("/test1/test2")
    public String test2() {
        return UserUtils.get();
    }
}
