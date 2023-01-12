/*
 * MIT License
 *
 * Copyright (c) 2022 Fairy Project
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.fairyproject.mc.version;

import org.junit.jupiter.api.*;

import java.io.IOException;

public class MCVersionMappingRegistryTest {

    @Nested
    class FindMapping {

        private MCVersionMappingRegistry registry;
        private MCVersionMapping v11702;
        private MCVersionMapping v11700;
        private MCVersionMapping v11605;
        private MCVersionMapping v11600;

        @BeforeEach
        public void setup() {
            this.registry = new MCVersionMappingRegistry();
            v11702 = new MCVersionMapping(1, 17, 2, false, true, 756);
            v11700 = new MCVersionMapping(1, 17, 0, false, true, 755);
            v11605 = new MCVersionMapping(1, 16, 5, false, true, 754);
            v11600 = new MCVersionMapping(1, 16, 0, false, true, 751);
            registry.register(v11702);
            registry.register(v11700);
            registry.register(v11605);
            registry.register(v11600);
        }

        @Test
        public void correctKey_shouldReturnCorrectValue() {
            Assertions.assertEquals(v11702, registry.findMapping(1, 17, 2));
            Assertions.assertEquals(v11700, registry.findMapping(1, 17, 1));
            Assertions.assertEquals(v11700, registry.findMapping(1, 17, 0));
            Assertions.assertEquals(v11605, registry.findMapping(1, 16, 5));
            Assertions.assertEquals(v11600, registry.findMapping(1, 16, 4));
            Assertions.assertEquals(v11600, registry.findMapping(1, 16, 0));
        }

        @Test
        public void alternativeMethod_shouldReturnCorrectValue() {
            Assertions.assertEquals(v11702, registry.findMapping(MCVersion.of(1, 17, 2)));
            Assertions.assertEquals(v11700, registry.findMapping(MCVersion.of(1, 17, 1)));
            Assertions.assertEquals(v11700, registry.findMapping(MCVersion.of(1, 17, 0)));
        }

        @Test
        public void findMappingByProtocol_shouldReturnCorrectValue() {
            Assertions.assertEquals(v11702, registry.findMappingByProtocol(756));
            Assertions.assertEquals(v11700, registry.findMappingByProtocol(755));
            Assertions.assertEquals(v11605, registry.findMappingByProtocol(754));
            Assertions.assertEquals(v11600, registry.findMappingByProtocol(751));
        }

        @Test
        public void noMappingFound_shouldThrowException() {
            Assertions.assertThrows(IllegalArgumentException.class, () -> registry.findMapping(1, 15, 5));
        }

    }

    @Nested
    class LoadFromInternet {

        private MCVersionMappingRegistry registry;

        @BeforeEach
        public void setup() throws IOException {
            this.registry = new MCVersionMappingRegistry();
            this.registry.loadFromInternet();
        }

        @Test
        public void shouldLoadCorrectlyWithFewPredefinedVersions() {
            Assertions.assertEquals(760, registry.findMapping(1, 19, 2).getProtocolVersion());
            Assertions.assertEquals(754, registry.findMapping(1, 16, 5).getProtocolVersion());
            Assertions.assertEquals(735, registry.findMapping(1, 16, 0).getProtocolVersion());
            Assertions.assertEquals(340, registry.findMapping(1, 12, 2).getProtocolVersion());
            Assertions.assertEquals(47, registry.findMapping(1, 8, 9).getProtocolVersion());
        }

        @Test
        public void hexColorSupport_shouldOnlyBeAvailableAbove1_16() {
            Assertions.assertFalse(registry.findMapping(1, 15, 2).isHexColorSupport());
            Assertions.assertTrue(registry.findMapping(1, 16, 0).isHexColorSupport());
            Assertions.assertTrue(registry.findMapping(1, 17, 0).isHexColorSupport());
        }

        @Test
        public void nmsPrefix_shouldNotBeAvailableAbove1_17() {
            Assertions.assertTrue(registry.findMapping(1, 15, 2).isNmsPrefix());
            Assertions.assertTrue(registry.findMapping(1, 16, 0).isNmsPrefix());
            Assertions.assertFalse(registry.findMapping(1, 17, 0).isNmsPrefix());
        }

    }

}
