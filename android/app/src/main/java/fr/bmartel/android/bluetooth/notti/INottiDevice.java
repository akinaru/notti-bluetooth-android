/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Bertrand Martel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fr.bmartel.android.bluetooth.notti;

import fr.bmartel.android.bluetooth.listener.IPushListener;

/**
 * Generic interface for Notti device
 */
public interface INottiDevice {

    /**
     * switch notti ON/OFF
     *
     * @param ledState
     */
    public void setOnOff(boolean ledState,IPushListener listener);

    /**
     * Set RGB color
     *
     * @param red
     * @param green
     * @param blue
     */
    public void setRGBColor(int red,int green,int blue,IPushListener listener);

    /**
     * Set intensity for a specific color
     *
     * @param value
     *      0-100 %
     * @param red
     * @param green
     * @param blue
     */
    public void setLuminosityForColor(int value,int red,int green,int blue,IPushListener listener);
}