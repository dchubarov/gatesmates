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

package org.twowls.gatesmates.registry.publictests;

import org.junit.Before;
import org.junit.Test;
import org.twowls.gatesmates.registry.Registry;
import org.twowls.gatesmates.registry.RegistryException;
import org.twowls.gatesmates.util.Gates;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeFalse;

/**
 * <p>Tests for situations when registry is not available (e.g. system is not Windows).</p>
 *
 * @author bubo &lt;bubo@twowls.org&gt;
 */
public class RegistryUnavailableTests {

    @Before
    public void assumeUnavailable() {
        assumeFalse(Gates.isAvailable());
    }

    @Test
    public void methodInvocationShouldThrowUnavailable() {
        int errorCode = 0;
        try {
            Registry.openKey(Registry.KEY_CURRENT_USER, "Windows/CurrentVersion");
        } catch (RegistryException e) {
            errorCode = e.getErrorCode();
        }
        assertEquals(RegistryException.UNAVAILABLE, errorCode);
    }
}
