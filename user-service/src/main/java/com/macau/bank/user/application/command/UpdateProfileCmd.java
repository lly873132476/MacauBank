package com.macau.bank.user.application.command;

import lombok.Data;
import java.io.Serializable;

@Data
public class UpdateProfileCmd implements Serializable {
    private String userNo;
    private String occupation;
    private String addressRegion;
    private String addressDetail;
}
