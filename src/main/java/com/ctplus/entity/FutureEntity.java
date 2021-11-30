package com.ctplus.entity;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Date;

@Slf4j
@Data
public class FutureEntity implements Serializable {
    private String code;
    private Date startDate;
    private Date endDate;
    private String exchange;
}
