package ru.netology.testUtils;

import com.github.javafaker.Faker;

import java.time.LocalDate;
import java.util.Random;

public class UtilGenerator {


    public static Card getCardForPay(String numberCard) {
        Card cardForPay = new Card();
        cardForPay.setNumber(numberCard);
        cardForPay.setMonth(generateMonth());
        cardForPay.setYear(getShiftedYYFromCurrent(2));
        cardForPay.setCardholder(generateCardholder());
        cardForPay.setCvc(generateCvc());
        return cardForPay;
    }

    public static String generateMonth() {
        Random random = new Random();
        return String.format("%02d", (random.nextInt(12) + 1));
    }

    public static String getShiftedMMFromCurrent(int shiftingMonths) {
        LocalDate date = LocalDate.now().plusMonths(shiftingMonths);
        return String.format("%02d", date.getMonthValue());
    }

    public static String getShiftedYYFromCurrentByMonth(int shiftingMonths) {
        LocalDate date = LocalDate.now().plusMonths(shiftingMonths);
        date = date.minusYears(2000);
        return String.format("%02d", date.getYear());
    }

    public static String getShiftedYYFromCurrent(int shiftingYear) {
        LocalDate date = LocalDate.now().plusYears(shiftingYear-2000);
        return String.format("%02d", date.getYear());
    }

    public static String generateCardholder() {
        Faker faker = new Faker();
        return faker.name().fullName();
    }

    public static String generateCvc() {
        Random random = new Random();
        return String.format("%03d", random.nextInt(1000));
    }
}