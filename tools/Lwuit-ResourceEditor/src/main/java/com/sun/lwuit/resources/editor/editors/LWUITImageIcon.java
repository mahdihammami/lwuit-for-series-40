/*
 * Copyright (c) 2008, 2010, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores
 * CA 94065 USA or visit www.oracle.com if you need additional information or
 * have any questions.
 */

package com.sun.lwuit.resources.editor.editors;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.Icon;

/**
 * An icon that renders a LWUIT image at 16x16 for renderers
 *
 * @author Shai Almog
 */
public class LWUITImageIcon implements Icon {
    private com.sun.lwuit.Image img;
    private int width;
    private int height;
    public LWUITImageIcon(com.sun.lwuit.Image img) {
        this(img, 16, 16);
    }

    public static LWUITImageIcon create(com.sun.lwuit.Image img) {
        return new LWUITImageIcon(img, img.getWidth(), img.getHeight());
    }

    public LWUITImageIcon(com.sun.lwuit.Image img, int width, int height) {
        this.img = img;
        this.width = width;
        this.height = height;
    }
    
    public void paintIcon(Component c, Graphics g, int x, int y) {
        if(getImage() == null) {
            return;
        }
        int[] rgb = getImage().scaled(width, height).getRGB();
        BufferedImage i = new BufferedImage(getIconWidth(), getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        i.setRGB(0, 0, getIconWidth(), getIconHeight(), rgb, 0, getIconWidth());
        g.drawImage(i, x, y, null);
    }

    public int getIconWidth() {
        return width;
    }

    public int getIconHeight() {
        return height;
    }

    /**
     * @return the img
     */
    public com.sun.lwuit.Image getImage() {
        return img;
    }

}
