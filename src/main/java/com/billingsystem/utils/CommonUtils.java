package com.billingsystem.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CommonUtils {

    public static String generateInvoiceNumberFromOrder(String orderNumber) {
        if (orderNumber == null || !orderNumber.startsWith("ORD-")) {
            throw new IllegalArgumentException("Invalid order number: " + orderNumber);
        }
        return orderNumber.replaceFirst("ORD-", "INV-");
    }


}
