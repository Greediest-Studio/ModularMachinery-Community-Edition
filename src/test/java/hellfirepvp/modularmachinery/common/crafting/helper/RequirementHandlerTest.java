package hellfirepvp.modularmachinery.common.crafting.helper;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RequirementHandlerTest {
    @Test
    void failedRecheckDoesNotChargeAndCanBeRetried() {
        Counter handler = new Counter();
        assertTrue(handler.tryHandle(4));
        assertFalse(handler.tryHandle(4));
        assertEquals(2, handler.stored);
        handler.stored += 2;
        assertTrue(handler.tryHandle(4));
        assertEquals(0, handler.stored);
        assertEquals(2, handler.commits);
    }

    private static class Counter implements IRequirementHandler<Integer> {
        int stored = 6;
        int commits;
        public CraftCheck canHandle(Integer amount) {
            return stored >= amount ? CraftCheck.success() : CraftCheck.failure("insufficient");
        }
        public void handle(Integer amount) { stored -= amount; commits++; }
    }
}
