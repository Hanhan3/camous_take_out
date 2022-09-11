package com.hanhan.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hanhan.common.R;
import com.hanhan.entity.User;
import com.hanhan.service.UserService;
import com.hanhan.utils.SMSUtils;
import com.hanhan.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;


    /*
    发送短信验证
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {
        //1.获取手机号码
        String phone = user.getPhone();
        //2.判断手机号是否为空
        if (StringUtils.isNotEmpty(phone)) {
            //不为空则生成验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code = {}", code);
            //2.发送验证码
            //SMSUtils.sendMessage();
            //3.将验证码存储到Session方便之后校验
            session.setAttribute(phone, code);
            return R.success("发送验证码成功");
        }
        return R.error("短信发送失败");
    }

    /*
    移动用户登陆
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session) {
        log.info(map.toString());
        //获取手机号
        String phone = map.get("phone").toString();
        //获取验证码
        String code = map.get("code").toString();
        //从Session中获取保存的验证码
        Object sessionCode = session.getAttribute(phone);

        //进行验证码的比对（页面提交的验证码和Session中保存的验证码比对）
        if(sessionCode != null && sessionCode.equals(code)) {
            //如果能够比对成功，说明登录成功
            LambdaQueryWrapper<User> lqw = new LambdaQueryWrapper<>();
            lqw.eq(User::getPhone,phone);

            User one = userService.getOne(lqw);
            if(one == null){
                //判断当前手机号对应的用户是否为新用户，如果是新用户就自动完成注册
                User user = new User();
                user.setPhone(phone);
                user.setStatus(1);

                userService.save(user);
            }
            session.setAttribute("user", one.getId());
            return R.success(one);
        }
        return R.error("登陆失败");
    }
}
