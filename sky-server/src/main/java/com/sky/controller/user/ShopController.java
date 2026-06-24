package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("userShopController")
@RequestMapping("/user/shop")
@Slf4j
@Api(tags = "C端店铺操作接口")
public class ShopController {

    private static final String SHOP_STATUS_KEY = "SHOP_STATUS";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 获取营业状态
     */
    @ApiOperation("获取营业状态")
    @GetMapping("/status")
    public Result<Integer> getStatus() {
        String status = stringRedisTemplate.opsForValue().get(SHOP_STATUS_KEY);
        // 默认营业状态为1（营业）
        int shopStatus = status == null ? 1 : Integer.parseInt(status);
        log.info("获取营业状态：{}", shopStatus);
        return Result.success(shopStatus);
    }
}
