package fdups;

import org.junit.Test;

import java.util.Optional;

import static org.apache.commons.lang3.SystemUtils.IS_OS_LINUX;
import static org.apache.commons.lang3.SystemUtils.IS_OS_MAC_OSX;
import static org.junit.Assert.assertEquals;

public class Md5SumCommandCheckerTest {

    private final Md5SumCommandChecker systemUnderTest = new Md5SumCommandChecker();

    @Test
    public void testGetBinaryName() {
        final Optional<String> binaryName = systemUnderTest.getBinaryName();

        if (IS_OS_LINUX) {
            assertEquals("md5sum", binaryName.get());
        } else if (IS_OS_MAC_OSX) {
            assertEquals("md5", binaryName.get());
        } else {
            throw new UnsupportedOperationException("You are trying to run test(s) on an unsupported operating system, disable tests at your own risks!");
        }
    }

}
