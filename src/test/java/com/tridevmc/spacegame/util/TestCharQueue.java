package com.tridevmc.spacegame.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestCharQueue {
    private final CharQueue _queue = new CharQueue();

    @Test
    void enqueueDequeue() {
        _queue.add('\uFF00');
        Assertions.assertEquals('\uFF00', _queue.remove());
    }

    @Test
    void multipleQueues() {
        _queue.add('\u0100');
        _queue.add('\uDEAD');
        _queue.add('\uBEEF');
        Assertions.assertEquals('\u0100', _queue.remove());
        _queue.add('\uCAFE');
        Assertions.assertEquals('\uDEAD', _queue.remove());
        Assertions.assertEquals('\uBEEF', _queue.remove());
        Assertions.assertEquals('\uCAFE', _queue.remove());
        Assertions.assertThrows(RuntimeException.class, _queue::remove);
    }

    @Test
    void maxQueue() {
        for(int i = 0;i < 256;i++) {
            _queue.add((char)i);
        }
        for(int j = 0;j < 256;j++) {
            Assertions.assertEquals((char)j, _queue.remove());
        }
        _queue.add('\uDEAD');
        Assertions.assertEquals('\uDEAD', _queue.remove());
        Assertions.assertThrows(RuntimeException.class, _queue::remove);
    }

    @Test
    void emptyDequeueFails() {
        Assertions.assertThrows(RuntimeException.class, _queue::remove);
    }

    @Test
    void fullEnqueueFails() {
        for(int i = 0;i < 256;i++) {
            _queue.add((char)i);
        }
        Assertions.assertThrows(RuntimeException.class, () -> _queue.add('\uFF00'));
    }
    
}
