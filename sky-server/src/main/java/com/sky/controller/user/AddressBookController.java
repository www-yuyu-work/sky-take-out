package com.sky.controller.user;

import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * C端地址簿接口
 */
@RestController
@RequestMapping("/user/addressBook")
@Slf4j
@Api(tags = "C端地址簿接口")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 新增地址
     */
    @ApiOperation("新增地址")
    @PostMapping
    public Result addAddressBook(@RequestBody AddressBook addressBook) {
        log.info("新增地址：{}", addressBook);
        addressBookService.addAddressBook(addressBook);
        return Result.success();
    }

    /**
     * 查询当前登录用户的所有地址信息
     */
    @ApiOperation("查询当前登录用户的所有地址信息")
    @GetMapping("/list")
    public Result<List<AddressBook>> getAddressList() {
        log.info("查询当前登录用户的所有地址信息");
        List<AddressBook> list = addressBookService.getAddressList();
        return Result.success(list);
    }

    /**
     * 查询默认地址
     */
    @ApiOperation("查询默认地址")
    @GetMapping("/default")
    public Result<AddressBook> getDefaultAddress() {
        log.info("查询默认地址");
        AddressBook addressBook = addressBookService.getDefaultAddress();
        return Result.success(addressBook);
    }

    /**
     * 根据id修改地址
     */
    @ApiOperation("根据id修改地址")
    @PutMapping
    public Result updateAddressBook(@RequestBody AddressBook addressBook) {
        log.info("修改地址：{}", addressBook);
        addressBookService.updateAddressBook(addressBook);
        return Result.success();
    }

    /**
     * 根据id删除地址
     */
    @ApiOperation("根据id删除地址")
    @DeleteMapping
    public Result deleteAddressBook(@RequestParam Long id) {
        log.info("删除地址：id={}", id);
        addressBookService.deleteAddressBook(id);
        return Result.success();
    }

    /**
     * 根据id查询地址
     */
    @ApiOperation("根据id查询地址")
    @GetMapping("/{id}")
    public Result<AddressBook> getAddressById(@PathVariable Long id) {
        log.info("查询地址：id={}", id);
        AddressBook addressBook = addressBookService.getAddressById(id);
        return Result.success(addressBook);
    }

    /**
     * 设置默认地址
     */
    @ApiOperation("设置默认地址")
    @PutMapping("/default")
    public Result setDefaultAddress(@RequestBody AddressBook addressBook) {
        log.info("设置默认地址：id={}", addressBook.getId());
        addressBookService.setDefaultAddress(addressBook.getId());
        return Result.success();
    }

}
