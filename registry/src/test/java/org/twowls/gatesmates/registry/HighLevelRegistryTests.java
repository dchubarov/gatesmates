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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * <p>High level registry tests.</p>
 *
 * @author bubo &lt;bubo@twowls.org&gt;
 */
public abstract class HighLevelRegistryTests {

    static final String EXISTENT_SUB_KEY = "Windows/CurrentVersion";
    static final String NON_EXISTENT_SUB_KEY = "$$NON-EXISTENT$$";

    @Test
    public void openNonExistentKeyShouldThrowNotFoundError() {
        int errorCode = 0;
        try {
            Registry.openKey(Registry.KEY_CURRENT_USER, NON_EXISTENT_SUB_KEY);
        } catch (RegistryException e) {
            e.printStackTrace(System.err);
            errorCode = e.getErrorCode();
        }
        assertEquals(RegistryConst.ERROR_NOT_FOUND, errorCode);
    }

    @Test
    public void openKeyShouldReturnHandle() {
        try (Registry.Key key = Registry.openKey(Registry.KEY_CURRENT_USER, EXISTENT_SUB_KEY)) {
            System.out.println(key.toString());
        } catch (RegistryException e) {
            e.printStackTrace(System.err);
        }
    }
}
