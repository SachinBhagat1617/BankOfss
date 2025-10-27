package com.ofss.Account;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AccountApplication {

	public static void main(String[] args) {

        SpringApplication.run(AccountApplication.class, args);
        //System.out.println("Account runnning on"+serverPort);

    }

}
