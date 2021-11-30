package com.ctplus.service;


import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class MessageService {

    @Resource
    private Client messageClient;

    public void sendNotification(String time, String real) throws Exception {
        String[] phoneList = new String[]{"13716061221"};
        for(String phone:phoneList){
            SendSmsRequest sendSmsRequest = new SendSmsRequest()
                    .setPhoneNumbers(phone)
                    .setSignName("中金鼎石")
                    .setTemplateCode("SMS_223586734")
                    .setTemplateParam("{\"time\":\""+time+"\", \"real\":\""+real+"\", \"normal\":\"50\"}");
            messageClient.sendSms(sendSmsRequest);
        }

    }


}
