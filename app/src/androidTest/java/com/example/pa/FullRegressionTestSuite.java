package com.example.pa;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        UserTest.class,
        ExampleInstrumentedTest.class

})
public class FullRegressionTestSuite {
    // 这个类只是一个容器，不需要内容
}