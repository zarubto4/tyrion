import org.junit.Test;

import static org.junit.Assert.*;

public class ApplicationTest {

    @Test
    public void testSum() {
        int a = 1 + 1;
        assertEquals(2, a);
    }

    @Test
    public void testString() {
        String str = "Hello world";
        assertFalse(str.isEmpty());
    }

}