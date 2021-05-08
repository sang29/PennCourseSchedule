import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;

public class DatabaseTest {

    Database db;
    
    @Before
    public void setUp() throws Exception {
        db.openClient();
    }

    @Test
    public void test() {
        fail("Not yet implemented");
    }

    
    @After
    public void breakDown() throws Exception {
        db.closeClient();
    }
    
}
