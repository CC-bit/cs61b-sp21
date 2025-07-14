package flik;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class FlikTest {

    /** Test if Flik.issameNumber can handle int 128.
     * Case 1:
     * Integer a = 127;
     * Integer b = 127;
     * // 编译器实际上执行的是 Integer.valueOf(127)
     * // 两次调用都返回了同一个缓存中的对象
     * System.out.println(a == b); // true，因为 a 和 b 指向同一个内存地址
     *
     * Case 2:
     * Integer a = 128;
     * Integer b = 128;
     * // 编译器执行 Integer.valueOf(128)
     * // 因为 128 超出了缓存范围，所以每次都会创建一个全新的 Integer 对象
     * System.out.println(a == b); // false，因为 a 和 b 是两个独立的对象，内存地址不同
     */
    @Test
    public void t1() {
        int i = 128;
        int j = 128;
        assertTrue(Flik.isSameNumber(i, j));
    }
}
