/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

/* The following code was generated by JFlex 1.4.1 on 8/03/06 20:58 */
/* BrowserCommandLexer.java is a generated file.  You probably want to
 * edit BrowserCommandLexer to make changes.  Use JFlex to generate it.
 * JFlex may be obtained from
 * <a href="http://www.jflex.de">the JFlex website</a>.
 * Jflex 1.4 or later is required.
 * Run:<br>
 * jflex BrowserCommandLexer<br>
 * You will then have a file called BrowserCommandLexer.java
 */
/*
 * Tokenize a command line into application and arguments.
 * Copyright (C) 2001-2004 Stephen Ostermiller
 * http://ostermiller.org/contact.pl?regarding=Java+Utilities
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * See COPYING.TXT for details.
 */
package org.opensourcephysics.desktop.ostermiller;
import java.io.IOException;

/**
 * Tokenize a command line into application and arguments.
 *
 * @author Stephen Ostermiller http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * @since ostermillerutils 1.00.00
 */
class BrowserCommandLexer {
  /** This character denotes the end of file */
  private static final int YYEOF = -1;

  /** initial size of the lookahead buffer */
  private static final int ZZ_BUFFERSIZE = 16384;

  /** lexical states */
  private static final int YYINITIAL = 0;

  /**

  /**
   * Translates characters to character classes
   */
  private static final String ZZ_CMAP_PACKED = "\11\0\2\2\1\0\2\2\22\0\1\2\1\0\1\3\36\0\1\0\32\0\1\1\uffa3\0"; //$NON-NLS-1$

  /**
   * Translates characters to character classes
   */
  private static final char[] ZZ_CMAP = zzUnpackCMap(ZZ_CMAP_PACKED);

  /**
   * Translates DFA states to action switch labels.
   */
  private static final int[] ZZ_ACTION = zzUnpackAction();
  private static final String ZZ_ACTION_PACKED_0 = "\2\1\2\2\1\1\3\0\1\3\1\1\1\3\1\0\2\3"; //$NON-NLS-1$

  private static int[] zzUnpackAction() {
    int[] result = new int[14];
    int offset = 0;
    offset = zzUnpackAction(ZZ_ACTION_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAction(String packed, int offset, int[] result) {
    int i = 0;      /* index in packed string  */
    int j = offset; /* index in unpacked array */
    int l = packed.length();
    while(i<l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do {
        result[j++] = value;
      } while(--count>0);
    }
    return j;
  }

  /**
   * Translates a state to a row index in the transition table
   */
  private static final int[] ZZ_ROWMAP = zzUnpackRowMap();
  private static final String ZZ_ROWMAP_PACKED_0 = "\0\0\0\4\0\10\0\14\0\20\0\10\0\24\0\30"+ //$NON-NLS-1$
    "\0\4\0\34\0\20\0\40\0\14\0\30";                                                         //$NON-NLS-1$

  private static int[] zzUnpackRowMap() {
    int[] result = new int[14];
    int offset = 0;
    offset = zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackRowMap(String packed, int offset, int[] result) {
    int i = 0;      /* index in packed string  */
    int j = offset; /* index in unpacked array */
    int l = packed.length();
    while(i<l) {
      int high = packed.charAt(i++)<<16;
      result[j++] = high|packed.charAt(i++);
    }
    return j;
  }

  /**
   * The transition table of the DFA
   */
  private static final int[] ZZ_TRANS = zzUnpackTrans();
  private static final String ZZ_TRANS_PACKED_0 = "\1\2\1\3\1\4\1\5\1\2\1\6\1\0\5\2"+ //$NON-NLS-1$
    "\4\0\1\5\1\7\1\10\1\11\1\5\1\12\1\5"+                                            //$NON-NLS-1$
      "\1\13\1\10\1\14\1\10\1\15\1\5\1\7\1\10"+                                       //$NON-NLS-1$
        "\1\13\1\10\1\14\1\10\1\16";                                                  //$NON-NLS-1$

  private static int[] zzUnpackTrans() {
    int[] result = new int[36];
    int offset = 0;
    offset = zzUnpackTrans(ZZ_TRANS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackTrans(String packed, int offset, int[] result) {
    int i = 0;      /* index in packed string  */
    int j = offset; /* index in unpacked array */
    int l = packed.length();
    while(i<l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      value--;
      do {
        result[j++] = value;
      } while(--count>0);
    }
    return j;
  }

  /* error codes */
  private static final int ZZ_UNKNOWN_ERROR = 0;
  private static final int ZZ_NO_MATCH = 1;
  /* error messages for the codes above */
  private static final String ZZ_ERROR_MSG[] = {
    "Unkown internal scanner error",                                                        //$NON-NLS-1$
    "Error: could not match input",                                                         //$NON-NLS-1$
    "Error: pushback value was too large"                                                   //$NON-NLS-1$
  };

  /**
   * ZZ_ATTRIBUTE[aState] contains the attributes of state <code>aState</code>
   */
  private static final int[] ZZ_ATTRIBUTE = zzUnpackAttribute();
  private static final String ZZ_ATTRIBUTE_PACKED_0 = "\3\1\1\11\1\1\3\0\3\1\1\0\1\11\1\1"; //$NON-NLS-1$

  private static int[] zzUnpackAttribute() {
    int[] result = new int[14];
    int offset = 0;
    offset = zzUnpackAttribute(ZZ_ATTRIBUTE_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAttribute(String packed, int offset, int[] result) {
    int i = 0;      /* index in packed string  */
    int j = offset; /* index in unpacked array */
    int l = packed.length();
    while(i<l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do {
        result[j++] = value;
      } while(--count>0);
    }
    return j;
  }

  /** the input device */
  private java.io.Reader zzReader;

  /** the current state of the DFA */
  private int zzState;

  /** the current lexical state */
  private int zzLexicalState = YYINITIAL;

  /** this buffer contains the current text to be matched and is
      the source of the yytext() string */
  private char zzBuffer[] = new char[ZZ_BUFFERSIZE];

  /** the textposition at the last accepting state */
  private int zzMarkedPos;

  /** the textposition at the last state to be included in yytext */
  //private int zzPushbackPos;

  /** the current text position in the buffer */
  private int zzCurrentPos;

  /** startRead marks the beginning of the yytext() string in the buffer */
  private int zzStartRead;

  /** endRead marks the last character in the buffer, that has been read
      from input */
  private int zzEndRead;

  /** zzAtEOF == true <=> the scanner is at the EOF */
  private boolean zzAtEOF;

  /**
   * Return the next token from the browser command.
   *
   * @return the next token
   * @throws IOException if an error occurs while reading the command.
   */
  public String getNextToken() throws IOException {
    return getToken();
  }

  private static String unescape(String s) {
    StringBuffer sb = new StringBuffer(s.length());
    for(int i = 0; i<s.length(); i++) {
      if((s.charAt(i)=='\\')&&(i<s.length())) {
        i++;
      }
      sb.append(s.charAt(i));
    }
    return sb.toString();
  }

  /**
   * Creates a new scanner
   * There is also a java.io.InputStream version of this constructor.
   *
   * @param   in  the java.io.Reader to read input from.
   */
  BrowserCommandLexer(java.io.Reader in) {
    this.zzReader = in;
  }

  /**
   * Creates a new scanner.
   * There is also java.io.Reader version of this constructor.
   *
   * @param   in  the java.io.Inputstream to read input from.
   */
  BrowserCommandLexer(java.io.InputStream in) {
    this(new java.io.InputStreamReader(in));
  }

  /**
   * Unpacks the compressed character translation table.
   *
   * @param packed   the packed character translation table
   * @return         the unpacked character translation table
   */
  private static char[] zzUnpackCMap(String packed) {
    char[] map = new char[0x10000];
    int i = 0; /* index in packed string  */
    int j = 0; /* index in unpacked array */
    while(i<26) {
      int count = packed.charAt(i++);
      char value = packed.charAt(i++);
      do {
        map[j++] = value;
      } while(--count>0);
    }
    return map;
  }

  /**
   * Refills the input buffer.
   *
   * @return      <code>false</code>, iff there was new input.
   *
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  private boolean zzRefill() throws java.io.IOException {
    /* first: make room (if you can) */
    if(zzStartRead>0) {
      System.arraycopy(zzBuffer, zzStartRead, zzBuffer, 0, zzEndRead-zzStartRead);
      /* translate stored positions */
      zzEndRead -= zzStartRead;
      zzCurrentPos -= zzStartRead;
      zzMarkedPos -= zzStartRead;
      //zzPushbackPos -= zzStartRead;
      zzStartRead = 0;
    }
    /* is the buffer big enough? */
    if(zzCurrentPos>=zzBuffer.length) {
      /* if not: blow it up */
      char newBuffer[] = new char[zzCurrentPos*2];
      System.arraycopy(zzBuffer, 0, newBuffer, 0, zzBuffer.length);
      zzBuffer = newBuffer;
    }
    /* finally: fill the buffer with new input */
    int numRead = zzReader.read(zzBuffer, zzEndRead, zzBuffer.length-zzEndRead);
    if(numRead<0) {
      return true;
    }
    zzEndRead += numRead;
    return false;
  }

  /**
   * Returns the text matched by the current regular expression.
   */
  private final String yytext() {
    return new String(zzBuffer, zzStartRead, zzMarkedPos-zzStartRead);
  }

  /**
   * Reports an error that occured while scanning.
   *
   * In a wellformed scanner (no or only correct usage of
   * yypushback(int) and a match-all fallback rule) this method
   * will only be called with things that "Can't Possibly Happen".
   * If this method is called, something is seriously wrong
   * (e.g. a JFlex bug producing a faulty scanner etc.).
   *
   * Usual syntax/scanner level error handling should be done
   * in error fallback rules.
   *
   * @param   errorCode  the code of the errormessage to display
   */
  private void zzScanError(int errorCode) {
    String message;
    try {
      message = ZZ_ERROR_MSG[errorCode];
    } catch(ArrayIndexOutOfBoundsException e) {
      message = ZZ_ERROR_MSG[ZZ_UNKNOWN_ERROR];
    }
    throw new Error(message);
  }

  /**
   * Resumes scanning until the next regular expression is matched,
   * the end of input is encountered or an I/O-Error occurs.
   *
   * @return      the next token
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  private String getToken() throws java.io.IOException {
    int zzInput;
    int zzAction;
    // cached fields:
    int zzCurrentPosL;
    int zzMarkedPosL;
    int zzEndReadL = zzEndRead;
    char[] zzBufferL = zzBuffer;
    char[] zzCMapL = ZZ_CMAP;
    int[] zzTransL = ZZ_TRANS;
    int[] zzRowMapL = ZZ_ROWMAP;
    int[] zzAttrL = ZZ_ATTRIBUTE;
    while(true) {
      zzMarkedPosL = zzMarkedPos;
      zzAction = -1;
      zzCurrentPosL = zzCurrentPos = zzStartRead = zzMarkedPosL;
      zzState = zzLexicalState;
      zzForAction:
      {
        while(true) {
          if(zzCurrentPosL<zzEndReadL) {
            zzInput = zzBufferL[zzCurrentPosL++];
          } else if(zzAtEOF) {
            zzInput = YYEOF;
            break zzForAction;
          } else {
            // store back cached positions
            zzCurrentPos = zzCurrentPosL;
            zzMarkedPos = zzMarkedPosL;
            boolean eof = zzRefill();
            // get translated positions and possibly new buffer
            zzCurrentPosL = zzCurrentPos;
            zzMarkedPosL = zzMarkedPos;
            zzBufferL = zzBuffer;
            zzEndReadL = zzEndRead;
            if(eof) {
              zzInput = YYEOF;
              break zzForAction;
            }
            zzInput = zzBufferL[zzCurrentPosL++];
          }
          int zzNext = zzTransL[zzRowMapL[zzState]+zzCMapL[zzInput]];
          if(zzNext==-1) {
            break zzForAction;
          }
          zzState = zzNext;
          int zzAttributes = zzAttrL[zzState];
          if((zzAttributes&1)==1) {
            zzAction = zzState;
            zzMarkedPosL = zzCurrentPosL;
            if((zzAttributes&8)==8) {
              break zzForAction;
            }
          }
        }
      }
      // store back cached position
      zzMarkedPos = zzMarkedPosL;
      switch((zzAction<0) ? zzAction : ZZ_ACTION[zzAction]) {
         case 3 : {
           return unescape(yytext().substring(1, yytext().length()-1));
         }
         case 4 :
           break;
         case 2 :
         case 5 :
           break;
         case 1 : {
           return unescape(yytext());
         }
         case 6 :
           break;
         default :
           if((zzInput==YYEOF)&&(zzStartRead==zzCurrentPos)) {
             zzAtEOF = true;
             return null;
           }
           zzScanError(ZZ_NO_MATCH);
      }
    }
  }

}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.

 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be released
 * under the GNU GPL license.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2024  The Open Source Physics project
 *                     http://www.opensourcephysics.org
 */
