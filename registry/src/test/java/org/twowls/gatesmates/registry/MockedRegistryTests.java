/*
 * Copyright (c) 2016 Twowls.org.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.twowls.gatesmates.registry;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.twowls.gatesmates.registry.apitest.RegistryTests;
import org.twowls.gatesmates.util.Gates;
import org.twowls.gatesmates.util.GatesConst;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * <p>Executes registry tests defined in {@link RegistryTests}, in mocked environment.</p>
 *
 * @author bubo &lt;bubo@twowls.org&gt;
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Gates.AdvApi32.class})
public class MockedRegistryTests extends RegistryTests {

    private static final int NORMAL_HANDLE_UPPER_BOUND = 10000;
    private static final int DWORD_PROPERTY_SIZE_IN_BYTES = 4;

    private static Set<Integer> virtualHandles = new HashSet<>();

    @Before
    public void setup() {
        Registry.forceAvailable();
        PowerMockito.mockStatic(Gates.AdvApi32.class);

        // create mocks for native methods
        mockOpenKey(NON_EXISTENT_SUB_KEY, GatesConst.ERROR_NOT_FOUND);
        mockOpenKey(EXISTENT_SUB_KEY, GatesConst.ERROR_SUCCESS);
        mockOpenKey(EXISTENT_SUB_SUB_KEY, GatesConst.ERROR_SUCCESS);
        mockQueryStringValue(UNNAMED_PROPERTY, UNNAMED_PROPERTY_VALUE, GatesConst.ERROR_SUCCESS);
        mockQueryStringValue(NAMED_STRING_PROPERTY, NAMED_STRING_PROPERTY_VALUE, GatesConst.ERROR_SUCCESS);
        mockQueryIntValue(NAMED_DWORD_PROPERTY, NAMED_DWORD_PROPERTY_VALUE, GatesConst.ERROR_SUCCESS);
        mockCloseKey();
    }

    private static void mockOpenKey(final String subKey, final int retCode) {
        when(Gates.AdvApi32.RegOpenKeyExA(anyInt(), eq(Registry.toWindowsPath(subKey)), anyInt(), anyInt(), any()))
                .then(invocation -> {
                    if (GatesConst.ERROR_SUCCESS == retCode) {
                        int[] data = invocation.getArgumentAt(4, int[].class);
                        while (true) {
                            int virtualHandle = new Random().nextInt(NORMAL_HANDLE_UPPER_BOUND);
                            if (virtualHandle > 0 && !virtualHandles.contains(virtualHandle)) {
                                virtualHandles.add(virtualHandle);
                                data[0] = virtualHandle;
                                break;
                            }
                        }
                    }
                    return retCode;
                });
    }

    private static void mockQueryStringValue(final String valueName, final String value, final int retCode) {
        // call without buffer for obtaining required buffer size
        when(Gates.AdvApi32.RegQueryValueExA(anyInt(), eq(valueName), any(), any(), isNull(byte[].class), any()))
                .thenAnswer(invocation -> {
                    if (GatesConst.ERROR_SUCCESS == retCode) {
                        int[] out = invocation.getArgumentAt(3, int[].class);
                        out[0] = RegistryConst.REG_SZ;
                        out = invocation.getArgumentAt(5, int[].class);
                        out[0] = value.length() + 1;
                    }
                    return retCode;
                });

        // call with buffer for getting actual value data
        when(Gates.AdvApi32.RegQueryValueExA(anyInt(), eq(valueName), any(), any(), isNotNull(byte[].class), any()))
                .thenAnswer(invocation -> {
                    if (GatesConst.ERROR_SUCCESS == retCode) {
                        int[] out = invocation.getArgumentAt(3, int[].class);
                        out[0] = RegistryConst.REG_SZ;

                        out = invocation.getArgumentAt(5, int[].class);
                        if (out[0] < value.length() + 1) {
                            return GatesConst.ERROR_MORE_DATA;
                        }

                        byte[] data = invocation.getArgumentAt(4, byte[].class);
                        System.arraycopy(value.getBytes(), 0, data, 0, value.length());
                        data[value.length()] = 0;
                    }
                    return retCode;
                });
    }

    private static void mockQueryIntValue(final String valueName, final int value, final int retCode) {
        // call without buffer for obtaining required buffer size
        when(Gates.AdvApi32.RegQueryValueExA(anyInt(), eq(valueName), any(), any(), isNull(byte[].class), any()))
                .thenAnswer(invocation -> {
                    if (GatesConst.ERROR_SUCCESS == retCode) {
                        int[] out = invocation.getArgumentAt(3, int[].class);
                        out[0] = RegistryConst.REG_DWORD;
                        out = invocation.getArgumentAt(5, int[].class);
                        out[0] = DWORD_PROPERTY_SIZE_IN_BYTES;
                    }
                    return retCode;
                });

        // call with buffer for getting actual value data
        when(Gates.AdvApi32.RegQueryValueExA(anyInt(), eq(valueName), any(), any(), isNotNull(byte[].class), any()))
                .thenAnswer(invocation -> {
                    if (GatesConst.ERROR_SUCCESS == retCode) {
                        int[] out = invocation.getArgumentAt(3, int[].class);
                        out[0] = RegistryConst.REG_DWORD;

                        out = invocation.getArgumentAt(5, int[].class);
                        if (out[0] < DWORD_PROPERTY_SIZE_IN_BYTES) {
                            return GatesConst.ERROR_MORE_DATA;
                        }
                        byte[] data = invocation.getArgumentAt(4, byte[].class);
                        System.arraycopy(ByteBuffer.allocate(DWORD_PROPERTY_SIZE_IN_BYTES)
                                        .order(ByteOrder.LITTLE_ENDIAN).putInt(value).array(),
                                0, data, 0, DWORD_PROPERTY_SIZE_IN_BYTES);
                    }
                    return retCode;
                });
    }

    private static void mockCloseKey() {
        when(Gates.AdvApi32.RegCloseKey(anyInt()))
                .thenAnswer(invocation -> {
                    int handleToClose = invocation.getArgumentAt(0, Integer.class);
                    if (!virtualHandles.contains(handleToClose)) {
                        return GatesConst.ERROR_INVALID_HANDLE;
                    } else {
                        virtualHandles.remove(handleToClose);
                        return GatesConst.ERROR_SUCCESS;
                    }
                });
    }
}
