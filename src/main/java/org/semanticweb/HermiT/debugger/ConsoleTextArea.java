/* Copyright 2008, 2009, 2010 by the Oxford University Computing Laboratory
   
   This file is part of HermiT.

   HermiT is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.
   
   HermiT is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.
   
   You should have received a copy of the GNU Lesser General Public License
   along with HermiT.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.semanticweb.HermiT.debugger;
 
import java.awt.AWTEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * ConsoleTextArea.
 */
 @SuppressWarnings("serial")
public class ConsoleTextArea extends JTextArea {
    protected final ConsoleWriter m_writer=new ConsoleWriter();
    protected final ConsoleReader m_reader=new ConsoleReader();
    protected int m_userTypedTextStart;

    /**
     * Init class.
     */
    public ConsoleTextArea() {
        setDocument(new ConsoleDocument());
        enableEvents(AWTEvent.KEY_EVENT_MASK);
    }
    /**
     * @return writer
     */
    public Writer getWriter() {
        return m_writer;
    }
    /**
     * @return reader
     */
    public Reader getReader() {
        return m_reader;
    }
    /**
     * Clear.
     */
    public void clear() {
        m_userTypedTextStart=0;
        setText("");
    }
    protected void moveToEndIfNecessary() {
        int selectionStart=getSelectionStart();
        int selectionEnd=getSelectionEnd();
        if (selectionEnd<m_userTypedTextStart || (selectionEnd==m_userTypedTextStart && selectionStart!=selectionEnd)) {
            int length=getDocument().getLength();
            select(length,length);
        }
    }
    @Override
    public void replaceSelection(String string) {
        moveToEndIfNecessary();
        super.replaceSelection(string);
    }
    @Override
    protected void processKeyEvent(KeyEvent event) {
        if (event.getKeyCode()!=KeyEvent.VK_ENTER)
            super.processKeyEvent(event);
        if (event.getID()==KeyEvent.KEY_PRESSED && event.getKeyCode()==KeyEvent.VK_ENTER) {
            int textEnd=getDocument().getLength();
            select(textEnd,textEnd);
            super.replaceSelection("\n");
            textEnd=getDocument().getLength();
            String text;
            try {
                text=getDocument().getText(m_userTypedTextStart,textEnd-m_userTypedTextStart);
            }
            catch (@SuppressWarnings("unused") BadLocationException error) {
                text="";
            }
            m_reader.addToBuffer(text);
            m_userTypedTextStart=textEnd;
            select(m_userTypedTextStart,m_userTypedTextStart);
        }
    }

    protected class ConsoleDocument extends PlainDocument {
        @Override
        public void remove(int offset,int length) throws BadLocationException {
            if (offset>=m_userTypedTextStart)
                super.remove(offset,length);
        }
        @Override
        public void insertString(int offset,String string,AttributeSet attributeSet) throws BadLocationException {
            if (offset>=m_userTypedTextStart)
                super.insertString(offset,string,attributeSet);
        }
    }

    protected class ConsoleWriter extends Writer implements ActionListener {
        protected final char[] m_buffer;
        protected final Timer m_timer;
        protected int m_firstFreeChar;

        public ConsoleWriter() {
            m_buffer=new char[4096];
            m_timer=new Timer(500,this);
            m_timer.setRepeats(false);
            m_firstFreeChar=0;
        }
        @Override
        public void close() {
            flush();
        }
        @Override
        public void flush() {
            synchronized (lock) {
                if (m_firstFreeChar>0) {
                    final String string=new String(m_buffer,0,m_firstFreeChar);
                    m_firstFreeChar=0;
                    SwingUtilities.invokeLater(() -> {
                        replaceSelection(string);
                        m_userTypedTextStart=getDocument().getLength();
                        select(m_userTypedTextStart,m_userTypedTextStart);
                     });
                    m_timer.stop();
                }
            }
        }
        @Override
        public void write(char[] buffer,int o,int c) {
            int count=c;
            int offset=o;
            synchronized (lock) {
                int lastPosition=offset+count;
                while (offset!=lastPosition) {
                    int toCopy=Math.min(m_buffer.length-m_firstFreeChar,count);
                    if (toCopy!=0) {
                        System.arraycopy(buffer,offset,m_buffer,m_firstFreeChar,toCopy);
                        count-=toCopy;
                        offset+=toCopy;
                        boolean bufferWasEmpty=(m_firstFreeChar==0);
                        m_firstFreeChar+=toCopy;
                        if (m_firstFreeChar>=m_buffer.length)
                            flush();
                        else if (bufferWasEmpty)
                            m_timer.start();
                    }
                }
            }
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            flush();
        }
    }

    protected class ConsoleReader extends Reader {
        protected char[] m_buffer;
        protected int m_nextCharToRead;
        protected int m_firstFreeChar;

        public ConsoleReader() {
            m_buffer=new char[4096];
            m_nextCharToRead=0;
            m_firstFreeChar=0;
        }
        public void addToBuffer(String string) {
            synchronized (lock) {
                if (m_nextCharToRead==m_firstFreeChar) {
                    m_nextCharToRead=0;
                    m_firstFreeChar=0;
                }
                else {
                    if (m_nextCharToRead!=0) {
                        System.arraycopy(m_buffer,m_nextCharToRead,m_buffer,0,m_firstFreeChar-m_nextCharToRead);
                        m_nextCharToRead=0;
                        m_firstFreeChar=0;
                    }
                }
                if (m_firstFreeChar+string.length()>m_buffer.length) {
                    char[] newBuffer=new char[m_firstFreeChar+string.length()];
                    System.arraycopy(m_buffer,0,newBuffer,0,m_buffer.length);
                    m_buffer=newBuffer;
                }
                string.getChars(0,string.length(),m_buffer,m_firstFreeChar);
                m_firstFreeChar+=string.length();
                notifyAll();
            }
        }
        @Override
        public void close() {
        }
        @Override
        public int read(char[] buffer,int offset,int length) throws IOException {
            m_writer.flush();
            synchronized (lock) {
                while (m_nextCharToRead==m_firstFreeChar)
                    try {
                        lock.wait();
                    }
                    catch (InterruptedException error) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Read interrupted.",error);
                    }
                int toCopy=Math.min(m_firstFreeChar-m_nextCharToRead,length);
                System.arraycopy(m_buffer,m_nextCharToRead,buffer,offset,toCopy);
                m_nextCharToRead+=toCopy;
                return toCopy;
            }
        }
    }
}
