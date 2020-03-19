package ru.netology.test;

import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import ru.netology.testUtils.HomePageStart;
import ru.netology.testUtils.UtilGenerator;
import ru.netology.testUtils.UtilSQL;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestCredit {

    public static final String APPROVED = "APPROVED";
    public static final String DECLINED = "DECLINED";
    public static String approvedCardNumber = "4444444444444441";
    public static String declinedCardNumber = "4444444444444442";


    @BeforeAll
    static void setUpAll() throws SQLException {
        UtilSQL.cleanTables();
        SelenideLogger.addListener("allure", new AllureSelenide());
    }

    @AfterAll
    static void tearDownAll() {
        SelenideLogger.removeListener("allure");
    }

    @AfterEach
    void cleanTables() throws SQLException {
        UtilSQL.cleanTables();
    }

    @Test
    @DisplayName("12. Купить в кредит. Успешная оплата кредитом с разрешённой картой")
    void shouldConfirmCreditPayWithApprovedCard() {
        HomePageStart page = HomePageStart.creditPayButtonClick();
        page.inputData(UtilGenerator.getCardForPay(approvedCardNumber));
        page.continueButtonClick();
        page.checkNotificationApprovedVisible();
        assertEquals(APPROVED, UtilSQL.getOperationStatus(UtilSQL.getCreditTable()));
    }

    @Test
    @DisplayName("13. Купить в кредит. Отказ в оплате кредитом с запрещённой картой")
    void shouldNotConfirmCreditPayWithDeclinedCard() {
        HomePageStart page = HomePageStart.creditPayButtonClick();
        page.inputData(UtilGenerator.getCardForPay(declinedCardNumber));
        page.continueButtonClick();
        page.checkNotificationDeclinedVisible();
        assertEquals(DECLINED, UtilSQL.getOperationStatus(UtilSQL.getCreditTable()));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/notExistCardNumber.cvs", numLinesToSkip = 1)
    @DisplayName("14. Купить в кредит. Отказ. Номер карты валиден, но не существует в системе")
    void shouldNotConfirmCreditPayWithBadCard(String number, String message) {
        HomePageStart page = HomePageStart.creditPayButtonClick();
        page.inputData(UtilGenerator.getCardForPay(approvedCardNumber));
        page.cleanNumberAndInputNewData(number);
        page.continueButtonClick();
        page.checkNotificationDeclinedVisible();
        assertEquals(DECLINED, UtilSQL.getOperationStatus(UtilSQL.getCreditTable()), message);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/wrongCardNumber.cvs", numLinesToSkip = 1)
    @DisplayName("15. Купить в кредит. Оплата проходит после исправления номера карты на валидный")
    void shouldCreditPayProcessAfterRetypeNumCard(String number) {
        HomePageStart page = HomePageStart.creditPayButtonClick();
        page.inputData(UtilGenerator.getCardForPay(approvedCardNumber));
        page.cleanNumberAndInputNewData(number);
        page.continueButtonClick();
        page.checkNotificationDeclinedNotVisible();
        page.checkNotificationApprovedNotVisible();
        assertEquals("", UtilSQL.getOperationStatus(UtilSQL.getCreditTable()));
        page.checkCardNumberFormatErrorHave();
        page.cleanNumberAndInputNewData(approvedCardNumber);
        page.continueButtonClick();
        page.checkNotificationApprovedVisible();
        assertEquals(APPROVED, UtilSQL.getOperationStatus(UtilSQL.getCreditTable()));
        page.checkCardNumberFormatErrorNotHave();
    }

    @Test
    @DisplayName("16. Купить в кредит. Оплата не происходит. Карта просрочена на месяц")
    void shouldNotCreditPayProcessWithOverdueMonth() {
        HomePageStart page = HomePageStart.creditPayButtonClick();
        page.inputData(UtilGenerator.getCardForPay(approvedCardNumber));
        page.cleanMonthAndInputNewData(UtilGenerator.getShiftedMMFromCurrent(-1));
        page.cleanYearAndInputNewData(UtilGenerator.getShiftedYYFromCurrentByMonth(-1));
        page.continueButtonClick();
        page.checkNotificationDeclinedNotVisible();
        page.checkNotificationApprovedNotVisible();
        assertEquals("", UtilSQL.getOperationStatus(UtilSQL.getCreditTable()));
        page.checkDateErrorHave();
    }

    @Test
    @DisplayName("17. Купить в кредит. Оплата не происходит. Карта просрочена на год")
    void shouldNotCreditPayProcessWithOverdueYear() {
        HomePageStart page = HomePageStart.creditPayButtonClick();
        page.inputData(UtilGenerator.getCardForPay(approvedCardNumber));
        page.cleanMonthAndInputNewData(UtilGenerator.getShiftedMMFromCurrent(0));
        page.cleanYearAndInputNewData(UtilGenerator.getShiftedYYFromCurrent(-1));
        page.continueButtonClick();
        page.checkNotificationDeclinedNotVisible();
        page.checkNotificationApprovedNotVisible();
        assertEquals("", UtilSQL.getOperationStatus(UtilSQL.getCreditTable()));
        page.checkDateErrorHave();
    }

    @Test
    @DisplayName("18. Купить в кредит. Оплата не происходит. Срок действия карты больше 5 лет")
    void shouldNotCreditPayProcessWithOverdueYearMore5FromCurrent() {
        HomePageStart page = HomePageStart.creditPayButtonClick();
        page.inputData(UtilGenerator.getCardForPay(approvedCardNumber));
        page.cleanMonthAndInputNewData(UtilGenerator.getShiftedMMFromCurrent(0));
        page.cleanYearAndInputNewData(UtilGenerator.getShiftedYYFromCurrent(6));
        page.continueButtonClick();
        page.checkNotificationDeclinedNotVisible();
        page.checkNotificationApprovedNotVisible();
        assertEquals("", UtilSQL.getOperationStatus(UtilSQL.getPayTable()));
        page.checkYearErrorHave();
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/wrongMonth.cvs", numLinesToSkip = 1)
    @DisplayName("19. Купить в кредит. Оплата проходит после исправления месяца")
    void shouldCreditPayProcessWithCorrectionWrongMonth(String month, String message) {
        HomePageStart page = HomePageStart.creditPayButtonClick();
        page.inputData(UtilGenerator.getCardForPay(approvedCardNumber));
        page.cleanMonthAndInputNewData(month);
        page.continueButtonClick();
        page.checkMonthDateErrorHave();
        page.checkNotificationDeclinedNotVisible();
        page.checkNotificationApprovedNotVisible();
        assertEquals("", UtilSQL.getOperationStatus(UtilSQL.getCreditTable()), message);
        page.cleanMonthAndInputNewData("12");
        page.continueButtonClick();
        page.checkNotificationApprovedVisible();
        page.checkMonthDateErrorNotHave();
        assertEquals(APPROVED, UtilSQL.getOperationStatus(UtilSQL.getCreditTable()));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/wrongYear.cvs", numLinesToSkip = 1)
    @DisplayName("20. Купить в кредит. Оплата проходит после исправления года")
    void shouldCreditPayProcessWithCorrectionWrongYear(String year, String message) {
        HomePageStart page = HomePageStart.creditPayButtonClick();
        page.inputData(UtilGenerator.getCardForPay(approvedCardNumber));
        page.cleanYearAndInputNewData(year);
        page.continueButtonClick();
        page.checkYearFormatErrorHave();
        page.checkNotificationDeclinedNotVisible();
        page.checkNotificationApprovedNotVisible();
        assertEquals("", UtilSQL.getOperationStatus(UtilSQL.getCreditTable()), message);
        page.cleanYearAndInputNewData(UtilGenerator.getShiftedYYFromCurrent(2));
        page.continueButtonClick();
        page.checkNotificationApprovedVisible();
        page.checkYearFormatErrorNotHave();
        assertEquals(APPROVED, UtilSQL.getOperationStatus(UtilSQL.getCreditTable()));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/wrongCardholderName.cvs", numLinesToSkip = 1)
    @DisplayName("21. Купить в кредит. Купить. Оплата происходит после исправления имени владельца")
    void shouldCreditPayProcessWithCorrectedCardholderName(String owner, String message) {
        HomePageStart page = HomePageStart.creditPayButtonClick();
        page.inputData(UtilGenerator.getCardForPay(approvedCardNumber));
        page.cleanOwnerAndInputNewData(owner);
        page.continueButtonClick();
        page.checkNotificationDeclinedNotVisible();
        page.checkNotificationApprovedNotVisible();
        assertEquals("", UtilSQL.getOperationStatus(UtilSQL.getCreditTable()), message);
        page.checkOwnerFormatErrorHave();
        page.cleanOwnerAndInputNewData(UtilGenerator.generateCardholder());
        page.continueButtonClick();
        page.checkNotificationApprovedVisible();
        assertEquals(APPROVED, UtilSQL.getOperationStatus(UtilSQL.getCreditTable()));
        page.checkOwnerFormatErrorNotHave();
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/wrongCVV.cvs", numLinesToSkip = 1)
    @DisplayName("22. Купить в кредит. Оплата происходит после исправления CVC/CVV")
    void shouldCreditPayProcessWithCorrectedCvc(String cvv, String message) {
        HomePageStart page = HomePageStart.creditPayButtonClick();
        page.inputData(UtilGenerator.getCardForPay(approvedCardNumber));
        page.cleanCvcAndInputNewData(cvv);
        page.continueButtonClick();
        page.checkNotificationDeclinedNotVisible();
        page.checkNotificationApprovedNotVisible();
        assertEquals("", UtilSQL.getOperationStatus(UtilSQL.getCreditTable()), message);
        page.checkCvcErrorHave();
        page.cleanCvcAndInputNewData(UtilGenerator.generateCvc());
        page.continueButtonClick();
        page.checkNotificationApprovedVisible();
        assertEquals(APPROVED, UtilSQL.getOperationStatus(UtilSQL.getCreditTable()));
        page.checkCvcErrorNotHave();
    }
}