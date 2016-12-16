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
import org.twowls.gatesmates.util.Gates;
import org.twowls.gatesmates.util.GatesConst;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * <p>Executes registry tests defined in {@link HighLevelRegistryTests}, in mocked environment.</p>
 *
 * @author bubo &lt;bubo@twowls.org&gt;
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Gates.AdvApi32.class})
public class MockedRegistryTests extends HighLevelRegistryTests {

    private static final int NORMAL_HANDLE_UPPER_BOUND = 10000;

    private static Set<Integer> virtualHandles = new HashSet<>();

    @Before
    public void setup() {
        Registry.forceAvailable();
        PowerMockito.mockStatic(Gates.AdvApi32.class);

        // when opening non-existent key, return not found error code
        when(Gates.AdvApi32.RegOpenKeyExA(anyInt(), eq(NON_EXISTENT_SUB_KEY), anyInt(), anyInt(), any()))
                .thenReturn(GatesConst.ERROR_NOT_FOUND);

        // when opening existent sub-key generate virtual handle and return success code
        when(Gates.AdvApi32.RegOpenKeyExA(anyInt(), eq(Registry.toWindowsPath(EXISTENT_SUB_KEY)), anyInt(), anyInt(), any()))
                .thenAnswer(invocation -> {
                    int[] data = invocation.getArgumentAt(4, int[].class);
                    while (true) {
                        int virtualHandle = new Random().nextInt(NORMAL_HANDLE_UPPER_BOUND);
                        if (virtualHandle > 0 && !virtualHandles.contains(virtualHandle)) {
                            virtualHandles.add(virtualHandle);
                            data[0] = virtualHandle;
                            break;
                        }
                    }
                    return GatesConst.ERROR_SUCCESS;
                });

        // when closing key check whether handle is correct
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

        // mock RegQueryValueExA
        when(Gates.AdvApi32.RegQueryValueExA(anyInt(), eq(""), any(), any(), any(), any()))
                .thenReturn(GatesConst.ERROR_NOT_FOUND);

    }
}
