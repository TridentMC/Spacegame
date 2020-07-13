package com.tridevmc.spacegame.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestCharQueue {
    private final CharQueue _queue = new CharQueue();

    @Test
    void enqueueDequeue() throws OverCapacityException {
        _queue.enqueue('\uFF00');
        Assertions.assertEquals('\uFF00', _queue.dequeue());
    }

    @Test
    void multipleQueues() throws OverCapacityException {
        _queue.enqueue('\u0100');
        _queue.enqueue('\uDEAD');
        _queue.enqueue('\uBEEF');
        Assertions.assertEquals('\u0100', _queue.dequeue());
        _queue.enqueue('\uCAFE');
        Assertions.assertEquals('\uDEAD', _queue.dequeue());
        Assertions.assertEquals('\uBEEF', _queue.dequeue());
        Assertions.assertEquals('\uCAFE', _queue.dequeue());
        Assertions.assertThrows(RuntimeException.class, () -> _queue.dequeue());
    }

    @Test
    void maxQueue() throws OverCapacityException {
        for(int i = 0;i < 256;i++) {
            _queue.enqueue((char)i);
        }
        for(int j = 0;j < 256;j++) {
            Assertions.assertEquals((char)j, _queue.dequeue());
        }
        _queue.enqueue('\uDEAD');
        Assertions.assertEquals('\uDEAD', _queue.dequeue());
        Assertions.assertThrows(RuntimeException.class, () -> _queue.dequeue());
    }

    @Test
    void emptyDequeueFails() {
        Assertions.assertThrows(RuntimeException.class, () -> _queue.dequeue());
    }

    @Test
    void fullEnqueueFails() throws OverCapacityException {
        for(int i = 0;i < 256;i++) {
            _queue.enqueue((char)i);
        }
        Assertions.assertThrows(RuntimeException.class, () -> _queue.enqueue('\uFF00'));
    }
    
}
