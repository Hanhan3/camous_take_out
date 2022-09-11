package com.hanhan.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hanhan.common.BaseContex;
import com.hanhan.common.R;
import com.hanhan.entity.AddressBook;
import com.hanhan.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addressBook")
@Slf4j
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 新增通讯地址
     * @param addressBook
     * @return
     */
    @PostMapping
    public R<AddressBook> save(@RequestBody AddressBook addressBook){
        Long currentId = BaseContex.getCurrentId();
        addressBook.setUserId(currentId);
        log.info("addressBook = {}",addressBook);

        addressBookService.save(addressBook);
        return R.success(addressBook);
    }

    /**
     * 设置默认地址
     * @param addressBook
     * @return
     */
    @PutMapping("/default")
    public R<AddressBook> setDefault(@RequestBody AddressBook addressBook) {
        log.info("addressBook:{}",addressBook);

        Long currentId = BaseContex.getCurrentId();
        //将该用户的所有地址取消默认
        //Update AddressBook set is_default = 0 where user_id = currentid
        LambdaUpdateWrapper<AddressBook> luw = new LambdaUpdateWrapper<>();
        luw.eq(AddressBook::getUserId,currentId);
        luw.set(AddressBook::getIsDefault,0);
        addressBookService.update(luw);

        //设置默认地址
        addressBook.setIsDefault(1);
        addressBookService.updateById(addressBook);

        return R.success(addressBook);
    }

    /**
     * 根据id查找对象
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R get(@PathVariable Long id){
        AddressBook addressBook = addressBookService.getById(id);
        if(addressBook != null){
            return R.success(addressBook);
        }else{
            return R.error("没有找到该对象");
        }
    }

    /**
     * 查找默认地址
     * @return
     */
    @GetMapping("/default")
    public R<AddressBook> getDefault(){
        //select * from addressBook where user_id = ? and is_default = 1;
        LambdaQueryWrapper<AddressBook> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AddressBook::getUserId,BaseContex.getCurrentId());
        lqw.eq(AddressBook::getIsDefault,1);

        AddressBook addressBook = addressBookService.getOne(lqw);
        if(null == addressBook){
            return R.error("没有找到该对象");
        }else {
            return R.success(addressBook);
        }
    }

    /**
     * 查询指定用户的全部地址
     * @param addressBook
     * @return
     */
    @GetMapping("/list")
    public R<List> list(AddressBook addressBook){
        Long currentId = BaseContex.getCurrentId();
        addressBook.setUserId(currentId);
        log.info("addressBook :{}",addressBook);

        //select * from AddressBook where userid = currentid order by update_time desc
        LambdaQueryWrapper<AddressBook> lqw = new LambdaQueryWrapper<>();
        lqw.eq(null!= addressBook.getUserId(),AddressBook::getUserId,currentId);
        lqw.orderByDesc(AddressBook::getUpdateTime);

        return R.success(addressBookService.list(lqw));
    }
}
