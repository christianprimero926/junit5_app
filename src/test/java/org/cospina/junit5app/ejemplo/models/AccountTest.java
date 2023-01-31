package org.cospina.junit5app.ejemplo.models;

import org.cospina.junit5app.ejemplo.exceptions.DineroInsuficienteException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AccountTest {
    Account account;
    private TestInfo info;
    private TestReporter reporter;

    @BeforeEach
    void initMethodTest(TestInfo info, TestReporter reporter) {
        this.account = new Account("Andres", new BigDecimal("1000.12345"));
        System.out.println("iniciando el metodo");

        this.info = info;
        this.reporter = reporter;
        reporter.publishEntry("ejecutando: " + info.getDisplayName() + " " + info.getTestMethod().orElse(null).getName()
                + " con los tags " + info.getTags());
    }

    @AfterEach
    void tearDown() {
        System.out.println("finalizando el metodo de prueba");
    }

    @BeforeAll
    static void beforeAll() {
        System.out.println("inicializando el test");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("finalizando el test");
    }

    @Nested
    @Tag("account")
    @DisplayName("Test: atributos cuenta")
    class AccountTestNameValue {
        @Test
        @DisplayName("Test: nombre de la cuenta")
        void testAccountName() {
            System.out.println(info.getTags().toString());
            if (info.getTags().contains("account")) {
                reporter.publishEntry("Hacer algo mas");
            }
//        account.setPersona("Andres");
            String esperado = "Andres";
            String real = account.getPersona();
            assertAll(
                    () -> assertNotNull(real, () -> "La cuenta no puede ser nula"),
                    () -> assertEquals(esperado, real, () -> "el nombre de la cuenta no es el esperado: se esperaba " + esperado + " sin embargo fue " + real),
                    () -> assertTrue(real.equals("Andres"), () -> "El nombre de la cuenta esperada no es igual a la real"));
        }

        @Test
        @DisplayName("Test: Saldo de la cuenta")
        void testSaldoCuenta() {
            assertAll(
                    () -> assertNotNull(account.getSaldo(), () -> "el saldo no puede ser nulo"),
                    () -> assertEquals(1000.12345, account.getSaldo().doubleValue(), () -> "el valor esperado no es igual al real"),
                    () -> assertFalse(account.getSaldo().compareTo(BigDecimal.ZERO) < 0, () -> "el valor no puede ser negativo"),
                    () -> assertTrue(account.getSaldo().compareTo(BigDecimal.ZERO) > 0, () -> "el valor debe ser mayor a 0"));
        }

        @Test
        @DisplayName("Test: referencias iguales")
        void testReferenciaCuenta() {
            account = new Account("John Doe", new BigDecimal("8900.9997"));
            Account account2 = new Account("John Doe", new BigDecimal("8900.9997"));

            //assertNotEquals(account2, account);
            assertEquals(account2, account);
        }
    }


    @Nested
    class AccountOperationsTest {
        @Test
        @Tag("account")
        void testDebitAccount() {
            account.debit(new BigDecimal(100));
            assertAll(
                    () -> assertNotNull(account.getSaldo(), () -> "el saldo no puede ser nulo"),
                    () -> assertEquals(900, account.getSaldo().intValue(), () -> "el valor esperado no es igual al real"),
                    () -> assertEquals("900.12345", account.getSaldo().toPlainString(), () -> "el valor esperado no es igual al real"));
        }

        @Test
        @Tag("account")
        void testCreditAccount() {
            account.credit(new BigDecimal(100));
            assertNotNull(account.getSaldo());
            assertEquals(1100, account.getSaldo().intValue());
            assertEquals("1100.12345", account.getSaldo().toPlainString());
        }

        @Test
        @Tag("account")
        @Tag("bank")
        void testTransferMoneyAccount() {
            Account account1 = new Account("John Doe", new BigDecimal("2500"));
            Account account2 = new Account("Andres", new BigDecimal("1500.8989"));

            Bank bank = new Bank();
            bank.setName("Banco del estado");
            bank.transfer(account2, account1, new BigDecimal(500));
            assertEquals("1000.8989", account2.getSaldo().toPlainString());
            assertEquals("3000", account1.getSaldo().toPlainString());
        }
    }

    @Test
    @Tag("account")
    @Tag("error")
    void testDineroInsuficienteExceptionCuenta() {
        Exception exception = assertThrows(DineroInsuficienteException.class, () -> {
            account.debit(new BigDecimal(1500));
        });
        String actual = exception.getMessage();
        String expected = "Dinero insuficiente";
        assertEquals(expected, actual);
    }

    // @Disabled
    @Test
    @Tag("account")
    @Tag("bank")
    @DisplayName("Test: relaciones cuenta y banco con assertAll")
    void testRelationsBankAccount() {
        //       fail();
        Account account1 = new Account("John Doe", new BigDecimal("2500"));
        Account account2 = new Account("Andres", new BigDecimal("1500.8989"));

        Bank bank = new Bank();
        bank.addAccount(account1);
        bank.addAccount(account2);

        bank.setName("Banco del estado");
        bank.transfer(account2, account1, new BigDecimal(500));
        assertAll(() -> assertEquals("1000.8989", account2.getSaldo().toPlainString()),
                () -> assertEquals("3000", account1.getSaldo().toPlainString()),
                () -> assertEquals(2, bank.getAccounts().size()),
                () -> assertEquals("Banco del estado", account1.getBank().getName()),
                () -> assertEquals("Andres", bank.getAccounts().stream()
                        .filter(a -> a.getPersona().equals("Andres"))
                        .findFirst()
                        .get().getPersona()),
                () -> assertTrue(bank.getAccounts().stream()
                        .anyMatch(a -> a.getPersona().equals("Andres")))
        );
    }

    @Nested
    class OperatingSystemTest {
        @Test
        @EnabledOnOs(OS.WINDOWS)
        void testOnlyWindows() {

        }

        @Test
        @EnabledOnOs({OS.LINUX, OS.MAC})
        void testOnlyLinuxMac() {

        }

        @Test
        @DisabledOnOs(OS.WINDOWS)
        void testNoWindows() {
        }

    }

    @Nested
    class JavaVersionTest {
        @Test
        @EnabledOnJre(JRE.JAVA_8)
        void testOnlyJdk8() {
        }

        @Test
        @EnabledOnJre(JRE.JAVA_19)
        void testOnlyJdk19() {
        }

        @Test
        @DisabledOnJre(JRE.JAVA_19)
        void testNoJdk19() {
        }
    }

    @Nested
    class SystemPropertiesTest {
        @Test
        void printSystemProperties() {
            Properties properties = System.getProperties();
            properties.forEach((k, v) -> System.out.println(k + ":" + v));
        }

        @Test
        @EnabledIfSystemProperty(named = "java.version", matches = ".*19.*")
        void testJavaVersion() {
        }

        @Test
        @DisabledIfSystemProperty(named = "os.arch", matches = ".*32.*")
        void testOnly64() {
        }

        @Test
        @EnabledIfSystemProperty(named = "os.arch", matches = ".*32.*")
        void testNo64() {
        }

        @Test
        @EnabledIfSystemProperty(named = "user.name", matches = "chris")
        void testUsername() {
        }

        @Test
        @EnabledIfSystemProperty(named = "ENV", matches = "dev")
        void testDev() {
        }
    }

    @Nested
    class EnvVariableTest {
        @Test
        void printEnvProperties() {
            Map<String, String> getenv = System.getenv();
            getenv.forEach((k, v) -> System.out.println(k + " = " + v));
        }

        @Test
        @EnabledIfEnvironmentVariable(named = "JAVA_HOME", matches = ".*jdk-19.0.2.*")
        void testJavaHome() {
        }

        @Test
        @EnabledIfEnvironmentVariable(named = "NUMBER_OF_PROCESSORS", matches = "12")
        void testProcessors() {
        }

        @Test
        @EnabledIfEnvironmentVariable(named = "ENVIRONMENT", matches = "dev")
        void testEnv() {
        }

        @Test
        @DisabledIfEnvironmentVariable(named = "ENVIRONMENT", matches = "prod")
        void testEnvProd() {
        }
    }


    @Test
    @DisplayName("Test: Saldo de la cuenta en Dev")
    void testSaldoCuentaDev() {
        boolean isDev = "dev".equals(System.getProperty("ENV"));
        assumeTrue(isDev);
        assertAll(() -> assertNotNull(account.getSaldo(),
                        () -> "el saldo no puede ser nulo"),
                () -> assertEquals(1000.12345, account.getSaldo().doubleValue(),
                        () -> "el valor esperado no es igual al real"),
                () -> assertFalse(account.getSaldo().compareTo(BigDecimal.ZERO) < 0,
                        () -> "el valor no puede ser negativo"),
                () -> assertTrue(account.getSaldo().compareTo(BigDecimal.ZERO) > 0,
                        () -> "el valor debe ser mayor a 0"));
    }

    @Test
    @DisplayName("Test: Saldo de la cuenta en Dev2")
    void testSaldoCuentaDev2() {
        boolean isDev = "dev".equals(System.getProperty("ENV"));
        assumingThat(isDev, () -> {
            assertAll(() -> assertNotNull(account.getSaldo(),
                            () -> "el saldo no puede ser nulo"),
                    () -> assertEquals(1000.12345, account.getSaldo().doubleValue(),
                            () -> "el valor esperado no es igual al real"),
                    () -> assertFalse(account.getSaldo().compareTo(BigDecimal.ZERO) < 0,
                            () -> "el valor no puede ser negativo"),
                    () -> assertTrue(account.getSaldo().compareTo(BigDecimal.ZERO) > 0,
                            () -> "el valor debe ser mayor a 0"));
        });
    }

    @RepeatedTest(value = 5, name = "{displayName} - iteración {currentRepetition} de {totalRepetitions}")
    @DisplayName("Repeated Test: Saldo de la cuenta en Dev2")
    void testDebitAccountRepeated(RepetitionInfo info) {
        if (info.getCurrentRepetition() == 3) {
            System.out.println("estamos en " + info.getCurrentRepetition());
        }
        boolean isDev = "dev".equals(System.getProperty("ENV"));
        assumingThat(isDev, () -> {
            assertAll(() -> assertNotNull(account.getSaldo(),
                            () -> "el saldo no puede ser nulo"),
                    () -> assertEquals(1000.12345, account.getSaldo().doubleValue(),
                            () -> "el valor esperado no es igual al real"),
                    () -> assertFalse(account.getSaldo().compareTo(BigDecimal.ZERO) < 0,
                            () -> "el valor no puede ser negativo"),
                    () -> assertTrue(account.getSaldo().compareTo(BigDecimal.ZERO) > 0,
                            () -> "el valor debe ser mayor a 0"));
        });
    }

    @Nested
    @Tag("param")
    class ParameterizedTests {
        @ParameterizedTest(name = "numero {index} ejecutando con valor {0} - {argumentsWithNames}")
        @ValueSource(strings = {"100", "200", "300", "500", "700", "1000"})
        void testDebitAccountValueSource(String value) {
            account.debit(new BigDecimal(value));
            assertAll(
                    () -> assertNotNull(account.getSaldo(), () -> "el saldo no puede ser nulo"),
                    () -> assertTrue(account.getSaldo().compareTo(BigDecimal.ZERO) > 0, () -> "el valor esperado no es igual al real")
            );
        }

        @ParameterizedTest(name = "numero {index} ejecutando con valor {0} - {argumentsWithNames}")
        @CsvSource({"1,100", "2,200", "3,300", "5,500", "6,700", "7,1000"})
        void testDebitAccountCsvSource(String index, String value) {
            System.out.println(index + "->" + value);
            account.debit(new BigDecimal(value));
            assertAll(
                    () -> assertNotNull(account.getSaldo(), () -> "el saldo no puede ser nulo"),
                    () -> assertTrue(account.getSaldo().compareTo(BigDecimal.ZERO) > 0, () -> "el valor esperado no es igual al real")
            );
        }

        @ParameterizedTest(name = "numero {index} ejecutando con valor {0} - {argumentsWithNames}")
        @CsvSource({"200,100,John,Andres", "250,200,Pepe,Pepe", "310,300,Maria,Maria", "510,500,Pepa,Pepa", "750,700,Lucas,Lucas", "1000.12345,1000.12345,Kata,Kata"})
        void testDebitAccountCsvSource2(String saldo, String value, String expected, String actual) {
            System.out.println(saldo + "->" + value);
            account.setSaldo(new BigDecimal(saldo));
            account.setPersona(actual);
            account.debit(new BigDecimal(value));
            assertAll(
                    () -> assertNotNull(account.getPersona(), () -> "no puede ser nulo"),
                    () -> assertEquals(expected, actual, () -> "el nombre no coincide"),
                    () -> assertNotNull(account.getSaldo(), () -> "el saldo no puede ser nulo"),
                    () -> assertTrue(account.getSaldo().compareTo(BigDecimal.ZERO) > 0, () -> "el valor esperado no es mayor a 0")
            );
        }

        @ParameterizedTest(name = "numero {index} ejecutando con valor {0} - {argumentsWithNames}")
        @CsvFileSource(resources = "/data.csv")
        void testDebitAccountCsvFileSource(String value) {
            account.debit(new BigDecimal(value));
            assertAll(
                    () -> assertNotNull(account.getSaldo(), () -> "el saldo no puede ser nulo"),
                    () -> assertTrue(account.getSaldo().compareTo(BigDecimal.ZERO) > 0, () -> "el valor esperado no es mayor a 0")
            );
        }

        @ParameterizedTest(name = "numero {index} ejecutando con valor {0} - {argumentsWithNames}")
        @CsvFileSource(resources = "/data2.csv")
        void testDebitAccountCsvFileSource2(String saldo, String value, String expected, String actual) {
            account.setSaldo(new BigDecimal(saldo));
            account.setPersona(actual);
            account.debit(new BigDecimal(value));
            assertAll(
                    () -> assertNotNull(account.getPersona(), () -> "no puede ser nulo"),
                    () -> assertEquals(expected, actual, () -> "el nombre no coincide"),
                    () -> assertNotNull(account.getSaldo(), () -> "el saldo no puede ser nulo"),
                    () -> assertTrue(account.getSaldo().compareTo(BigDecimal.ZERO) > 0, () -> "el valor esperado no es mayor a 0")
            );
        }

    }

    @Tag("param")
    @ParameterizedTest(name = "numero {index} ejecutando con valor {0} - {argumentsWithNames}")
    @MethodSource("valueList")
    void testDebitAccountMethodSource(String value) {
        account.debit(new BigDecimal(value));
        assertAll(
                () -> assertNotNull(account.getSaldo(), () -> "el saldo no puede ser nulo"),
                () -> assertTrue(account.getSaldo().compareTo(BigDecimal.ZERO) > 0, () -> "el valor esperado no es igual al real")
        );
    }

    static List<String> valueList() {
        return Arrays.asList("100", "200", "300", "500", "700", "1000");
    }

    @Nested
    @Tag("timeout")
    class TimeOutTest {
        @Test
        @Timeout(1)
        void testTimeOut() throws InterruptedException {
            TimeUnit.MILLISECONDS.sleep(100);
        }

        @Test
        @Timeout(value = 1000, unit = TimeUnit.MILLISECONDS)
        void testTimeOut2() throws InterruptedException {
            TimeUnit.MILLISECONDS.sleep(900);
        }

        @Test
        void testTimeOutAssert() {
            assertTimeout(Duration.ofSeconds(5), ()->{
                TimeUnit.MILLISECONDS.sleep(4000);
            });
        }
    }
}