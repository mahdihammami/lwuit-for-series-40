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
package com.sun.lwuit;

import com.nokia.lwuit.TextEditorProvider;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.events.FocusListener;
import com.sun.lwuit.geom.Dimension;
import com.sun.lwuit.geom.Rectangle;
import com.sun.lwuit.impl.s40.S40Implementation;
import com.sun.lwuit.plaf.LookAndFeel;
import com.sun.lwuit.plaf.Style;
import com.sun.lwuit.plaf.UIManager;
import com.sun.lwuit.impl.LWUITImplementation;
import java.util.Vector;
/**
 * An optionally multi-line editable region that can display text and allow a user to edit it.
 * Depending on the platform editing might occur in a new screen. Notice that when creating
 * a text area with one row it will act as a text field and never grow beyond that, however 
 * when assigning a greater number of rows the text area becomes multi-line with a minimum
 * number of visible rows, the text area will grow based on its content.
 *
 * @author Chen Fishbein
 */
public class TextArea extends Component implements TextEditorProvider.TextEditorListener, FocusListener {
    private static int defaultValign = TOP;
    
    /**
     * Indicates the defaut vertical alignment for a text field, only applies to single line text fields
     * @return the defaultValign
     */
    public static int getDefaultValign() {
        return defaultValign;
    }

    /**
     * Indicates the defaut vertical alignment for a text field, only applies to single line text fields
     * @param aDefaultValign the defaultValign to set
     */
    public static void setDefaultValign(int aDefaultValign) {
        defaultValign = aDefaultValign;
    }

    private int valign = defaultValign;

    private static int defaultMaxSize = 124;
    private static boolean autoDegradeMaxSize = false;
    private static boolean hadSuccessfulEdit = false;

    private int linesToScroll = 1;

    /**
     * Indicates the enter key to be used for editing the text area and by the
     * text field
     */
    private static final char ENTER_KEY = '\n';

    /**
     * Unsupported characters is a string that contains characters that cause issues 
     * when rendering on some problematic fonts. The rendering engine can thus remove them
     * when drawing.
     */
    private String unsupportedChars = "\t\r";

    /**
     * By default text area uses charWidth since its much faster on some devices
     * than string width. However, with some fonts and especially some languages (such
     * as Arabic, Korean etc.) the width of the string drawn might not equal the summary
     * of the width of the chars. Hence for portability to those languages/fonts this
     * flag must be set to true.
     */
    private static boolean useStringWidth;

    /**
     * Allows any type of input into a text field, if a constraint is not supported
     * by an underlying implementation this will be the default.
     */
    public static final int ANY = 0;

    /**
     * The user is allowed to enter an e-mail address.
     */
    public static final int EMAILADDR = 1;

    /**
     * The user is allowed to enter only an integer value.
     */
    public static final int NUMERIC = 2;

    /**
     * The user is allowed to enter a phone number.
     */
    public static final int PHONENUMBER = 3;

    /**
     * The user is allowed to enter a URL.
     */
    public static final int URL = 4;

    /**
     * The user is allowed to enter numeric values with optional decimal 
     * fractions, for example "-123", "0.123", or ".5".
     */
    public static final int DECIMAL = 5;
    
    /**
     * Indicates that the text entered is confidential data that should be 
     * obscured whenever possible.
     */
    public static final int PASSWORD = 0x10000;

    /**
     *  Indicates that editing is currently disallowed.
     */
    public static final int UNEDITABLE = 0x20000;

    /**
     * Indicates that the text entered is sensitive data that the 
     * implementation must never store into a dictionary or table for use 
     * in predictive, auto-completing, or other accelerated input schemes.
     */
    public static final int SENSITIVE = 0x40000;

    /**
     * Indicates that the text entered does not consist of words that are 
     * likely to be found in dictionaries typically used by predictive input 
     * schemes.
     */
    public static final int NON_PREDICTIVE= 0x80000;

    /**
     * This flag is a hint to the implementation that during text editing, 
     * the initial letter of each word should be capitalized.
     */
    public static final int INITIAL_CAPS_WORD = 0x100000;

    /**
     * This flag is a hint to the implementation that during text editing, 
     * the initial letter of each sentence should be capitalized.
     */
    public static final int INITIAL_CAPS_SENTENCE = 0x200000;
    //private int modifierFlag = 0x00000;
             
    /**
     * Input constraint which should be one of ANY, NUMERIC,
     * PHONENUMBER, URL or EMAIL
     */
    private int constraint = ANY;
    
    private  String text="";
    
    private  boolean editable = true;
    
    private int maxSize = defaultMaxSize ; //maximum size (number of characters) that can be stored in this TextField.
    
    private int rows = 1;
    
    private int columns = 3;
    
    // problematic  maxSize = 20; //maximum size (number of characters) that can be stored in this TextField.
    
    private Vector rowStrings;
    private int widthForRowCalculations = -1;

    private int rowsGap = 2;

    private boolean triggerClose;

    private Vector actionListeners = null;
    
    /**
     * Indicates that the text area should "grow" in height based on the content beyond the
     * limits indicate by the rows variable
     */
    private boolean growByContent = true;

    /**
     * Indicates the widest character in the alphabet, this is useful for detecting
     * linebreaks internally. In CJK languages the widest char is different than W
     * hence this functionality is exposed to developers.
     */
    private static char widestChar = 'W';

    /**
     * Indicates whether this is a single line text area, in which case "growing" won't
     * work as expected.
     */
    private boolean singleLineTextArea;

    private int currentRowWidth;

    private int caretPosition = -1;
    
    private Label hintLabel;
    
    /**
     * In place texteditor. To save memory, a single TextEditor instance
     * serves editing mode for all TextAreas
     */
    private static TextEditorProvider textEditor;
    /**
     * The y position of the text in native TextEditor
     */
    private int visibleContentPosition;
    
    /**
     * Flag to indicate if textEditor is enabled or not
     */
    private boolean textEditorEnabled = true;
    
    private boolean dontWaitForKeyReleased = false;
    
    int leftPadding;
    int rightPadding;
    int topPadding;
    int bottomPadding;
    
    private Command clearCommand;
    private String clearText = UIManager.getInstance().localize("clear", "Clear");
    
    private Command selectCommand;

	
    /**
     * Used to listen dragevents from parent form.
     */
    private ActionListener dragListener = new ActionListener() {

        public void actionPerformed(ActionEvent evt) {
            if(isTextEditorActive()) {
                hideTextEditor();
            }
            
        }
    };
    
    private ActionListener showListener = new ActionListener() {
        
        public void actionPerformed(ActionEvent evt) {
            if(Display.getInstance().getDeviceType() != Display.NON_TOUCH_DEVICE) {
                ensureTextEditorIsShown();
            }
        }
    };
    
    /**
     * Creates an area with the given rows and columns
     * 
     * @param rows the number of rows
     * @param columns - the number of columns
     * @throws IllegalArgumentException if rows <= 0 or columns <= 1
     */
    public TextArea(int rows, int columns){
        this("", defaultMaxSize, rows, columns, ANY);
    }

    /**
     * Creates an area with the given rows, columns and constraint 
     * 
     * @param rows the number of rows
     * @param columns - the number of columns
     * @param constraint one of ANY, EMAILADDR, NUMERIC, PHONENUMBER, URL, DECIMAL
     * it can be bitwised or'd with one of PASSWORD, UNEDITABLE, SENSITIVE, NON_PREDICTIVE,
     * INITIAL_CAPS_SENTENCE, INITIAL_CAPS_WORD. E.g. ANY | PASSWORD.
     * @throws IllegalArgumentException if rows <= 0 or columns <= 1
     */
    public TextArea(int rows, int columns, int constraint){
        this("", defaultMaxSize, rows, columns, constraint);
    }
    
    /**
     * Creates an area with the given text, rows and columns
     * 
     * @param text the text to be displayed; if text is null, the empty 
     * string "" will be displayed
     * @param rows the number of rows
     * @param columns - the number of columns
     * @throws IllegalArgumentException if rows <= 0 or columns <= 1
     */
    public TextArea(String text, int rows, int columns){
        this(text,defaultMaxSize, rows, columns, ANY); //String , maxSize, constraints= 0 (ANY)
    }

    /**
     * Creates an area with the given text, rows, columns and constraint 
     * 
     * @param text the text to be displayed; if text is null, the empty 
     * string "" will be displayed
     * @param rows the number of rows
     * @param columns - the number of columns
     * @param constraint one of ANY, EMAILADDR, NUMERIC, PHONENUMBER, URL, DECIMAL
     * it can be bitwised or'd with one of PASSWORD, UNEDITABLE, SENSITIVE, NON_PREDICTIVE,
     * INITIAL_CAPS_SENTENCE, INITIAL_CAPS_WORD. E.g. ANY | PASSWORD.
     * @throws IllegalArgumentException if rows <= 0 or columns <= 1
     */
    public TextArea(String text, int rows, int columns, int constraint){
        this(text,defaultMaxSize, rows, columns, constraint);
    }

    /**
     * Creates an area with the given text and maximum size, this constructor
     * will create a single line text area similar to a text field! 
     * 
     * @param text the text to be displayed; if text is null, the empty 
     * string "" will be displayed
     * @param maxSize text area maximum size
     */
    public TextArea(String text, int maxSize){
        this(text,maxSize, 1, 3, ANY);
    }
    
    /**
     * Creates an area with the given text, this constructor
     * will create a single line text area similar to a text field! 
     * 
     * @param text the text to be displayed; if text is null, the empty 
     * string "" will be displayed
     */
    public TextArea(String text) {
        this(text, Math.max(defaultMaxSize, text.length()), 1, 3, ANY);
    }

    /**
     * Creates an empty text area, this constructor
     * will create a single line text area similar to a text field! 
     */
    public TextArea() {
        this("");
    }
    
    /**
     * Creates an area with the given text, maximum size, rows, columns and constraint 
     * 
     * @param text the text to be displayed; if text is null, the empty 
     * string "" will be displayed
     * @param maxSize text area maximum size
     * @param rows the number of rows
     * @param columns - the number of columns
     * @param constraint one of ANY, EMAILADDR, NUMERIC, PHONENUMBER, URL, DECIMAL
     * it can be bitwised or'd with one of PASSWORD, UNEDITABLE, SENSITIVE, NON_PREDICTIVE,
     * INITIAL_CAPS_SENTENCE, INITIAL_CAPS_WORD. E.g. ANY | PASSWORD.
     * @throws IllegalArgumentException if rows <= 0 or columns <= 1
     */
    private TextArea(String text, int maxSize, int rows, int columns, int constraint){
        
        setUIID("TextArea");
        setSelectCommandText(UIManager.getInstance().localize("edit", "Edit"));
        this.maxSize = maxSize;
        if(rows <= 0){
            throw new IllegalArgumentException("rows must be positive");
        }
        if(columns <= 1 && rows != 1){
            throw new IllegalArgumentException("columns must be larger than 1");
        }
        this.rows = rows;
        this.columns = columns;
        LookAndFeel laf = UIManager.getInstance().getLookAndFeel();
        setSmoothScrolling(laf.isDefaultSmoothScrolling());
        
        try {
            setText(text);
        }catch(IllegalArgumentException iae) {
            iae.printStackTrace();
        }
        addFocusListener(this);
        setGrowByContent(false);
        setConstraint(constraint);
        
    }

    /**
     * Sets the constraint 
     * 
     * @param constraint one of ANY, EMAILADDR, NUMERIC, PHONENUMBER, URL, DECIMAL
     * it can be bitwised or'd with one of PASSWORD, UNEDITABLE, SENSITIVE, NON_PREDICTIVE,
     * INITIAL_CAPS_SENTENCE, INITIAL_CAPS_WORD. E.g. ANY | PASSWORD.
     */
    public void setConstraint(int constraint) {
        this.constraint = constraint;
        if(textEditor != null) {
            textEditor.setConstraints(constraint);
        }
    }


    /**
     * Returns the editing constraint value
     * 
     * @return the editing constraint value
     * @see #setConstraint
     */
    public int getConstraint() {
        return constraint;
    }

    /**
     * Sets the text within this text area
     * 
     * @param t new value for the text area
     */
    public void setText(String t) {        
        this.text = (t != null) ? t : "";        
        if(maxSize < text.length()) {
            maxSize = text.length() + 1;
            if(textEditor != null) {
                textEditor.setMaxSize(maxSize);
            }
        }
        
        synchronized(this) {
            //zero the vector inorder to initialize it on the next paint
            rowStrings=null; 
        }

        if (isTextEditorActive() && !textEditor.getContent().equals(text)) {
            textEditor.setContent(text);
            textEditor.setCaret(text.length());
        }

        setShouldCalcPreferredSize(true);
    }

    /**
     * Returns the text in the text area
     * 
     * @return the text in the text area
     */
    public String getText() {
        return text;
    }
    
    /**
     * Returns true if this area is editable
     * 
     * @return true if this area is editable
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * Sets this text area to be editable or readonly
     * 
     * @param b true is text are is editable; otherwise false
     */
    public void setEditable(boolean b) {
        if(!b) {
            constraint = constraint | TextArea.UNEDITABLE;
        } else {
            if((constraint & TextArea.UNEDITABLE) != 0) {
                constraint &= ~TextArea.UNEDITABLE;
            }
        }
        
        setConstraint(constraint);
        editable = b;
    }

    /**
     * Returns the maximum size for the text area
     * 
     * @return the maximum size for the text area
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * Sets the maximum size of the text area
     * 
     * @param maxSize the maximum size of the text area
     */
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
        if(textEditor != null) {
            textEditor.setMaxSize(maxSize);
        }
    }
    
    /**
     * @inheritDoc
     */
    public void keyPressed(int keyCode) {
        super.keyPressed(keyCode);
        int action = com.sun.lwuit.Display.getInstance().getGameAction(keyCode);

        // this works around a bug where fire is also a softkey on devices such as newer Nokia
        // series 40's (e.g. the Nokia emulator). It closes its native text box on fire then
        // as a result of a Nokia bug we get the key released of that closing and assume the
        // users wants to edit the text... When means the only way to exit the native text box
        // is via the cancel option (after pressing OK once).
        triggerClose = action == Display.GAME_FIRE;

        //scroll the TextArea
        Rectangle rect = new Rectangle(getScrollX(), getScrollY(), getWidth(), getHeight());
        Font textFont = getStyle().getFont();
        if(action == Display.GAME_DOWN){
            if((getScrollY() + getHeight()) <(rowsGap + getStyle().getFont().getHeight()) * getLines()) {
                rect.setY(rect.getY() + (textFont.getHeight() + rowsGap) * linesToScroll);
                scrollRectToVisible(rect, this);
            } else {
                setHandlesInput(false);
            }
        } else {
            if(action == Display.GAME_UP){
                if(getScrollY() > 0) {
                    rect.setY(Math.max(0, rect.getY() - (textFont.getHeight() + rowsGap) * linesToScroll));
                    scrollRectToVisible(rect, this);
                } else {
                    setHandlesInput(false);
                }
            }
        }
        if(action == Display.GAME_RIGHT || action == Display.GAME_LEFT){
            setHandlesInput(false);
        }
    }
    
    
    /**
     * @inheritDoc
     */
    protected void fireClicked() {
        onClick();
    }
    
    /**
     * @inheritDoc
     */
    protected boolean isSelectableInteraction() {
        return editable;
    }

    /**
     * @inheritDoc
     */
    public void keyReleased(int keyCode) {
        if (!textEditorEnabled) {
            int action = com.sun.lwuit.Display.getInstance().getGameAction(keyCode);
            if (isEditable()) {
                // this works around a bug where fire is also a softkey on devices such as newer Nokia
                // series 40's
                if (triggerClose && (action == Display.GAME_FIRE || isEnterKey(keyCode))) {
                    triggerClose = false;
                    onClick();
                    return;
                }
                if (action == 0 && keyCode > 0) {
                    Display.getInstance().editString(this, getMaxSize(), getConstraint(), getText(), keyCode);
                }
            }
        }
    }
    
    /**
     * @inheritDoc
     */
    public boolean isScrollableY() {
        if(textEditorEnabled) {
            return false;
        }else {
            return isFocusable() && getScrollDimension().getHeight() > getHeight();
        }
        
    }

    void onClick(){
        if(isEditable()) {
            editString();
        }
    }
        
    void editString() {
        if (textEditor == null) {
            if (autoDegradeMaxSize && (!hadSuccessfulEdit) && (maxSize > 1024)) {
                try {
                    Display.getInstance().editString(this, getMaxSize(), getConstraint(), getText());
                } catch (IllegalArgumentException err) {
                    maxSize -= 1024;
                    setDefaultMaxSize(maxSize);
                    editString();
                }
            } else {
                Display.getInstance().editString(this, getMaxSize(), getConstraint(), getText());
            }

        }else {
            if(Display.getInstance().getDeviceType() == Display.NON_TOUCH_DEVICE) {
                if(!isTextEditorActive()) {
                    setOkAsSelectCommand();
                    focusTextEditor();
                    
                }else {
                    setEditAsSelectCommand();
                    hideTextEditor();
                }
            }
        }
    }
    
    private void setOkAsSelectCommand() {
        if (selectCommand != null) {
            getComponentForm().removeCommand(selectCommand);
        }
        getComponentForm().removeCommand(getComponentForm().getMenuBar().getSelectCommand());
        getComponentForm().getMenuBar().addSelectCommand(UIManager.getInstance().localize("OK", "OK"));
        selectCommand = getComponentForm().getMenuBar().getSelectCommand();
    }
    private void setEditAsSelectCommand() {
        if (selectCommand != null) {
            getComponentForm().removeCommand(selectCommand);
        }
        getComponentForm().removeCommand(getComponentForm().getMenuBar().getSelectCommand());
        getComponentForm().getMenuBar().addSelectCommand(UIManager.getInstance().localize("Edit", "Edit"));
        selectCommand = getComponentForm().getMenuBar().getSelectCommand();
    }

    private void changeEditBackIfOkInSelect() {
        if(selectCommand != null && 
           selectCommand.getCommandName()
                .equals(UIManager.getInstance().localize("OK", "OK"))) {
            setEditAsSelectCommand();
        } 
    }
    
    /**
     * @inheritDoc
     */
    public void pointerHover(int[] x, int[] y) {
        if(!textEditorEnabled) {
            requestFocus();
        }
    }

    /**
     * @inheritDoc
     */
    public void pointerHoverReleased(int[] x, int[] y) {
        requestFocus();
    }
    
    /**
     * @inheritDoc
     */
    public void pointerReleased(int x, int y) {
        // prevent a drag operation from going into edit mode
        if (isDragActivated()) {
            super.pointerReleased(x, y);
        } else {
            super.pointerReleased(x, y);
            if (isEditable() && isEnabled() && !isCellRenderer()) {
                if(textEditorEnabled) {  
                    onClick();
                }else {
                    if (Display.getInstance().isTouchScreenDevice()) {
                        if (!Display.getInstance().isVirtualKeyboardShowing()) {
                            Display.getInstance().setShowVirtualKeyboard(true);
                        }
                    } else {
                        onClick();
                    }
                }
            }
        }
        if(isInitialized()) {
            focusTextEditor();
        }
    }

    public void pointerPressed(int x, int y) {
        super.pointerPressed(x, y);
    }
    

    public void pointerDragged(int x, int y) {
        super.pointerDragged(x, y);
        hideTextEditor();
    }
    

    /**
     * @inheritDoc
     */
    void focusGainedInternal() {
        super.focusGainedInternal();
        setHandlesInput(isScrollableY());
    }

    /**
     * @inheritDoc
     */
    void focusLostInternal() {
        super.focusLostInternal();
        setHandlesInput(false);
    }
    
    /**
     * Returns the number of columns in the text area
     * 
     * @return the number of columns in the text area
     */
    public int getColumns() {
        return columns;
    }

    /**
     * Returns the number of text lines in the TextArea
     *
     * @return the number of text lines in the TextArea
     */
    public synchronized int getLines(){
        
        int l = getRowStrings().size();
        return l;
    }

    /**
     * Returns the number of actual rows in the text area taking into consideration
     * growsByContent
     * 
     * @return the number of rows in the text area
     */
    public int getActualRows() {
        if(growByContent) {
            return Math.max(rows, getLines());
        }
        return rows;
    }
    
    /**
     * Returns the number of rows in the text area
     * 
     * @return the number of rows in the text area
     */
    public int getRows() {
        return rows;
    }
    
    /**
     * Sets the number of columns in the text area
     * 
     * @param columns number of columns
     */
    public void setColumns(int columns) {
        setShouldCalcPreferredSize(true);
        this.columns = columns;
    }
    
    /**
     * Sets the number of rows in the text area
     * 
     * @param rows number of rows
     */
    public void setRows(int rows) {
        setShouldCalcPreferredSize(true);
        this.rows = rows;
        if(textEditor != null) {
            textEditor.setMultiline(rows > 1);
        }
    }
    
    void initComponentImpl() {
        Form f = getComponentForm();
        if (f != null) {
                f.addPointerDraggedListener(dragListener);
                f.addShowListener(showListener);
            }
        initTextEditor();
        super.initComponentImpl();
        setShouldCalcPreferredSize(true);
    }
    
    private void initTextEditor() {
        //the 100 is set to width since texteditor requires pixelwidth, not some columnwidth
        if(textEditor == null && Display.getInstance().getImplementation() instanceof S40Implementation) {
            S40Implementation impl = (S40Implementation) Display.getInstance().getImplementation();
            textEditor = impl.requestNewNativeTextEditor(maxSize, constraint, 100, rows);
        }
        if (textEditor != null) {

            setRowsGap(textEditor.getLineMarginHeight());
            updateTextAreaStyles();

            clearCommand = new Command(clearText) {

                public void actionPerformed(ActionEvent evt) {
                    super.actionPerformed(evt);
                    int c = textEditor.getCaretPosition();
                    if(c != 0) {
                        try {
                            textEditor.delete(c - 1, 1);
                        }catch(IllegalArgumentException iae) {
                            //texteditor does content validation only after you
                            //have typed text so if the content doesn't fit the 
                            //constraint delete method will throw this exception
                            //for now we just ignore it.
                        }
                        textEditor.setCaret(c - 1);
                        if(textEditor.getContent().length()  == 0) {
                            removeClearCommandFromForm();
                        }
                    }
                    evt.consume();                  
                }
            };            
        }        
    }
        
    private synchronized Vector getRowStrings() {

        updatePaddings();        
        if (rowStrings == null || widthForRowCalculations != getWidth() - leftPadding - rightPadding){
            initRowString();
            setShouldCalcPreferredSize(true);
        }

        return rowStrings;
    }

    /**
     * Indicates whether a native textEditor is active.
     * @return true, if textEditor is enabled, focused and visible
     */
    public boolean isTextEditorActive() {
        return textEditor != null && textEditorEnabled && hasFocus() && textEditor.isVisible();
    }

    public TextEditorProvider getTextEditorProvider() {
        return textEditor;
    }
    
    /**
     * Returns the text in the given row of the text box
     * 
     * @param line the line number in the text box
     * @return the text of the line
     */
    public String getTextAt(int line){
        Vector rowsV = getRowStrings();
        int size = rowsV.size();
        if(size == 0){
            return "";
        }
        if(line >= size){
            return (String)rowsV.elementAt(size-1);        
        }            
        return (String)rowsV.elementAt(line);
    }
    
    private int indexOf(char[] t, char c, int offset, int length) {
        for(int iter = offset ; iter < t.length && iter < offset+length; iter++) {
            if(t[iter] == c) {
                return iter;
           }
       }
       return -1;
   }

    
    /**
     * Override this to modify the text for rendering in cases of invalid characters 
     * for display, this method allows the developer to replace such characters e.g.:
     * replace "\\t" with 4 spaces
     * 
     * @param text the text to process
     * @return the given string as a processed char array ready for rendering
     */
    protected char[] preprocess(String text) {
        return text.toCharArray();
    }

    private int updateRowWidth(char c, Font font) {
        currentRowWidth += font.charWidth(c);
        return currentRowWidth;
    }

    private boolean fastCharWidthCheck(char[] chrs, int off, int length, int width, int charWidth, Font f) {
        if(length * charWidth < width) {
            return true;
        }
        length = Math.min(chrs.length, length);
        return f.charsWidth(chrs, off, length) < width;
    }

    private synchronized void initRowString() {
        Style style = getStyle();
        rowStrings= new Vector();
        widthForRowCalculations = getWidth() - leftPadding - rightPadding;
        // single line text area is essentially a text field, we call the method
        // to allow subclasses to override it
        if ((isSingleLineTextArea()) || (widthForRowCalculations<=0)) {
            rowStrings.addElement(getText());
            return;
        }
        if(text == null || text.equals("")){
            return;
        }
        char[] text = preprocess(getText());
        
        Font font = style.getFont();
        int charWidth = font.charWidth(widestChar);
        
        int textAreaWidth = getWidth() - leftPadding - rightPadding;
                
        int minCharactersInRow = Math.max(1, textAreaWidth / charWidth);
        int rowIndex=0;
        int from=0;
        int to=from+minCharactersInRow;
        int textLength=text.length;
        String rowText = null;
        int i,spaceIndex;
       
        String unsupported = getUnsupportedChars();
        
        /*
        iteration over the string using indexes, from - the beginning of the row , to - end of a row
        for each row we will try to search for a "space" character at the end of the row ( row is text area available width)
        indorder to improve the efficiency we do not search an entire row but we start from minCharactersInRow which indicates
        what is the minimum amount of characters that can feet in the text area width.
        if we dont find we will go backwards and search for the first space available,
        if there is no space in the entire row we will cut the line inorder to fit in.
         */

        //Don't rely on the fact that short text has no newline character. we always have to parse the text.
        to = Math.max( Math.min(textLength-1,to), 0 );
        while(to<textLength) {
            if(to>textLength){
                to=textLength;
            }

            spaceIndex=-1;
            rowText="";
            int maxLength = to;

            if(useStringWidth) {
                // fix for an infinite loop issue: http://forums.java.net/jive/thread.jspa?messageID=482802
                //currentRowWidth = 0;
                String currentRow = "";
                
                // search for "space" character at close as possible to the end of the row
                for( i=to; i < textLength && fastCharWidthCheck(text, from, i - from + 1, textAreaWidth, charWidth, font) ; i++){
                    char c = text[i];
                    /*if(updateRowWidth(c, font) >= textAreaWidth) {
                        break;
                    }*/
                    currentRow+=c;
                    if(font.stringWidth(currentRow) >= textAreaWidth) {
                        break;
                    }
                    if(unsupported.indexOf(c) > -1) {
                        text[i] = ' ';
                        c = ' ';
                    }
                    if(c == ' ' || c == '\n') {
                        spaceIndex=i;
                        // newline has been found. We can end the loop here as the line cannot grow more
                        if (c == '\n')
                            break;
                    }
                    maxLength++;
                }
            } else {
                currentRowWidth = 0;
                if(to != from) {
                    currentRowWidth = font.charsWidth(text, from, to - from);
                }

                // search for "space" character at close as possible to the end of the row
                for( i=to; i < textLength ; i++){
                    char c = text[i];
                    if(updateRowWidth(c, font) >= textAreaWidth) {
                        break;
                    }
                    if(unsupported.indexOf(c) > -1) {
                        text[i] = ' ';
                        c = ' ';
                    }
                    if(c == ' ' || c == '\n') {
                        spaceIndex=i;
                        // newline has been found. We can end the loop here as the line cannot grow more
                        if (c == '\n')
                            break;
                    }
                    maxLength++;
                }
            }
            
            // if we got to the end of the text use the entire row,
            // also if space is next character (in the next row) we can cut the line
            if(i == textLength || text[i] == ' ' || text[i] == '\n') {
                spaceIndex=i;
            }

            // if we found space in the limit width of the row (searched only from minCharactersInRow)
            if(spaceIndex!=-1){
                // make sure that if we have a newline character before the end of the line we should
                // break there instead
                int newLine = indexOf(text, '\n', from, spaceIndex - from);
                if(newLine > -1 && newLine < spaceIndex) {
                    spaceIndex = newLine;
                }

                rowText = new String(text, from, spaceIndex - from);
                from=spaceIndex+1;

            } // if there is no space from minCharactersInRow to limit need to search backwards
            else{
                for( i=to; spaceIndex==-1 && i>=from ; i--){
                    char chr = text[i];
                    if(chr == ' ' || chr == '\n' || chr == '\t') {
                        spaceIndex=i;
                        
                        // don't forget to search for line breaks in the
                        // remaining part. otherwise we overlook possible
                        // line breaks!
                        int newLine = indexOf(text, '\n', from, i - from);
                        if(newLine > -1 && newLine < spaceIndex) {
                           spaceIndex = newLine;
                        }
                        rowText = new String(text, from, spaceIndex - from);
                        from=spaceIndex+1;
                    }

                }
                if(spaceIndex==-1) {
                    // from = to + 1;
                    if(maxLength <= 0) {
                        maxLength = 1;
                    }
                    spaceIndex = maxLength;
                    rowText = new String(text, from, spaceIndex - from);
                    from = spaceIndex;
                }
            }

            rowStrings.addElement(rowText);
            //adding minCharactersInRow doesn't work if what is left is less
            //then minCharactersInRow
            to=from;//+minCharactersInRow;
            rowIndex++;
        }
        if(text[text.length -1 ] == '\n'){
            rowStrings.addElement("");
        }

    }
    
    /**
     * Gets the num of pixels gap between the rows
     * 
     * @return the gap between rows in pixels
     */
    public int getRowsGap() {
        return rowsGap;
    }

    /**
     * The gap in pixels between rows
     * 
     * @param rowsGap num of pixels to gap between rows
     */
    public void setRowsGap(int rowsGap) {
        this.rowsGap = rowsGap;
    }
    
    /**
     * @inheritDoc
     */
    public void paint(Graphics g) {
        UIManager.getInstance().getLookAndFeel().drawTextArea(g, this);    
        paintHint(g);
    }
    

    /**
     * overriden from Component class. This positions the textEditor to 
     * proper place.
     */
    protected void updateNativeComponentPosition() {
        updateTextEditorPosition();
    }
    
    
    /**
     * @inheritDoc
     */
    protected Dimension calcPreferredSize(){
        Dimension d = UIManager.getInstance().getLookAndFeel().getTextAreaSize(this, true);
        return d;
    }
        
    /**
     * @inheritDoc
     */
    protected Dimension calcScrollSize(){ 
        return UIManager.getInstance().getLookAndFeel().getTextAreaSize(this, false);
    }
        
    /**
     * Add an action listener which is invoked when the text area was modified not during
     * modification. A text <b>field</b> might never fire an action event if it is edited
     * in place and the user never leaves the text field!
     * 
     * @param a actionListener
     */
    public void addActionListener(ActionListener a) {
        if(actionListeners == null) {
            actionListeners = new Vector();
        }
        if(!actionListeners.contains(a)) {
            actionListeners.addElement(a);
        }
    }

    /**
     * Removes an action listener
     * 
     * @param a actionListener
     */
    public void removeActionListener(ActionListener a) {
        if(actionListeners == null) {
            actionListeners = new Vector();
        }
        actionListeners.removeElement(a);
    }
    
    /**
     * Notifies listeners of a change to the text area
     */
    void fireActionEvent() {
        if(actionListeners != null) {
            ActionEvent evt = new ActionEvent(this);
            for(int iter = 0 ; iter < actionListeners.size() ; iter++) {
                ActionListener a = (ActionListener)actionListeners.elementAt(iter);
                a.actionPerformed(evt);
            }
        }
    }
    
    /**
     * @inheritDoc
     */
    void onEditComplete(String text) {
        setText(text);
        if(getParent() != null) {
            getParent().revalidate();
        }
    }
    
    /**
     * Sets the default limit for the native text box size
     * 
     * @param value default value for the size of the native text box
     */
    public static void setDefaultMaxSize(int value) {
        defaultMaxSize = value;
    }

    /**
     * Indicates that the text area should "grow" in height based on the content beyond the
     * limits indicate by the rows variable
     * 
     * @return true if the text component should grow and false otherwise
     */
    public boolean isGrowByContent() {
        return growByContent;
    }

    /**
     * Indicates that the text area should "grow" in height based on the content beyond the
     * limits indicate by the rows variable
     * 
     * @param growByContent true if the text component should grow and false otherwise
     */
    public void setGrowByContent(boolean growByContent) {        
        this.growByContent = growByContent;
        setShouldCalcPreferredSize(true);        
        Form f = getComponentForm();
        if(f != null) {
            f.layoutContainer();
            f.repaint();
        }
    }
    
    /**
     * Indicates whether a high value for default maxSize will be reduced to a lower
     * value if the underlying platform throws an exception.
     * 
     * @param value new value for autoDegradeMaxSize
     */
    public static void setAutoDegradeMaxSize(boolean value) {
        autoDegradeMaxSize = value;
    }

    /**
     * Indicates whether a high value for default maxSize will be reduced to a lower
     * value if the underlying platform throws an exception.
     * 
     * @return value for autoDegradeMaxSize
     */
    public static boolean isAutoDegradeMaxSize() {
        return autoDegradeMaxSize;
    }

    /**
     * Unsupported characters is a string that contains characters that cause issues 
     * when rendering on some problematic fonts. The rendering engine can thus remove them
     * when drawing.
     * 
     * @return unsupported characters string
     */
    public String getUnsupportedChars() {
        return unsupportedChars;
    }

    /**
     * Unsupported characters is a string that contains characters that cause issues 
     * when rendering on some problematic fonts. The rendering engine can thus remove them
     * when drawing.
     * 
     * @param unsupportedChars the unsupported character string
     */
    public void setUnsupportedChars(String unsupportedChars) {
        this.unsupportedChars = unsupportedChars;
    }


    /**
     * Indicates the number of lines to scroll with every scroll operation
     * 
     * @return number bigger or equal to 1
     */
    public int getLinesToScroll() {
        return linesToScroll;
    }

    /**
     * Indicates the number of lines to scroll with every scroll operation
     * 
     * @param linesToScroll number bigger or equal to 1
     */
    public void setLinesToScroll(int linesToScroll) {
        if (linesToScroll < 1) {
            throw new IllegalArgumentException("lines to scroll has to be >= 1");
        }
        this.linesToScroll = linesToScroll;
    }

    /**
     * Indicates the widest character in the alphabet, this is useful for detecting
     * linebreaks internally. In CJK languages the widest char is different than W
     * hence this functionality is exposed to developers.
     * 
     * @param widestC the widest character
     */
    public static void setWidestChar(char widestC) {
        widestChar = widestC;
    }


    /**
     * Indicates the widest character in the alphabet, this is useful for detecting
     * linebreaks internally. In CJK languages the widest char is different than W
     * hence this functionality is exposed to developers.
     * 
     * @return the widest character
     */
    public static char getWidestChar() {
        return widestChar;
    }

    /**
     * Set singleLIneTextArea variable. Mostly used only internally.
     * @param singleLineTextArea true for single-line, false otherwise.
     */
    public void setSingleLineTextArea(boolean singleLineTextArea) {
		this.singleLineTextArea = singleLineTextArea;
    }

    /**
     * Indicates whether this is a single line text area, in which case "growing" won't
     * work as expected.
     *
     * @return  true if this is a single line text area
     */
    public boolean isSingleLineTextArea() {
        return rows == 1;
    }

    /**
     * Sets the Alignment of the TextArea to one of: CENTER, LEFT, RIGHT
     *
     * @param align alignment value
     * @see #CENTER
     * @see #LEFT
     * @see #RIGHT
     * @deprecated use Style.setAlignment instead
     */
    public void setAlignment(int align) {
        getStyle().setAlignment(align);
    }

    /**
     * Returns the alignment of the TextArea
     *
     * @return the alignment of the TextArea one of: CENTER, LEFT, RIGHT
     * @see #CENTER
     * @see #LEFT
     * @see #RIGHT
     * @deprecated use Style.getAlignment instead
     */
    public int getAlignment() {
        return getStyle().getAlignment();
    }

    /**
     * Returns the absolute alignment of the TextArea
     * In RTL LEFT alignment is actually RIGHT, but this method returns the actual alignment
     *
     * @return the alignment of the TextArea one of: CENTER, LEFT, RIGHT
     * @see #CENTER
     * @see #LEFT
     * @see #RIGHT
     * @deprecated this method is redundant and no longer used
     */
    public int getAbsoluteAlignment(){
        int a = getAlignment();
        if(isRTL()) {
            switch(a) {
                case RIGHT:
                    return LEFT;
                case LEFT:
                    return RIGHT;
            }
        }
        return a;
    }

    /**
     * Returns true if the text field is waiting for a commit on editing
     *
     * @return true if a commit is pending
     */
    public boolean isPendingCommit() {
        return false;
    }

    /**
     * Returns the position of the cursor char position
     *
     * @return the cursor position
     */
    public int getCursorPosition() {
        return -1;
    }
    
    /**
     * Returns the position of the cursor line position
     * 
     * @return the cursor line position
     */
    public int getCursorY() {
        return -1;
    }    

    /**
     * Returns the position of the cursor char position in the current line.
     * 
     * @return the cursor char position in the current line
     */
    public int getCursorX() {
        return -1;
    }
    
    /**
     * True is this is a qwerty device or a device that is currently in
     * qwerty mode.
     *
     * @return currently defaults to false
     */
    public boolean isQwertyInput() {
        return false;
    }

    /**
     * Returns the currently selected input mode
     *
     * @return the display name of the input mode by default the following modes
     * are supported: Abc, ABC, abc, 123
     */
    public String getInputMode() {
        return null;
    }

    /**
     * Returns the order in which input modes are toggled
     *
     * @return the order of the input modes
     */
    public String[] getInputModeOrder() {
        return null;
    }

    /**
     * Indicates whether text field input should scroll to the right side when no
     * more room for the input is present.
     *
     * @return true if scrolling is enabled
     */
    public boolean isEnableInputScroll() {
        return false;
    }

    /**
     * Indicates the enter key to be used for editing the text area and by the
     * text field
     *
     * @param keyCode the key tested
     */
    protected boolean isEnterKey(int keyCode) {
        return keyCode == ENTER_KEY;
    }

    /**
     * Searches the given string for the widest character using char width, this operation should only
     * be performed once and it solves cases where a devices language might have a char bigger than 'W'
     * that isn't consistently bigger.
     * Notice that this method will use the TextArea style font which might differ when switching themes etc.
     *
     * @param s string to search using charWidth
     */
    public static void autoDetectWidestChar(String s) {
        Font f = UIManager.getInstance().getComponentStyle("TextArea").getFont();
        int widest = 0;
        for(int iter = 0 ; iter < s.length() ; iter++) {
            char c = s.charAt(iter);
            int w = f.charWidth(c);
            if(w > widest) {
                widest = w;
                setWidestChar(c);
            }
        }
    }

    /**
     * By default text area uses charWidth since its much faster on some devices
     * than string width. However, with some fonts and especially some languages (such
     * as Arabic, Korean etc.) the width of the string drawn might not equal the summary
     * of the width of the chars. Hence for portability to those languages/fonts this
     * flag must be set to true.
     *
     * @return the value of useStringWidth
     */
    public static boolean isUseStringWidth() {
        return useStringWidth;
    }

    /**
     * By default text area uses charWidth since its much faster on some devices
     * than string width. However, with some fonts and especially some languages (such
     * as Arabic, Korean etc.) the width of the string drawn might not equal the summary
     * of the width of the chars. Hence for portability to those languages/fonts this
     * flag must be set to true.
     *
     * @param aUseStringWidth the new value for useStringWidth
     */
    public static void setUseStringWidth(boolean aUseStringWidth) {
        useStringWidth = aUseStringWidth;
    }
    
    /**
     * Sets the TextArea hint text, the hint text  is displayed on the TextArea
     * When there is no text in the TextArea
     * 
     * @param hint the hint text to display
     */
    public void setHint(String hint){
        super.setHint(hint, getHintIcon());
    }

    /**
     * Returns the hint text
     *
     * @return the hint text or null
     */
    public String getHint() {
        return super.getHint();
    }

    /**
     * Sets the TextArea hint icon, the hint is displayed on the TextArea
     * When there is no text in the TextArea
     *
     * @param icon the icon
     */
    public void setHintIcon(Image icon){
        setHint(getHint(), icon);
    }

    /**
     * Returns the hint icon
     *
     * @return the hint icon
     */
    public Image getHintIcon() {
        return super.getHintIcon();
    }

    /**
     * Sets the TextArea hint text and Icon, the hint text and icon are 
     * displayed on the TextArea when there is no text in the TextArea
     * 
     * @param hint the hint text to display
     * @param icon the hint icon to display
     */
    public void setHint(String hint, Image icon){
        super.setHint(hint, icon);
    }

    Label getHintLabelImpl() {
        return hintLabel;
    }

    void setHintLabelImpl(Label hintLabel) {
        this.hintLabel = hintLabel;
    }

    boolean shouldShowHint() {
        boolean showHint = getText().equals("");
        
        if(isTextEditorActive()) {
            String content = textEditor.getContent();
            showHint = content.length() == 0;
        }
        return showHint;
    }

    /**
     * Sets the vertical alignment of the text field to one of: CENTER, TOP, BOTTOM<br>
     * only applies to single line text field
     *
     * @param valign alignment value
     * @see #CENTER
     * @see #TOP
     * @see #BOTTOM
     */
    public void setVerticalAlignment(int valign) {
        if(valign != CENTER && valign != TOP && valign != BOTTOM){
            throw new IllegalArgumentException("Alignment can't be set to " + valign);
        }
        this.valign = valign;
    }

    /**
     * Returns the vertical alignment of the text field, this only applies to single line text field
     *
     *
     * @return the vertical alignment of the TextField one of: CENTER, TOP, BOTTOM
     * @see #CENTER
     * @see #TOP
     * @see #BOTTOM
     */
    public int getVerticalAlignment(){
        return valign;
    }
    
    /**
     * Get events from the textEditor. Add content from texteditor to text variable every
     * time there is some change in the editor.
     * @param textEditor
     * @param actions 
     */
    public void inputAction(TextEditorProvider textEditor, int actions) {
       if((actions&TextEditorProvider.TextEditorListener.ACTION_PAINT_REQUEST) != 0
               || (actions&TextEditorProvider.TextEditorListener.ACTION_CONTENT_CHANGE) != 0) {
           repaint();
       }
       if((actions&TextEditorProvider.TextEditorListener.ACTION_SCROLLBAR_CHANGED) != 0) {
           //Visible content position shows how far the y is from the textEditor Y position
           visibleContentPosition = textEditor.getVisibleContentPosition();
       }
       
        if ((actions & TextEditorProvider.TextEditorListener.ACTION_CONTENT_CHANGE) != 0) {
            int rowsHeight = rows * (textEditor.getFont().getHeight() + textEditor.getLineMarginHeight());
            if(growByContent) {
                updatePaddings();
                textEditor.setSize(getWidth() - leftPadding - rightPadding,
                        Math.max(textEditor.getContentHeight(), rowsHeight));
                setShouldCalcPreferredSize(true);
                Form f = getComponentForm();
                if(f != null) {
                    f.repaint();
                }
            }
        }

        text = textEditor.getContent();
        if(text.length() > 0) {
            addClearCommandToForm();
        }
        if(text.length() == 0) {
            removeClearCommandFromForm();
        }
    }

    /**
     * Returns the visibility of the Nokia TextEditor.
     * @return true if visible, false otherwise.
     */
    public boolean isNativeTextEditorVisible() {
        if(textEditor != null) {
            return textEditor.isVisible();
        }
        return false;
    }

    public void setX(int x) {
        super.setX(x);
        updateTextEditorPosition();
    }

    public void setY(int y) {
        super.setY(y);
        updateTextEditorPosition();
    }    

    /**
     * @inheritDoc
     */
    public void setSize(Dimension d) {
        super.setSize(d);
        if(isTextEditorActive()) {
            updatePaddings();
            int width = d.getWidth() - leftPadding - rightPadding;
            int height = d.getHeight() - topPadding - bottomPadding;
            textEditor.setSize(width, height);
        }        
    }

    /**
     * @inheritDoc
     */
    public void setWidth(int width) {
        if(width == getWidth()) {
            return;
        }
        super.setWidth(width);
        setShouldCalcPreferredSize(true);
        if(isTextEditorActive()) {
            updatePaddings();
            textEditor.setSize(width - leftPadding - rightPadding,
                    textEditor.getHeight());
        }
    }

    /**
     * @inheritDoc
     */
    public void setHeight(int height) {
        if(height == getHeight()) {
            return;
        }
        super.setHeight(height);
        if(isTextEditorActive()) {
            updatePaddings();
            textEditor.setSize(textEditor.getWidth(),
                    height - topPadding - bottomPadding);
        }
    }

    protected void deinitialize() {
        if(isTextEditorActive()) {
            hideTextEditor();
        }

        super.deinitialize();
        
        Form f = getComponentForm();
        if (f != null) {
            f.removePointerDraggedListener(dragListener);
            f.removeShowListener(showListener);
        }
    }

    /**
     * @inheritDoc
     */
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        
    }

    /**
     * @inheritDoc
     */
    public void setFocus(boolean focused) {
        super.setFocus(focused);        
    }

    /**
     * @inheritDoc
     */
    public void focusGained(Component cmp) {

    }

    /**
     * Set focus for the text editor and prepare it to be used
     * for editing this particular TextArea component
     */
    protected synchronized void focusTextEditor() {
        if (textEditor != null && textEditorEnabled) {
            dontWaitForKeyReleased = false;
            
            if ((constraint & TextArea.UNEDITABLE) == 0) {
                if(getText().length() > 0) {
                    addClearCommandToForm();
                }
                updatePaddings();
                updateTextAreaStyles();
                
                textEditor.setFocus(true);
                textEditor.setConstraints(constraint);
                textEditor.setMaxSize(maxSize);
                textEditor.setMultiline(rows > 1);                
                textEditor.setTextEditorListener(this);                
                textEditor.setContent(getText());                
                textEditor.setPosition(getAbsoluteX() + leftPadding - 1,
                        getAbsoluteY() + topPadding);

                if(caretPosition > -1 && caretPosition <= textEditor.size()) {
                    textEditor.setCaret(caretPosition);
                } else {
                    textEditor.setCaret(textEditor.size());
                }

                int minHeight = rows * (textEditor.getFont().getHeight() + textEditor.getLineMarginHeight());
                if(growByContent) {                    
                    textEditor.setSize(getWidth() - leftPadding - rightPadding,
                            Math.max(textEditor.getContentHeight(), minHeight));
                } else {
                    textEditor.setSize(getWidth() - leftPadding - rightPadding, minHeight);
                }

                textEditor.setVisible(true);

                setShouldCalcPreferredSize(true);

                Form f = getComponentForm();
                if(f != null) {
                    f.repaint();
                }
            }
        }
    }

    /**
     * Reflects the current style of TextArea to the TextEditor component
     */
    public void updateTextAreaStyles() {
        Style s = getStyle();
        Font editorFont = getUnselectedStyle().getFont();
        getSelectedStyle().setFont(editorFont);
        javax.microedition.lcdui.Font nativeFont =
                (javax.microedition.lcdui.Font) editorFont.getNativeFont();
        if(nativeFont == null) {
            // Use native input font as an undesirable fallback
            nativeFont = javax.microedition.lcdui.Font.getFont(
                    javax.microedition.lcdui.Font.FONT_INPUT_TEXT);
        }

        textEditor.setFont(nativeFont);
        textEditor.setForegroundColor(0xFF000000 | s.getFgColor());        
    }
    
    private void addClearCommandToForm() {
        Form f = getComponentForm();
        if(f == null || f.getClearCommand() == clearCommand) {
            return;
        }
        LWUITImplementation impl = Display.getInstance().getImplementation();
        if (impl instanceof S40Implementation) {
            if (!((S40Implementation) impl).shouldHideMenu()) {
                
                if ((constraint & TextArea.UNEDITABLE) == 0) {
                    if (f.getClearCommand() != clearCommand) {
                        f.addCommand(clearCommand);
                        f.setClearCommand(clearCommand);
                        f.repaint();
                    }
                }
            }
        }
    }
    /**
     * @inheritDoc
     */
    public void focusLost(Component cmp) {   
        if (textEditor != null && textEditorEnabled) {
            textEditor.setFocus(false);
            hideTextEditor();
        }
    }

    /**
     * Hide text editor and update TextArea accordingly
     */
    protected synchronized void hideTextEditor() {
        if (textEditor != null && textEditor.isVisible()) {
            if(Display.getInstance().getDeviceType() == Display.NON_TOUCH_DEVICE) {
                changeEditBackIfOkInSelect();
            }
            textEditor.setTextEditorListener(null);
            setText(textEditor.getContent());
            int c = textEditor.getCaretPosition();
            caretPosition = c;
            if(!growByContent) {
                int h = textEditor.getHeight();
                int ch = textEditor.getContentHeight();
                if (ch > h) {
                    
                    int s = textEditor.size();
                    //if caret at the end of the content, scroll the text to same place
                    if (c == s) {
                        visibleContentPosition = ch - h;
                    }
                }
            }
            textEditor.setVisible(false);
            Display.getInstance().callSerially(new Runnable() {

                public void run() {
                    removeClearCommandFromForm();
                }
            });
            setShouldCalcPreferredSize(true);
            Form f = getComponentForm();
            if(f != null) {
                f.repaint();
            }
        }        
    }

    private void removeClearCommandFromForm() {
        Form p = getComponentForm();
        if(p != null) {
            if(p.getClearCommand() == clearCommand) {
                p.removeCommand(clearCommand);
            }
        }
    }
    
    /**
     * Get visible contentposition of the content in TextEditorProvider
     * @return visible contentposition.
     */
    public int getVisibleContentPosition() {
        return visibleContentPosition;
    }
    /**
     * Enable or disable the native TextEditor
     * @param enable 
     */
    public void setTextEditorEnabled(boolean enable) {
        textEditorEnabled = enable;
        if(!enable) {
            if(textEditor != null) {
                textEditor.setFocus(false);
                setText(textEditor.getContent());
                textEditor.setVisible(false);
            }
        }
    }
    /**
     * Return true if Nokia textEditor is enabled in this field. False if otherwise.
     * @return true if enabled, false otherwise.
     */
    public boolean getTextEditorEnabled() {
        return textEditorEnabled;
    }

    private void updateTextEditorPosition() {
        if(isTextEditorActive()) {
            updatePaddings();            
            int x = getAbsoluteX() + leftPadding - 1;
            int y = getAbsoluteY() + topPadding;

            textEditor.setPosition(x,y);
        }
    }
    /**
     * Refreshes padding variables so that they reflect the current theme
     *
     */
    private void updatePaddings() {
        //update the theme values since theme might have changed
        Style style = getStyle();
        leftPadding = style.getPadding(this.isRTL(), Component.LEFT);
        rightPadding = style.getPadding(this.isRTL(), Component.RIGHT);
        topPadding = style.getPadding(false, Component.TOP);
        bottomPadding = style.getPadding(false, Component.BOTTOM);
    }   
    /**
     * Makes sure that if textArea has focus that textEditor is shown
     * Fixes issues with going back and fort between forms that have textareas
     */
    private void ensureTextEditorIsShown() {
        if(hasFocus()) {
            focusTextEditor();
        }
    }
    
    /**
     * Set the text that should appear on the clear softkey
     * 
     * @param text localized text for the clear softbutton
     */
    public void setClearText(String text) {
        clearText = text;
        if(text != null && text.length() > 0) {
            clearCommand.setCommandName(clearText);
        }
    }
    /**
     * Get the string that is shown in the clear-button.
     * @return String containing the clear-button label.
     */
    public  String getClearText() {
        return clearText;
    }
    
    
    
}
