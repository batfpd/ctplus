package com.ctplus;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.teaopenapi.models.Config;
import ctp.thostapi.CThostFtdcMdApi;
import ctp.thostapi.CThostFtdcTraderApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CaotpApplication {
    static{
        System.loadLibrary("thosttraderapi_se");
        System.loadLibrary("thostmduserapi_se");
        System.loadLibrary("thostapi_wrap");
    }


    public static void main(String[] args) {

        SpringApplication.run(CaotpApplication.class, args);

    }



    @Bean
    public CThostFtdcTraderApi traderApi() {
        return CThostFtdcTraderApi.CreateFtdcTraderApi();
    }

    @Bean
    public CThostFtdcMdApi mdApi() {
        return CThostFtdcMdApi.CreateFtdcMdApi();
    }

//    final static String accessKeyId="LTAI5tK2xzoQ7v4dcA91CcAy";
//    final static String accessKeySecret="PNZmUZWbxzEOrlFAxox3Eymee0gCJr";
    final static String accessKeyId="LTAI4G3niPmaFHTh4YrLCL8Z";
    final static String accessKeySecret="9CUEBEpFNTrMXVICdomWOFXoJ3ZCGt";

    @Bean
    public Client messageClient() throws Exception {
        Config config = new Config()
                // 您的AccessKey ID
                .setAccessKeyId(accessKeyId)
                // 您的AccessKey Secret
                .setAccessKeySecret(accessKeySecret);
        // 访问的域名
        config.endpoint = "dysmsapi.aliyuncs.com";
        return new Client(config);
    }


}
