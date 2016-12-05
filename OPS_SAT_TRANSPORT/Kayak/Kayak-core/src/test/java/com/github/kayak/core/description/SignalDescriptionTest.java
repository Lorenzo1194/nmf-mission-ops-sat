/**
 * 	This file is part of Kayak.
 *
 *	Kayak is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU Lesser General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	Kayak is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU Lesser General Public License
 *	along with Kayak.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.github.kayak.core.description;

import java.util.Set;
import com.github.kayak.core.Util;
import com.github.kayak.core.Frame;
import java.nio.ByteOrder;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jan-Niklas Meier < dschanoeh@googlemail.com >
 */
public class SignalDescriptionTest {

    private static SignalDescription description;
    private static SignalDescription description2;
    private static SignalDescription description3;
    private static SignalDescription description4;
    private static SignalDescription description5;
    private static Frame frame;
    private static Signal data, data2, data3, data4, data5;
    private static Label label;


    public SignalDescriptionTest() {
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        MessageDescription m = new MessageDescription(42, false);

        description = new SignalDescription(m);
        description.setType(SignalDescription.Type.UNSIGNED);
        description.setNotes("This is a test signal");
        description.setUnit("0.1 km/h");
        description.setName("Vehicle speed");
        description.setLength(12);
        description.setOffset(3);
        description.setIntercept(0);
        description.setSlope(1);

        description2 = new SignalDescription(m);
        description2.setType(SignalDescription.Type.SIGNED);
        description2.setLength(8);
        description2.setOffset(0);
        description2.setIntercept(0);
        description2.setSlope(1);
        label = new Label(168, "foo");
        description2.addLabel(label);

        description3 = new SignalDescription(m);
        description3.setType(SignalDescription.Type.SIGNED);
        description3.setLength(7);
        description3.setOffset(4);
        description3.setIntercept(0);
        description3.setSlope(1);

        description4 = new SignalDescription(m);
        description4.setType(SignalDescription.Type.SIGNED);
        description4.setByteOrder(ByteOrder.BIG_ENDIAN);
        description4.setLength(12);
        description4.setOffset(3);
        description4.setIntercept(0);
        description4.setSlope(1);

        description5 = new SignalDescription(m);
        description5.setType(SignalDescription.Type.SIGNED);
        description5.setByteOrder(ByteOrder.BIG_ENDIAN);
        description5.setLength(6);
        description5.setOffset(15);
        description5.setIntercept(0);
        description5.setSlope(1);

        frame = new Frame(0x12, false, Util.hexStringToByteArray("A8B37C"));

        data = description.decodeData(frame.getData());
        assertNotNull(data);

        data2 = description2.decodeData(frame.getData());
        assertNotNull(data2);

        data3 = description3.decodeData(frame.getData());
        assertNotNull(data2);

        data4 = description4.decodeData(frame.getData());
        assertNotNull(data2);

        data5 = description5.decodeData(frame.getData());
        assertNotNull(data2);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testValue() {

        System.out.println("Testing value 1");
        double expResult = 1653;
        double result = data.getValue();
        assertEquals(expResult, result, 0.1);
    }

    @Test
    public void testUnit() {
        System.out.println("Testing unit 1");
        String expResult = "0.1 km/h";
        String result = data.getUnit();
        assertEquals(expResult, result);
    }

    @Test
    public void testRawValue() {
        System.out.println("Testing raw value 1");
        long expResult = 1653;
        long result = data.getRawValue();
        assertEquals(expResult, result);
    }

    @Test
    public void testRawValue2() {
        System.out.println("Testing raw value 2");
        long expResult = 168;
        long result = data2.getRawValue();
        assertEquals(expResult, result);
    }

    @Test
    public void testIntegerValue() {
        System.out.println("Testing value 2");
        String expResult = "-88";
        String result = data2.getIntegerString();
        assertEquals(expResult, result);
    }

    @Test
    public void testValue4() {
        System.out.println("Testing value 4");
        double expResult = 1113;
        double result = data4.getValue();
        assertEquals(expResult, result, 0.1);
    }

    @Test
    public void testValue5() {
        System.out.println("Testing value 5");
        double expResult = -17;
        double result = data5.getValue();
        assertEquals(expResult, result, 0.1);
    }

    @Test
    public void labelTest() {
        System.out.println("Testing label");
        Set<Label> labels = description2.getAllLabels();
        assertNotNull(labels);
        assertEquals(1, labels.size());

        Set<String> l = data2.getLabels();
        assertNotNull(l);
        assertEquals(1, l.size());
        for(String label : l) {
            assertEquals(label, "foo");
        }

        assertEquals("foo", data2.getReadableString());
    }

    public void lengthTest() {
        System.out.println("Testing length");
        boolean ret=false;
        byte[] data1 = new byte[] {0x00 };
        try {
            description2.decodeData(data1);
        } catch (DescriptionException ex) {
            ret = true;
        }

        assertFalse(ret);

        try {
            description3.decodeData(data1);
        } catch (DescriptionException ex) {
            ret = true;
        }

        assertTrue(ret);
    }
}