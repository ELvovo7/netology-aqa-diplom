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

public class TestDebit {
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
    @DisplayName("1. Купить. Успешная оплата разрешённой картой")
    void shouldConfirmPayWithApprovedCard() {
        HomePageStart page = HomePageStart.payButtonClick();
        page.inputData(UtilGenerator.getCardForPay(approvedCardNumber));
        page.continueButtonClick();
        page.checkNotificationApprovedVisible();
        assertEquals(APPROVED, UtilSQL.getOperationStatus(UtilSQL.getPayTable()));
    }

    @Test
    @DisplayName("2. Купить. Отказ в оплате запрещённой картой")
    void shouldNotConfirmPayWithDeclinedCard() {
        HomePageStart page = HomePageStart.payButtonClick();
        page.inputData(UtilGenerator.getCardForPay(declinedCardNumber));
        page.continueButtonClick();
        page.checkNotificationDeclinedVisible();
        assertEquals(DECLINED, UtilSQL.getOperationStatus(UtilSQL.getPayTable()));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/notExistCardNumber.cvs", numLinesToSkip = 1)
    @DisplayName("3. Купить. Отказ. Номер карты валиден, но не существует в системе")
    void shouldNotConfirmPayWithBadCard(String number, String message) {
        HomePageStart page = HomePageStart.payButtonClick();
        page.inputData(UtilGenerator.getCardForPay(approvedCardNumber));
        page.cleanNumberAndInputNewData(number);
        page.continueButtonClick();
        page.checkNotificationDeclinedVisible();
        assertEquals(DECLINED, UtilSQL.getOperationStatus(UtilSQL.getPayTable()), message);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/wrongCardNumber.cvs", numLinesToSkip = 1)
    @DisplayName("4. Купить. Оплата проходит после исправления номера карты на валидный")
    void shouldPayProcessAfterRetypeNumCard(String number, String message) {
        HomePageStart page = HomePageStart.payButtonClick();
        page.inputData(UtilGenerator.getCardForPay(approvedCardNumber));
        page.cleanNumberAndInputNewData(number);
        page.continueButtonClick();
        page.checkNotificationDeclinedNotVisible();
        page.checkNotificationApprovedNotVisible();
        assertEquals("", UtilSQL.getOperationStatus(UtilSQL.getPayTable()), message);
        page.checkCardNumberFormatErrorHave();
        page.cleanNumberAndInputNewData(approvedCardNumber);
        page.continueButtonClick();
        page.checkNotificationApprovedVisible();
        assertEquals(APPROVED, UtilSQL.getOperationStatus(UtilSQL.getPayTable()));
        page.checkCardNumberFormatErrorNotHave();
    }

    @Test
    @DisplayName("5. Купить. Оплата не происходит. Карта просрочена на месяц")
    void shouldNotPayProcessWithOverdueMonth() {
        HomePageStart page = HomePageStart.payButtonClick();
        page.inputData(UtilGenerator.getCardForPay(approvedCardNumber));
        page.cleanMonthAndInputNewData(UtilGenerator.getShiftedMMFromCurrent(-1));
        page.cleanYearAndInputNewData(UtilGenerator.getShiftedYYFromCurrentByMonth(-1));
        page.continueButtonClick();
        page.checkNotificationDeclinedNotVisible();
        page.checkNotificationApprovedNotVisible();
        assertEquals("", UtilSQL.getOperationStatus(UtilSQL.getPayTable()));
        page.checkDateErrorHave();
    }

    @Test
    @DisplayName("6. Купить. Оплата не происходит. Карта просрочена на год")
    void shouldNotPayProcessWithOverdueYear() {
        HomePageStart page = HomePageStart.payButtonClick();
        page.inputData(UtilGenerator.getCardForPay(approvedCardNumber));
        page.cleanMonthAndInputNewData(UtilGenerator.getShiftedMMFromCurrent(0));
        page.cleanYearAndInputNewData(UtilGenerator.getShiftedYYFromCurrent(-1));
        page.continueButtonClick();
        page.checkNotificationDeclinedNotVisible();
        page.checkNotificationApprovedNotVisible();
        assertEquals("", UtilSQL.getOperationStatus(UtilSQL.getPayTable()));
        page.checkDateErrorHave();
    }

    @Test
    @DisplayName("7. Купить. Оплата не происходит. Срок действия карты больше 5 лет")
    void shouldNotPayProcessWithYearMore5FromCurrent() {
        HomePageStart page = HomePageStart.payButtonClick();
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
    @DisplayName("8. Купить. Оплата проходит после исправления месяца")
    void shouldPayProcessWithCorrectionWrongMonth(String month, String message) {
        HomePageStart page = HomePageStart.payButtonClick();
        page.inputData(UtilGenerator.getCardForPay(approvedCardNumber));
        page.cleanMonthAndInputNewData(month);
        page.continueButtonClick();
        page.checkMonthDateErrorHave();
        page.checkNotificationDeclinedNotVisible();
        page.checkNotificationApprovedNotVisible();
        assertEquals("", UtilSQL.getOperationStatus(UtilSQL.getPayTable()), message);
        page.cleanMonthAndInputNewData("12");
        page.continueButtonClick();
        page.checkNotificationApprovedVisible();
        page.checkMonthDateErrorNotHave();
        assertEquals(APPROVED, UtilSQL.getOperationStatus(UtilSQL.getPayTable()));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/wrongYear.cvs", numLinesToSkip = 1)
    @DisplayName("9. Купить. Оплата проходит после исправления года")
    void shouldPayProcessWithCorrectionWrongYear(String year, String message) {
        HomePageStart page = HomePageStart.payButtonClick();
        page.inputData(UtilGenerator.getCardForPay(approvedCardNumber));
        page.cleanYearAndInputNewData(year);
        page.continueButtonClick();
        page.checkYearFormatErrorHave();
        page.checkNotificationDeclinedNotVisible();
        page.checkNotificationApprovedNotVisible();
        assertEquals("", UtilSQL.getOperationStatus(UtilSQL.getPayTable()), message);
        page.cleanYearAndInputNewData(UtilGenerator.getShiftedYYFromCurrent(2));
        page.continueButtonClick();
        page.checkNotificationApprovedVisible();
        page.checkYearFormatErrorNotHave();
        assertEquals(APPROVED, UtilSQL.getOperationStatus(UtilSQL.getPayTable()));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/wrongCardholderName.cvs", numLinesToSkip = 1)
    @DisplayName("10. Купить. Оплата происходит после исправления имени владельца")
    void shouldPayProcessWithCorrectedCardholderName(String owner) {
        HomePageStart page = HomePageStart.payButtonClick();
        page.inputData(UtilGenerator.getCardForPay(approvedCardNumber));
        page.cleanOwnerAndInputNewData(owner);
        page.continueButtonClick();
        page.checkNotificationDeclinedNotVisible();
        page.checkNotificationApprovedNotVisible();
        assertEquals("", UtilSQL.getOperationStatus(UtilSQL.getPayTable()));
        page.checkOwnerFormatErrorHave();
        page.cleanOwnerAndInputNewData(UtilGenerator.generateCardholder());
        page.continueButtonClick();
        page.checkNotificationApprovedVisible();
        assertEquals(APPROVED, UtilSQL.getOperationStatus(UtilSQL.getPayTable()));
        page.checkOwnerFormatErrorNotHave();
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/wrongCVV.cvs", numLinesToSkip = 1)
    @DisplayName("11. Купить. Оплата происходит после исправления CVC/CVV")
    void shouldPayProcessWithCorrectedCvc(String cvv, String message) {
        HomePageStart page = HomePageStart.payButtonClick();
        page.inputData(UtilGenerator.getCardForPay(approvedCardNumber));
        page.cleanCvcAndInputNewData(cvv);
        page.continueButtonClick();
        page.checkNotificationDeclinedNotVisible();
        page.checkNotificationApprovedNotVisible();
        assertEquals("", UtilSQL.getOperationStatus(UtilSQL.getPayTable()), message);
        page.checkCvcErrorHave();
        page.cleanCvcAndInputNewData(UtilGenerator.generateCvc());
        page.continueButtonClick();
        page.checkNotificationApprovedVisible();
        assertEquals(APPROVED, UtilSQL.getOperationStatus(UtilSQL.getPayTable()));
        page.checkCvcErrorNotHave();
    }
}