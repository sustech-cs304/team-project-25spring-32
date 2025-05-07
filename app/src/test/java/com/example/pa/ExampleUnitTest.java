package com.example.pa;

import static com.example.pa.util.PasswordUtil.sha256;

import org.junit.Test;

import static org.junit.Assert.*;

import com.example.pa.data.Daos.*;

import java.security.NoSuchAlgorithmException;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void subtraction_isCorrect() {
        assertEquals(2, 4 - 2);
    }

    @Test
    public void test_hash_password() throws NoSuchAlgorithmException {
        assertEquals("2f75ad19416b54cdf23bf37d58650b4414ce75a6a3694b18ad703ef682775cef", sha256("ghj"));
        assertEquals("1a946a41ee4d54d8af5c2044a9fbb0adab51c44be38759bc5fd7d5083b61e3af", sha256("7hua931"));
    }

}