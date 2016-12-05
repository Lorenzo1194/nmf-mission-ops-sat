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

package com.github.kayak.ui.rawview;

import com.github.kayak.core.Frame;
import com.github.kayak.core.FrameListener;
import com.github.kayak.core.Util;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 */
public class RawViewTableModel extends AbstractTableModel implements FrameListener {

    private final Map<String, FrameData> data = Collections.synchronizedMap(new TreeMap<String, FrameData>());    private Thread refreshThread;
    private boolean colorize = false;

    private Runnable refreshRunnable = new Runnable() {

        @Override
        public void run() {
            while (true) {
                synchronized (data) {
                    Set<String> keys = data.keySet();
                    String[] keyArray = keys.toArray(new String[0]);
                    for(int i=0;i<keyArray.length;i++) {
                        FrameData element = data.get(keyArray[i]);
                        if(!element.isInTable()) {
                            fireTableRowsInserted(i, i);
                            element.setInTable(true);
                        } else if(element.isDataChanged()) {
                            fireTableRowsUpdated(i, i);
                            element.setDataChanged(false);
                        }
                    }
                }

                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    return;
                }
            }
        }

    };

    public boolean isColorized() {
        return colorize;
    }

    public void setColorized(boolean colorize) {
        this.colorize = colorize;
    }

    public void startRefresh() {
        refreshThread = new Thread(refreshRunnable);
        refreshThread.start();
    }

    public void stopRefresh() {
        refreshThread.interrupt();
        try {
            refreshThread.join();
        } catch (InterruptedException ex) {
            return;
        }
        refreshThread = null;
    }

    public void clear() {
        synchronized(data) {
            int length = data.size();
            data.clear();
            fireTableRowsDeleted(0, length-1);
        }
    }

    @Override
    public int getRowCount() {
        synchronized(data) {
            return data.size();
        }
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    private int getRowForKey(String key) {
        synchronized(data) {
            String[] keys = data.keySet().toArray(new String[] {});

            for(int i=0;i<keys.length;i++)
                if(keys[i].equals(key))
                    return i;

            return -1;
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        synchronized (data) {
            String[] keys = data.keySet().toArray(new String[]{});

            switch (columnIndex) {
                case 0:
                    StringBuilder sb = new StringBuilder(20);
                    long timestamp = data.get(keys[rowIndex]).getTimestamp();
                    sb.append(timestamp/1000000);
                    sb.append(".");
                    long l = timestamp % 1000000;
                    String lstring = String.valueOf(l);
                    sb.append(lstring);
                    for(int i=0;i<(6-lstring.length());i++)
                        sb.append('0');
                    return sb.toString();
                case 1:
                    return data.get(keys[rowIndex]).getInterval() / 1000;
                case 2:
                    FrameData f = data.get(keys[rowIndex]);
                    if(f.isExtended()) {
                        return String.format("%08x", f.getIdentifier());
                    } else {
                        return String.format("%03x", f.getIdentifier());
                    }
                case 3:
                    return data.get(keys[rowIndex]).getData().length;
                case 4:
                    FrameData frameData = data.get(keys[rowIndex]);
                    byte[] dat = frameData.getData();
                    int[] frequency = frameData.getFrequency();

                    String datString = com.github.kayak.core.Util.byteArrayToHexString(dat, false);
                    if (datString.length() % 2 != 0) {
                        datString = "0" + datString;
                    }

                    if(colorize) {
                        String res = "<html>";
                        for (int i = 0; i < datString.length(); i += 2) {
                            res += "<font color=\"#";
                            String s = Integer.toHexString(frequency[i/2]);
                            if(s.length() == 1)
                                s = "0" + s;
                            res += s + "0000\">";

                            res += datString.substring(i, i + 2);
                            if (i != datString.length()) {
                                res += " ";
                            }
                            res += "</font>";
                        }
                        res += "</html>";
                        return res;
                    } else {
                        String res = "";
                        for (int i = 0; i < datString.length(); i += 2) {
                            res += datString.substring(i, i + 2);
                            if (i != datString.length()) {
                                res += " ";
                            }
                        }
                        return res;
                    }
                default:
                    return null;
            }
        }
    }

    public byte[] getData(int row) {
        synchronized(data) {
            String[] keys = data.keySet().toArray(new String[]{});
            FrameData frameData = data.get(keys[row]);
            return frameData.getData();
        }
    }

    public byte[] getDataForID(String id) {
        synchronized(data) {
            FrameData d = data.get(id);
            if(d != null)
                return d.getData();
            else
                return null;
        }
    }

    @Override
    public void newFrame(Frame frame) {
        synchronized(data) {
            String idString;
            if(frame.isExtended())
                idString = String.format("%08x", frame.getIdentifier());
            else
                idString = String.format("%03x", frame.getIdentifier());

            int row = getRowForKey(idString);
            if (row != -1) {
                FrameData old = data.get(idString);
                old.updateWith(frame);
            } else {
                data.put(idString, new FrameData(frame));
            }
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch(columnIndex) {
            case 0:
                return String.class;
            case 1:
                return Integer.class;
            case 2:
                return String.class;
            case 3:
                return Integer.class;
            case 4:
                return String.class;
            default:
                return null;
        }
    }

    @Override
    public String getColumnName(int column) {
        switch(column) {
            case 0:
                return "Timestamp [s]";
            case 1:
                return "Interval [ms]";
            case 2:
                return "Identifier [hex]";
            case 3:
                return "DLC";
            case 4:
                return "Data [hex]";
            default:
                return null;
        }
    }

}
