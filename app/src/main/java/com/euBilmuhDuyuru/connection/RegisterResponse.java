package com.euBilmuhDuyuru.connection;

/**
 * Created by Yunus Emre Şeker on 08.03.2015.
 * -

 */
public class RegisterResponse {

    public static final int SUCCESS = 1;

    public String regID;
    public int regCode;

    public RegisterResponse(String regID, int regCode) {
        this.regID = regID;
        this.regCode = regCode;
    }

}
