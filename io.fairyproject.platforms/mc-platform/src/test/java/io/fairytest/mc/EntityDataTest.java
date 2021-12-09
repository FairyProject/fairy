package io.fairytest.mc;

import io.fairyproject.mc.entity.EntityData;
import io.fairyproject.mc.entity.EntityDataSerializer;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

public class EntityDataTest {

    @Test
    public void basicOperation() {
        EntityData entityData = EntityData.createImpl();
        EntityData.Item<Double> item = entityData.define(2, EntityDataSerializer.DOUBLE);
        item.setObject(20.0D);

        // test empty
        Assert.assertTrue(entityData.isEmpty());

        // test adding
        Assert.assertTrue(entityData.add(item));
        Assert.assertEquals(item, entityData.get(2));

        // test non-empty
        Assert.assertFalse(entityData.isEmpty());

        // test not exists index
        Assert.assertNull(entityData.get(1));

        // test all
        Set<Pair<Integer, EntityData.Item>> all = entityData.all();
        Set<Pair<Integer, EntityData.Item>> expected = Collections.singleton(Pair.of(2, item));
        Assert.assertEquals(expected, all);

        // test removing
        Assert.assertEquals(item, entityData.remove(2));
        Assert.assertNull(entityData.get(2));

        // test clear
        entityData.add(item);
        entityData.clear();
        Assert.assertTrue(entityData.isEmpty());
    }

}
