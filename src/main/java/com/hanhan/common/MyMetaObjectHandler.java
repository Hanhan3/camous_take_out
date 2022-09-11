package com.hanhan.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("插入时自动填充字段...");
        log.info(metaObject.toString());
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime",LocalDateTime.now() );
        metaObject.setValue("createUser", BaseContex.getCurrentId());
        metaObject.setValue("updateUser", BaseContex.getCurrentId());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("更新时自动填充字段...");
        log.info(metaObject.toString());
        metaObject.setValue("updateTime",LocalDateTime.now() );
        metaObject.setValue("updateUser",BaseContex.getCurrentId());
    }
}
