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

package org.twowls.gatesmates.util;

import com.sun.jna.Native;
import com.sun.jna.Platform;

/**
 * <p>System APIs</p>
 *
 * @author bubo &lt;bubo@twowls.org&gt;
 */
public class Gates {

    public static boolean isAvailable() {
        return Platform.isWindows();
    }

    public static class AdvApi32 {

        public static native int RegOpenKeyExA(int handle, String path, int options, int access, int[] result);

        public static native int RegQueryValueExA(int handle, String value, int[] ignore, int[] type, byte[] data, int[] size);

        public static native int RegCloseKey(int handle);

        static {
            if (isAvailable()) {
                Native.register(AdvApi32.class.getSimpleName());
            }
        }
    }
}
