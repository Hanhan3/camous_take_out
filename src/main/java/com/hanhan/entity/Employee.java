package com.hanhan.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 员工实体
 */
@Data
public class Employee implements Serializable {
//    serialVersionUID 用来表明实现序列化类的不同版本间的兼容性。
//    如果你修改了此类, 要修改此值。否则以前用老版本的类序列化的类恢复时会出错。
    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String name;

    private String password;

    private String phone;

    private String sex;

    private String idNumber;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    //fill: 字段自动填充策略
    //DEFAULT	默认不处理
    //INSERT	插入时填充字段
    //UPDATE	更新时填充字段
    //INSERT_UPDATE	插入和更新时填充字段
    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;

}
