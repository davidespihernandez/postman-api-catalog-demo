package com.example;

import com.orderssdk.OrdersSdk;
import com.orderssdk.exceptions.ApiError;
import com.orderssdk.models.HealthResponse;

public class Main {

  public static void main(String[] args) {
    OrdersSdk ordersSdk = new OrdersSdk();

    try {
      HealthResponse response = ordersSdk.system_.getHealth();

      System.out.println(response);
    } catch (ApiError e) {
      e.printStackTrace();
    }

    System.exit(0);
  }
}
