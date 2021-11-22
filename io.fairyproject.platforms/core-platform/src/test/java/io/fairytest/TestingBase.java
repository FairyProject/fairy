package io.fairytest;

import io.fairytest.entity.FairyTestingPlatform;
import io.fairyproject.FairyPlatform;
import org.junit.BeforeClass;

public abstract class TestingBase {

    @BeforeClass
    public static void setup() {
        FairyPlatform fairyPlatform = new FairyTestingPlatform();
        FairyPlatform.INSTANCE = fairyPlatform;
        fairyPlatform.load();
        fairyPlatform.enable();
    }

}
