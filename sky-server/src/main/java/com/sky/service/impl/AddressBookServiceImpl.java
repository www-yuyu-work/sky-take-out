package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.AddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressBookServiceImpl implements AddressBookService {

    @Autowired
    private AddressBookMapper addressBookMapper;

    /**
     * 新增地址
     * @param addressBook
     */
    @Override
    public void addAddressBook(AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBook.setIsDefault(0);
        addressBookMapper.insert(addressBook);
    }

    /**
     * 查询当前登录用户的所有地址
     * @return
     */
    @Override
    public List<AddressBook> getAddressList() {
        return addressBookMapper.getByUserId(BaseContext.getCurrentId());
    }

    /**
     * 查询当前登录用户的默认地址
     * @return
     */
    @Override
    public AddressBook getDefaultAddress() {
        return addressBookMapper.getDefault(BaseContext.getCurrentId());
    }

    /**
     * 修改地址
     * @param addressBook
     */
    @Override
    public void updateAddressBook(AddressBook addressBook) {
        addressBookMapper.update(addressBook);
    }

    /**
     * 根据id删除地址
     * @param id
     */
    @Override
    public void deleteAddressBook(Long id) {
        addressBookMapper.deleteById(id);
    }

    /**
     * 根据id查询地址
     * @param id
     * @return
     */
    @Override
    public AddressBook getAddressById(Long id) {
        return addressBookMapper.getById(id);
    }

    /**
     * 设置默认地址
     * @param id
     */
    @Override
    public void setDefaultAddress(Long id) {
        addressBookMapper.clearDefault(BaseContext.getCurrentId());
        addressBookMapper.setDefault(id);
    }

}
