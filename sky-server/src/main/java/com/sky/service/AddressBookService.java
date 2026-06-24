package com.sky.service;

import com.sky.entity.AddressBook;

import java.util.List;

public interface AddressBookService {

    /**
     * 新增地址
     * @param addressBook
     */
    void addAddressBook(AddressBook addressBook);

    /**
     * 查询当前登录用户的所有地址
     * @return
     */
    List<AddressBook> getAddressList();

    /**
     * 查询当前登录用户的默认地址
     * @return
     */
    AddressBook getDefaultAddress();

    /**
     * 修改地址
     * @param addressBook
     */
    void updateAddressBook(AddressBook addressBook);

    /**
     * 根据id删除地址
     * @param id
     */
    void deleteAddressBook(Long id);

    /**
     * 根据id查询地址
     * @param id
     * @return
     */
    AddressBook getAddressById(Long id);

    /**
     * 设置默认地址
     * @param id
     */
    void setDefaultAddress(Long id);

}
