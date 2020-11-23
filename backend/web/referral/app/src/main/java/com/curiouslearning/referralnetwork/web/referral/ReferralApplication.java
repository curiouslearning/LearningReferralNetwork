package com.curiouslearning.referralnetwork.web.referral;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@ComponentScan(basePackages = { "com.curiouslearning.referralnetwork.web.referral.api" })
public class ReferralApplication {

  public static void main(String[] args) {
    SpringApplication.run(ReferralApplication.class, args);
  }

}
