package org.cospina.junit5app.ejemplo.models;

import org.cospina.junit5app.ejemplo.exceptions.DineroInsuficienteException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AccountTest {
    Account account;

    @BeforeEach
    void initMethodTest() {
        this.account = new Account("Andres", new BigDecimal("1000.12345"));
        System.out.println("iniciando el metodo");
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

    @Test
    @DisplayName("Test: nombre de la cuenta")
    void testNombreCuenta() {
//        account.setPersona("Andres");
        String esperado = "Andres";
        String real = account.getPersona();
        assertAll(() -> assertNotNull(real,
                        () -> "La cuenta no puede ser nula"),
                () -> assertEquals(esperado, real,
                        () -> "el nombre de la cuenta no es el esperado: se esperaba " + esperado
                                + " sin embargo fue " + real),
                () -> assertTrue(real.equals("Andres"),
                        () -> "El nombre de la cuenta esperada no es igual a la real"));
    }

    @Test
    @DisplayName("Test: Saldo de la cuenta")
    void testSaldoCuenta() {
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
    @DisplayName("Test: referencias iguales")
    void testReferenciaCuenta() {
        account = new Account("John Doe", new BigDecimal("8900.9997"));
        Account account2 = new Account("John Doe", new BigDecimal("8900.9997"));

        //assertNotEquals(account2, account);
        assertEquals(account2, account);
    }

    @Test
    void testDebitAccount() {
        account.debit(new BigDecimal(100));
        assertAll(
                () -> assertNotNull(account.getSaldo(),
                        () -> "el saldo no puede ser nulo"),
                () -> assertEquals(900, account.getSaldo().intValue(),
                        () -> "el valor esperado no es igual al real"),
                () -> assertEquals("900.12345", account.getSaldo().toPlainString(),
                        () -> "el valor esperado no es igual al real"));
    }

    @Test
    void testCreditAccount() {
        account.credit(new BigDecimal(100));
        assertNotNull(account.getSaldo());
        assertEquals(1100, account.getSaldo().intValue());
        assertEquals("1100.12345", account.getSaldo().toPlainString());
    }

    @Test
    void testDineroInsuficienteExceptionCuenta() {
        Exception exception = assertThrows(DineroInsuficienteException.class, () -> {
            account.debit(new BigDecimal(1500));
        });
        String actual = exception.getMessage();
        String expected = "Dinero insuficiente";
        assertEquals(expected, actual);
    }

    @Test
    void testTransferMoneyAccount() {
        Account account1 = new Account("John Doe", new BigDecimal("2500"));
        Account account2 = new Account("Andres", new BigDecimal("1500.8989"));

        Bank bank = new Bank();
        bank.setName("Banco del estado");
        bank.transfer(account2, account1, new BigDecimal(500));
        assertEquals("1000.8989", account2.getSaldo().toPlainString());
        assertEquals("3000", account1.getSaldo().toPlainString());
    }

   // @Disabled
    @Test
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

    @Test
    void printEnvProperties() {
        Map<String, String> getenv = System.getenv();
        getenv.forEach((k, v) -> System.out.println( k + " = " + v));
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